package com.abjt.vmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.abjt.vmeet.R;
import com.abjt.vmeet.models.User;
import com.abjt.vmeet.network.ApiClient;
import com.abjt.vmeet.network.ApiServices;
import com.abjt.vmeet.utils.Constants;
import com.abjt.vmeet.utils.SharedPreferenceManager;
import com.abjt.vmeet.utils.Toaster;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private SharedPreferenceManager sharedPreferenceManager;
    private String inviterToken;
    private Toaster toaster;

    private String meetingRoom = null;
    private String meetingType = null;
    private TextView textFirstChar;
    private TextView textUsername;
    private TextView textEmail;

    private int rejectionCount = 0;
    private int totalReceivers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);
        init();
    }

    private void init() {

        if (getApplicationContext() != null) {
            sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
            toaster = new Toaster(getApplicationContext());
        }

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        meetingType = getIntent().getStringExtra(Constants.KEY_MEETING_TYPE);


        //Set Meeting Type
        if (meetingType != null) {
            if (meetingType.equals(Constants.MEETING_TYPE_VIDEO)) {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            } else if (meetingType.equals(Constants.MEETING_TYPE_AUDIO)) {
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }

        //Receiver User Data
        textFirstChar = findViewById(R.id.textFirstChar);
        textUsername = findViewById(R.id.textUsername);
        textEmail = findViewById(R.id.textEmail);

        User user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);


        //Set Receiver User Details
        if (user != null) {
            textFirstChar.setText(user.firstName.substring(0, 1));
            textUsername.setText(String.format("%s %s", user.firstName, user.lastName));
            textEmail.setText(user.email);
        }


        //Cancel Invitation
        ImageView imageStopInvitation = findViewById(R.id.imageStopInvitation);
        imageStopInvitation.setOnClickListener(v -> {
            if (getIntent().getBooleanExtra(Constants.IS_MULTIPLE_SELECTED, false)) {
                Type type = new TypeToken<ArrayList<User>>() {
                }.getType();
                ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra(Constants.SELECTED_USERS), type);
                cancelInvitation(null, receivers);
            } else {
                if (user != null) {
                    cancelInvitation(user.token, null);
                }
            }

        });


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        inviterToken = task.getResult();
                        //initiateMeeting
                        if (meetingType != null) {
                            if (getIntent().getBooleanExtra(Constants.IS_MULTIPLE_SELECTED, false)) {
                                Type type = new TypeToken<ArrayList<User>>() {
                                }.getType();
                                ArrayList<User> receivers = new Gson().fromJson(getIntent().getStringExtra(Constants.SELECTED_USERS), type);
                                if (receivers != null) {
                                    totalReceivers = receivers.size();
                                }
                                initiateMeeting(meetingType, null, receivers);
                            } else {
                                if (user != null) {
                                    totalReceivers = 1;
                                    initiateMeeting(meetingType, user.token, null);
                                }
                            }
                        }


                    }
                })
                .addOnFailureListener(e -> toaster.showToast("Something went wring try again"));

    }

    private void initiateMeeting(String meetingType, String receiverToken, ArrayList<User> receivers) {
        try {
            JSONArray tokens = new JSONArray();

            if (receiverToken != null) {
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0) {
                StringBuilder userNames = new StringBuilder();
                for (int i = 0; i < receivers.size(); i++) {
                    tokens.put(receivers.get(i).token);
                    userNames.append(receivers.get(i).firstName).append(" ").append(receivers.get(i).lastName).append("\n");
                }
                textFirstChar.setVisibility(View.GONE);
                textEmail.setVisibility(View.GONE);
                textUsername.setText(userNames);
            }


            JSONObject body = new JSONObject();
            JSONObject notification = new JSONObject();
            JSONObject data = new JSONObject();

            //Notification Channel
            notification.put(Constants.CHANNEL_ID, Constants.PUSH_NOTIFICATION_CHANNEL_ID);

            //custom data field(key,value pairs)
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_FIRST_NAME, sharedPreferenceManager.getString(Constants.KEY_FIRST_NAME));
            data.put(Constants.KEY_LAST_NAME, sharedPreferenceManager.getString(Constants.KEY_LAST_NAME));
            data.put(Constants.KEY_EMAIl, sharedPreferenceManager.getString(Constants.KEY_EMAIl));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            meetingRoom =
                    sharedPreferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                            UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            //Body
            body.put(Constants.NOTIFICATION, notification);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            //send Invitation
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        } catch (Exception e) {
            toaster.showToast(e.getMessage());
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getRetrofit().create(ApiServices.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(),
                remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                        Timber.tag("Response :").d("Invitation Sent");
                    } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                        Timber.tag("Response :").d("Invitation Cancelled");
                        finish();
                    }
                } else {
                    toaster.showToast(response.message());
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                toaster.showToast("Failed : " + t.getMessage());
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken, ArrayList<User> receivers) {
        try {

            JSONArray tokens = new JSONArray();
            if (receiverToken != null) {
                tokens.put(receiverToken);
            }

            if (receivers != null && receivers.size() > 0) {
                for (User user : receivers) {
                    tokens.put(user.token);
                }
            }

            JSONObject body = new JSONObject();
            JSONObject notification = new JSONObject();
            JSONObject data = new JSONObject();

            //Notification Channel
            notification.put(Constants.CHANNEL_ID, Constants.PUSH_NOTIFICATION_CHANNEL_ID);

            //custom data field(key,value pairs)
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            //Body
            body.put(Constants.NOTIFICATION, notification);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            //send Invitation
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        } catch (Exception e) {
            toaster.showToast(e.getMessage());
        }
    }

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals(Constants.MEETING_TYPE_AUDIO)) {
                            builder.setVideoMuted(true);
                        } else if (meetingType.equals(Constants.MEETING_TYPE_VIDEO)) {
                            builder.setVideoMuted(false);
                        }
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this, builder.build());
                        finish();
                    } catch (Exception e) {
                        toaster.showToast(e.getMessage());
                        finish();
                    }
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    rejectionCount += 1;
                    if (rejectionCount == totalReceivers) {
                        toaster.showToast("Invitation Rejected");
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(
                        invitationResponseReceiver,
                        new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}