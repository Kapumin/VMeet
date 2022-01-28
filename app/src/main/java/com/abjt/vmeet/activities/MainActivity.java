package com.abjt.vmeet.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.abjt.vmeet.R;
import com.abjt.vmeet.adapters.UsersAdapter;
import com.abjt.vmeet.authentication.SignInActivity;
import com.abjt.vmeet.listeners.UsersListener;
import com.abjt.vmeet.models.User;
import com.abjt.vmeet.utils.Constants;
import com.abjt.vmeet.utils.SharedPreferenceManager;
import com.abjt.vmeet.utils.Toaster;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsersListener {

    private SharedPreferenceManager sharedPreferenceManager;
    private Toaster toaster;

    private List<User> users;
    private UsersAdapter usersAdapter;
    private TextView textErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ImageView imageConference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getFCMToken();
        getUsers();
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        sendFCMTokenToDatabase(task.getResult());
                    }
                })
                .addOnFailureListener(e -> toaster.showToast("Token retrieve failed"));
    }

    private void init() {

        //Custom
        if (getApplicationContext() != null) {
            sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
            toaster = new Toaster(getApplicationContext());
        }

        //Header Title
        TextView textTitle = findViewById(R.id.textTitle);
        textTitle.setText(String.format(
                "%s %s",
                sharedPreferenceManager.getString(Constants.KEY_FIRST_NAME),
                sharedPreferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        //Sign Out
        findViewById(R.id.textSignOut).setOnClickListener(v -> signOut());

        //Users
        RecyclerView usersRecyclerView = findViewById(R.id.usersRecyclerView);
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        usersRecyclerView.setAdapter(usersAdapter);

        //Text Error Message
        textErrorMessage = findViewById(R.id.textErrorMessage);

        //Swipe Refresh Layout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        //imageConference
        imageConference = findViewById(R.id.imageConference);

        //Battery Optimizations
        checkForBatteryOptimizations();

    }

    private void sendFCMTokenToDatabase(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(sharedPreferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> toaster.showToast("Token Update Failed"));
    }


    @SuppressLint("NotifyDataSetChanged")
    private void getUsers() {
        swipeRefreshLayout.setRefreshing(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String myUserId = sharedPreferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        users.clear();
                        usersAdapter.notifyDataSetChanged();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            if (myUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIl);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            usersAdapter.notifyItemRangeInserted(0, users.size());
                        } else {
                            textErrorMessage.setText(String.format("%s", "No users available"));
                            textErrorMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        textErrorMessage.setText(String.format("%s", "No users available"));
                        textErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void signOut() {
        toaster.showToast("Signing Out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(sharedPreferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    sharedPreferenceManager.clearSharedPreference();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> toaster.showToast("Unable to sign out"));
    }

    @Override
    public void initiateVideoMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            toaster.showToast(user.firstName + " " + user.lastName + " is not available for meeting");
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            intent.putExtra(Constants.KEY_MEETING_TYPE, Constants.MEETING_TYPE_VIDEO);
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            toaster.showToast(user.firstName + " " + user.lastName + " is not available for meeting");
        } else {
            Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            intent.putExtra(Constants.KEY_MEETING_TYPE, Constants.MEETING_TYPE_AUDIO);
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected) {
            imageConference.setVisibility(View.VISIBLE);
            imageConference.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
                intent.putExtra(Constants.SELECTED_USERS, new Gson().toJson(usersAdapter.getSelectedUsers()));
                intent.putExtra(Constants.KEY_MEETING_TYPE, Constants.MEETING_TYPE_VIDEO);
                intent.putExtra(Constants.IS_MULTIPLE_SELECTED, true);
                startActivity(intent);
            });
        } else {
            imageConference.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void checkForBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning!");
                builder.setMessage("Battery Optimizations is enabled. It can interrupt running background services");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent, Constants.REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            checkForBatteryOptimizations();
        }
    }
}