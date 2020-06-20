package com.falcon.switchapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

public class NotificationService  extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String SHARED_PREF_KEY = "NEWS_DATA";
    private static final String NEWS_KEY_PREFIX = "NEWS_KEY_";
    private static final String NEWS_TITLE_KEY_SUFFIX = "_TITLE";
    private static final String NEWS_IMAGE_KEY_SUFFIX = "_IMAGE";
    private static final String NEWS_CONTENT_KEY_SUFFIX = "_CONTENT";
    private static final String NEWS_COUNT_KEY = "NEWS_COUNT_KEY";
    private SharedPreferences sharedPref;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            sharedPref = this.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            int newsCount = sharedPref.getInt(NEWS_COUNT_KEY,0);
            editor.putString(NEWS_KEY_PREFIX+String.valueOf(newsCount)+NEWS_TITLE_KEY_SUFFIX, remoteMessage.getNotification().getTitle());
            editor.putString(NEWS_KEY_PREFIX+String.valueOf(newsCount)+NEWS_IMAGE_KEY_SUFFIX, remoteMessage.getNotification().getImageUrl().toString());
            editor.putString(NEWS_KEY_PREFIX+String.valueOf(newsCount)+NEWS_CONTENT_KEY_SUFFIX, remoteMessage.getNotification().getBody());
            editor.putInt(NEWS_COUNT_KEY, newsCount+1);
            editor.commit();
            sendNotification(remoteMessage.getNotification());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param notif FCM message body received.
     */
    private void sendNotification(RemoteMessage.Notification notif) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "switchapp";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle(notif.getTitle())
                        .setContentText(notif.getBody())
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        try {
            Bitmap image = Glide.with(this).asBitmap().load(notif.getImageUrl()).submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
