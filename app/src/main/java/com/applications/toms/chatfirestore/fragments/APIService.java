package com.applications.toms.chatfirestore.fragments;

import com.applications.toms.chatfirestore.notifications.MyResponse;
import com.applications.toms.chatfirestore.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAFxwfcu4:APA91bG9j5z6psgq0_b_2IN1wDHKRY8wV9_z_gg51RQ1D5f3iRe9H_vFv5Wqs22kq1mgbbrpy74eDjT7LKbpELqGe5zTLahv0CX9j3J6FTXoZnr1yJqZZXCiPNpHQMhyKXaWg6xER5SZ"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
