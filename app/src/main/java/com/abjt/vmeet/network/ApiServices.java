package com.abjt.vmeet.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiServices {

    @POST("send")
    Call<String> sendRemoteMessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String remoteBody
    );
}
