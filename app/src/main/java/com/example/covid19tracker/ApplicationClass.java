package com.example.covid19tracker;

import android.app.Application;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

public class ApplicationClass extends Application {
    private static final String ONESIGNAL_APP_ID = "88c353ba-5743-4f54-86a3-fb157d422e0b";

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        OneSignal.setNotificationOpenedHandler(new OneSNotificationOpenedHandler(this));
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }
}
