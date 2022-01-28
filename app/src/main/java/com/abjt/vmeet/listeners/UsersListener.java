package com.abjt.vmeet.listeners;

import com.abjt.vmeet.models.User;

public interface UsersListener {

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);

    void onMultipleUsersAction(Boolean isMultipleUsersSelected);

}
