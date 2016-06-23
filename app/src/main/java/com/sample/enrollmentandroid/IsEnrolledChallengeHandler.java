/*
 * Copyright 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sample.enrollmentandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLLogoutResponseListener;
import com.worklight.wlclient.api.challengehandler.SecurityCheckChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class IsEnrolledChallengeHandler extends SecurityCheckChallengeHandler{
    private Context context;
    private LocalBroadcastManager broadcastManager;

    private static String securityCheckName = "IsEnrolled";
    private static String challengeHandlerName = "IsEnrolledCH";

    public IsEnrolledChallengeHandler() {
        super(securityCheckName);
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        //Receive logout requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                logout();
            }
        },new IntentFilter(Constants.ACTION_PINCODE_LOGOUT_SUCCESS));
    }

    public static IsEnrolledChallengeHandler createAndRegister(){
        IsEnrolledChallengeHandler challengeHandler = new IsEnrolledChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }

    @Override
    public void handleChallenge(JSONObject challenge) {
        Log.d(challengeHandlerName, "handleChallenge" + challenge.toString());
    }

    @Override
    public void handleSuccess(JSONObject identity) {
        super.handleSuccess(identity);
        Log.d(challengeHandlerName, "handleSuccess" + identity.toString());
        Intent intent = new Intent();
        try {
            String userDisplayName = identity.getJSONObject("user").getString("displayName");
            intent.putExtra("displayName", userDisplayName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.setAction(Constants.ACTION_ISENROLLED_CHALLENGE_SUCCESS);
        broadcastManager.sendBroadcast(intent);
    }

    private void logout() {
        Log.d(challengeHandlerName, "logout");
        WLAuthorizationManager.getInstance().logout("IsEnrolled", new WLLogoutResponseListener() {
            @Override
            public void onSuccess() {
                Log.d(challengeHandlerName, "Logout onSuccess");
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_ISENROLLED_LOGOUT_SUCCESS);
                broadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d(challengeHandlerName, "Logout onFailure: " + wlFailResponse.toString());
            }
        });
    }
}

