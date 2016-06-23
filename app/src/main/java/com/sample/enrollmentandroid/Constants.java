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

public class Constants {
    static final String ACTION_USERLOGIN_SUBMIT_ANSWER = "com.sample.enrollmentandroid.broadcast.userlogin.submit.answer";
    static final String ACTION_USERLOGIN_CHALLENGE_RECEIVED = "com.sample.enrollmentandroid.broadcast.userlogin.challenge.received";
    static final String ACTION_USERLOGIN_CHALLENGE_SUCCESS = "com.sample.enrollmentandroid.broadcast.userlogin.challenge.success";
    static final String ACTION_USERLOGIN_LOGOUT_SUCCESS = "com.sample.enrollmentandroid.broadcast.userlogin.logout.success";


    static final String ACTION_PINCODE_CHALLENGE_RECEIVED = "com.sample.enrollmentandroid.broadcast.pincode.challenge.received";
    static final String ACTION_PINCODE_SUBMIT_ANSWER = "com.sample.enrollmentandroid.broadcast.pincode.submit.answer";
    static final String ACTION_PINCODE_CHALLENGE_CANCEL = "com.sample.enrollmentandroid.broadcast.pincode.challenge.cancel";
    static final String ACTION_PINCODE_LOGOUT_SUCCESS = "com.sample.enrollmentandroid.broadcast.pincode.logout.success";
    static final String ACTION_PINCODE_CHALLENGE_FAILURE = "com.sample.enrollmentandroid.broadcast.pincode.challenge.failure";

    static final String ACTION_ISENROLLED_LOGOUT_SUCCESS = "com.sample.enrollmentandroid.broadcast.isenrolled.logout.success";
    static final String ACTION_ISENROLLED_CHALLENGE_SUCCESS = "com.sample.enrollmentandroid.broadcast.isenrolled.challenge.success";

    static final String ACTION_LOGOUT = "com.sample.enrollmentandroid.broadcast.logout";
    static final String PREFERENCES_FILE = "com.sample.enrollmentandroid.preferences";
    public static final int USER_LOGIN = 1;
}
