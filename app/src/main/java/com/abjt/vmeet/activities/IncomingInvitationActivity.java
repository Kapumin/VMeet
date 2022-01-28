package com.abjt.vmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.abjt.vmeet.R;
import com.abjt.vmeet.network.ApiClient;
import com.abjt.vmeet.network.ApiServices;
import com.abjt.vmeet.utils.Constants;
import com.abjt.vmeet.utils.Toaster;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class IncomingInvitationActivity extends AppCompatActivity {
    private Toaster toaster;
    private String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);

        init();
    }


    private void init() {

        if (getApplicationContext() != null) {
            toaster = new Toaster(getApplicationContext());
        }


        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);

        //get User Data
        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        String lastName = getIntent().getStringExtra(Constants.KEY_LAST_NAME);
        String email = getIntent().getStringExtra(Constants.KEY_EMAIl);

        //Set Meeting Type
        if (meetingType != null) {
            if (meetingType.equals(Constants.MEETING_TYPE_VIDEO)) {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            } else if (meetingType.equals(Constants.MEETING_TYPE_AUDIO)) {
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }


        TextView textFirstChar = findViewById(R.id.textFirstChar);
        TextView textUsername = findViewById(R.id.textUsername);
        TextView textEmail = findViewById(R.id.textEmail);

        if (firstName != null) {
            textFirstChar.setText(firstName.substring(0, 1));
            textUsername.setText(String.format("%s %s", firstName, lastName));
        }

        if (email != null) {
            textEmail.setText(email);
        }


        //Response
        //Accept
        ImageView imageAcceptInvitation = findViewById(R.id.imageAcceptInvitation);
        imageAcceptInvitation.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

        //Reject
        ImageView imageRejectInvitation = findViewById(R.id.imageRejectInvitation);
        imageRejectInvitation.setOnClickListener(v -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN))
        );

    }


    private void sendInvitationResponse(String type, String receiverToken) {
        try {

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);


            JSONObject body = new JSONObject();
            JSONObject notification = new JSONObject();
            JSONObject data = new JSONObject();

            //Notification Channel
            notification.put(Constants.CHANNEL_ID, Constants.PUSH_NOTIFICATION_CHANNEL_ID);

            //custom data field(key,value pairs)
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            //Body
            body.put(Constants.NOTIFICATION, notification);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            //send Invitation
            sendRemoteMessage(body.toString(), type);

        } catch (Exception e) {
            toaster.showToast(e.getMessage());
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
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                        try {
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));
                            if (meetingType.equals(Constants.MEETING_TYPE_AUDIO)) {
                                builder.setVideoMuted(true);
                            } else if (meetingType.equals(Constants.MEETING_TYPE_VIDEO)) {
                                builder.setVideoMuted(false);
                            }
                            JitsiMeetActivity.launch(IncomingInvitationActivity.this, builder.build());
                            finish();
                        } catch (Exception e) {
                            toaster.showToast(e.getMessage());
                            finish();
                        }
                    } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                        Timber.tag("Response").d("Invitation Rejected");
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

    private final BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                    toaster.showToast("Invitation Cancelled");
                    finish();
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