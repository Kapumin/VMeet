package com.abjt.vmeet.authentication;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.abjt.vmeet.R;
import com.abjt.vmeet.activities.MainActivity;
import com.abjt.vmeet.utils.Constants;
import com.abjt.vmeet.utils.SharedPreferenceManager;
import com.abjt.vmeet.utils.Toaster;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private MaterialButton signInButton;
    private ProgressBar loading;

    private Toaster toaster;
    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();
        setListeners();

    }

    private void init() {
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        signInButton = findViewById(R.id.buttonSignIn);
        loading = findViewById(R.id.loading);

        //Custom
        if (getApplicationContext() != null) {
            sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
            if (sharedPreferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
            toaster = new Toaster(getApplicationContext());
        }
    }

    private void setListeners() {
        findViewById(R.id.textSignUp).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        signInButton.setOnClickListener(v -> validate());
    }

    private void validate() {
        if (inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
        } else if (inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
        } else {
            signIn();
        }
    }

    private void signIn() {
        toggleLoading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIl, inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        sharedPreferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        sharedPreferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        sharedPreferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        sharedPreferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        sharedPreferenceManager.putString(Constants.KEY_EMAIl, documentSnapshot.getString(Constants.KEY_EMAIl));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        showToast("Logged In");
                        startActivity(intent);
                    } else {
                        toggleLoading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    private void showToast(String message) {
        toaster.showToast(message);
    }

    private void toggleLoading(Boolean isLoading) {
        if (isLoading) {
            signInButton.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            loading.setVisibility(View.INVISIBLE);
        }
    }

}