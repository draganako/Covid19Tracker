package com.example.covid19tracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

public class OneSNotificationOpenedHandler implements OneSignal.OSNotificationOpenedHandler {

    private Context mContext;
    SharedPreferences sharedPreferences;

    public OneSNotificationOpenedHandler(Context context) {
        mContext = context;
        sharedPreferences=context.getSharedPreferences("Userdata", Context.MODE_PRIVATE);
    }

    @Override
    public void notificationOpened(OSNotificationOpenedResult result)
    {
            String title = result.getNotification().getTitle();
            String message = result.getNotification().getBody();

            Intent intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("push_title", title);
            intent.putExtra("push_message", message);
            mContext.startActivity(intent);
    }
}