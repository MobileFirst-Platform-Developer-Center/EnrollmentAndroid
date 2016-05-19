package com.sample.enrollmentandroid;

import android.app.Application;

import com.worklight.wlclient.api.WLClient;

public class EnrollmentApplication extends Application {
    public void onCreate () {
        super.onCreate();
        WLClient.createInstance(this);
        PinCodeChallengeHandler.createAndRegister();
        UserLoginChallengeHandler.createAndRegister();
    }
}
