package com.abjt.vmeet.utils;

import java.util.HashMap;

public class Constants {
    //Notification
    //Channel Name -> VMeet Notification
    public static final String PUSH_NOTIFICATION_CHANNEL_ID = "VMeet Notification Channel";


    //Firebase Firestore
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_EMAIl = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_FCM_TOKEN = "fcm_token";


    //Shared Preference Manger
    public static final String KEY_SHARED_PREFERENCE_NAME = "VMeetSharedPreference";

    //Authentication
    public static final String KEY_IS_SIGNED_IN = "is_signed_in";


    //Meeting
    public static final String KEY_USER = "user";
    public static final String KEY_MEETING_TYPE = "meetingType";
    public static final String MEETING_TYPE_VIDEO = "video";
    public static final String MEETING_TYPE_AUDIO = "audio";

    //Sending Remote Invitation
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String CHANNEL_ID = "channel_id";
    public static final String NOTIFICATION = "notification";


    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";


    //Sending Invitation Response
    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";


    //Sending Multiple Invitation
    public static final String SELECTED_USERS = "selectedUsers";
    public static final String IS_MULTIPLE_SELECTED = "isMultipleSelected";

    //Cancel Invitation
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";


    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=your_server_key_here"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;
    }


    //Battery Optimizations
    public static final int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

}
