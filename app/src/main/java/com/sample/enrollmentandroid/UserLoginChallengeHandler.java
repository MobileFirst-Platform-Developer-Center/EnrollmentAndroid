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
import com.worklight.wlclient.api.WLLoginResponseListener;
import com.worklight.wlclient.api.WLLogoutResponseListener;
import com.worklight.wlclient.api.challengehandler.WLChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class UserLoginChallengeHandler extends WLChallengeHandler {
    private String challengeHandlerName = "UserLoginChallengeHandler";
    private static String securityCheckName = "EnrollmentUserLogin";
    private String errorMsg = "";
    private Context context;
    private boolean isChallenged = false;

    private LocalBroadcastManager broadcastManager;

    private UserLoginChallengeHandler() {
        super(securityCheckName);
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        //Receive login requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    JSONObject credentials = new JSONObject(intent.getStringExtra("credentials"));
                    login(credentials);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new IntentFilter(Constants.ACTION_USERLOGIN_SUBMIT_ANSWER));

        //Receive logout requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                logout();
            }
        }, new IntentFilter(Constants.ACTION_LOGOUT));
    }

    public static UserLoginChallengeHandler createAndRegister(){
        UserLoginChallengeHandler challengeHandler = new UserLoginChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }


    @Override
    public void handleChallenge(JSONObject response) {
        Log.d(challengeHandlerName, "handleChallenge" + response.toString());
        isChallenged = true;
        try {
            if(response.isNull("errorMsg")){
                errorMsg = "";
            }
            else{
                errorMsg = response.getString("errorMsg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_USERLOGIN_CHALLENGE_RECEIVED);
        intent.putExtra("errorMsg", errorMsg);
        broadcastManager.sendBroadcast(intent);

    }

    @Override
    public void handleFailure(JSONObject error) {
        super.handleFailure(error);
        isChallenged = false;
        Log.d(challengeHandlerName, "handleFailure: " + error.toString());
    }

    @Override
    public void handleSuccess(JSONObject identity) {
        super.handleSuccess(identity);
        Log.d(securityCheckName, "handleSuccess" + identity.toString());
        isChallenged = false;
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_USERLOGIN_CHALLENGE_SUCCESS);
        broadcastManager.sendBroadcast(intent);
    }

    public void login(JSONObject credentials){
        if(isChallenged){
            submitChallengeAnswer(credentials);
        }
        else{
            WLAuthorizationManager.getInstance().login(securityCheckName, credentials, new WLLoginResponseListener() {
                @Override
                public void onSuccess() {
                    Log.d(challengeHandlerName, "Login onSuccess");
                }

                @Override
                public void onFailure(WLFailResponse wlFailResponse) {
                    Log.d(challengeHandlerName, "Login onFailure: " + wlFailResponse.toString());
                }
            });
        }
    }

    public void logout(){
        WLAuthorizationManager.getInstance().logout(securityCheckName, new WLLogoutResponseListener() {
            @Override
            public void onSuccess() {
                Log.d(challengeHandlerName, "Logout onSuccess");
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_USERLOGIN_LOGOUT_SUCCESS);
                broadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d(challengeHandlerName, "Logout onFailure: " + wlFailResponse.toString());
            }
        });
    }
}
