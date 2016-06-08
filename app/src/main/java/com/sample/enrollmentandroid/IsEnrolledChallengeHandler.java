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

import android.util.Log;

import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.challengehandler.SecurityCheckChallengeHandler;

import org.json.JSONObject;

public class IsEnrolledChallengeHandler extends SecurityCheckChallengeHandler{

    private static String securityCheckName = "IsEnrolled";

    public IsEnrolledChallengeHandler() {
        super(securityCheckName);
    }

    public static IsEnrolledChallengeHandler createAndRegister(){
        IsEnrolledChallengeHandler challengeHandler = new IsEnrolledChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }

    @Override
    public void handleChallenge(JSONObject challenge) {
        Log.d("IsEnrolledCH", "handleChallenge" + challenge.toString());
    }
}
