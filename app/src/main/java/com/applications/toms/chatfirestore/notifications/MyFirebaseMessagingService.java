package com.applications.toms.chatfirestore.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.applications.toms.chatfirestore.MessageActivity;
import com.applications.toms.chatfirestore.adapter.UserAdapter;
import com.applications.toms.chatfirestore.fragments.ChatsFragment;
import com.applications.toms.chatfirestore.util.Keys;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TOM-FirebaseMsgServ";

    @Override
    public void onNewToken(@NonNull String s) {
        updateTokenDB(s);
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Al llegar una notificación llega a través de este metodo
        String sentedTo = remoteMessage.getData().get(Keys.KEY_MSG_SENTEDTO);
        String user = remoteMessage.getData().get(Keys.KEY_MSG_USER);

        SharedPreferences preferences = getSharedPreferences("PREFS",MODE_PRIVATE);
        String currentUser = preferences.getString("currentuser","none");

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (!currentUser.equals(user)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationThroughChannel(remoteMessage);
                //ChatsFragment.refresh(user);
            } else {
                if (fuser != null && sentedTo.equals(fuser.getUid())) {
                    sendNotification(remoteMessage);
                    //ChatsFragment.refresh(user);
                }
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationThroughChannel(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get(Keys.KEY_MSG_USER);
        String icon = remoteMessage.getData().get(Keys.KEY_MSG_ICON);
        String title = remoteMessage.getData().get(Keys.KEY_MSG_TITLE);
        String body = remoteMessage.getData().get(Keys.KEY_MSG_BODY);

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        long j = Long.parseLong(user.replaceAll("\\D",""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Keys.KEY_MSG_USERID,user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, Math.toIntExact(j),intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        ChannelForNotifications channelForNotifications = new ChannelForNotifications(this);
        Notification.Builder builder = channelForNotifications.getNotification(title,body,pendingIntent,defaultSound,icon);

        int i = 0;
        if (j>0){
            i=Math.toIntExact(j);
        }

        channelForNotifications.getManager().notify(i,builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get(Keys.KEY_MSG_USER);
        String icon = remoteMessage.getData().get(Keys.KEY_MSG_ICON);
        String title = remoteMessage.getData().get(Keys.KEY_MSG_TITLE);
        String body = remoteMessage.getData().get(Keys.KEY_MSG_BODY);

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        long j = Long.parseLong(user.replaceAll("\\D",""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Keys.KEY_MSG_USERID,user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) j,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
         if (j>0){
             i= (int) j;
         }

         notificationManager.notify(i,builder.build());
    }

    private void updateTokenDB(String token){
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore reference = FirebaseFirestore.getInstance();

        if (fuser != null) {
            DocumentReference userRef = reference.collection(Keys.KEY_USERS).document(fuser.getUid());
            userRef.update(Keys.KEY_USERS_TOKEN, token);
        }
    }


}
