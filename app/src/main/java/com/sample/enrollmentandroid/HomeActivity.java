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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.worklight.wlclient.api.WLAccessTokenListener;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;
import com.worklight.wlclient.auth.AccessToken;

import java.net.URI;
import java.net.URISyntaxException;

public class HomeActivity extends AppCompatActivity {
    private final String activityName = "HomeActivity";
    private String displayName = "Guest";
    private boolean isEnrolled = false;
    private HomeActivity _this;
    private Button getBalanceBtn, getTransactionsBtn;
    private TextView resultTxt, helloUserTxt;
    private BroadcastReceiver loginRequiredReceiver, picodeRequiredReceiver, enrollmentRequiredReceiver, UIChangeRequiredReciver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultTxt = (TextView) findViewById(R.id.resultTxt);
        helloUserTxt  = (TextView) findViewById(R.id.helloUser);

        Button getPublicDataBtn = (Button) findViewById(R.id.getPublicData);
        assert getPublicDataBtn != null;
        getPublicDataBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d(activityName, "getPublicDataBtn");
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/ResourceAdapter/publicData");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        Log.d("getPublicData success: ", wlResponse.getResponseText());
                        updateTextView(wlResponse.getResponseText());
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d("getPublicData failure: ", wlFailResponse.getErrorMsg());
                    }
                });
            }
        });

        getTransactionsBtn = (Button) findViewById(R.id.getTransactions);
        getTransactionsBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d(activityName, "getTransactionsBtn");
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/ResourceAdapter/transactions");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        Log.d("transactions success: ", wlResponse.getResponseText());
                        updateTextView(wlResponse.getResponseText());
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d("transactions failure: ", wlFailResponse.getErrorMsg());
                    }
                });
            }
        });

        getBalanceBtn = (Button) findViewById(R.id.getBalance);
        getBalanceBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d(activityName, "getBalanceBtn");
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/ResourceAdapter/balance");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        Log.d("getBalance success: ", wlResponse.getResponseText());
                        updateTextView(wlResponse.getResponseText());
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d("getBalance failure: ", wlFailResponse.getErrorMsg());
                    }
                });
            }
        });

        //Receive login required requests
        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("loginRequiredReceiver","Receive login required requests");
                Intent loginActivity = new Intent(_this, LoginActivity.class);
                _this.startActivityForResult(loginActivity,Constants.USER_LOGIN);
            }
        };

        //Receive pincode required requests
        picodeRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showPinCodePopup(intent.getStringExtra("msg"));
            }
        };

        //Receive enrollment required requests
        enrollmentRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                enrollAfterFailure(intent.getStringExtra("errorMsg"));
            }
        };

        //Receive UI change required requests
        UIChangeRequiredReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra("displayName") != null){
                    displayName = intent.getStringExtra("displayName");
                    changeUIState("Hello, " + displayName, "visible", true);
                } else {
                    changeUIState("Hello, Guest", "invisible", false);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(activityName, "onStart");
        getTransactionsBtn.setVisibility(View.INVISIBLE);
        getBalanceBtn.setVisibility(View.INVISIBLE);


        LocalBroadcastManager.getInstance(this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_USERLOGIN_CHALLENGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(picodeRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_CHALLENGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(enrollmentRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_CHALLENGE_FAILURE));
        LocalBroadcastManager.getInstance(this).registerReceiver(UIChangeRequiredReciver, new IntentFilter(Constants.ACTION_ISENROLLED_LOGOUT_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(UIChangeRequiredReciver, new IntentFilter(Constants.ACTION_ISENROLLED_CHALLENGE_SUCCESS));

        WLAuthorizationManager.getInstance().obtainAccessToken("IsEnrolled", new WLAccessTokenListener() {
            @Override
            public void onSuccess(AccessToken accessToken) {
                Log.d("IsEnrolled", "onSuccess");
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d("IsEnrolled", "onFailure: " + wlFailResponse.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        MenuItem unenrollItem = menu.findItem(R.id.action_unenroll);
        MenuItem enrollItem = menu.findItem(R.id.action_enroll);

        if (isEnrolled) {
            unenrollItem.setVisible(true);
            enrollItem.setVisible(false);
        } else {
            unenrollItem.setVisible(false);
            enrollItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_unenroll:
                Log.d(activityName, "action_unenroll");
                unenroll();
                return true;
            case R.id.action_enroll:
                Log.d(activityName, "action_enroll");
                enroll();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void enrollAfterFailure(String errorMsg) {
        Log.d(activityName, "enrollAfterFailure");
        if (errorMsg.equals("Account blocked")){
            changeUIState("Hello, Guest", "invisible", false);
            enroll();
        }
    }

    private void showPinCodePopup(final String errorMsg) {
        Log.d(activityName, "showPinCodePopup");
        Runnable run = new Runnable() {
            public void run() {
                final Intent intent = new Intent();
                final EditText pinCodeTxt = new EditText(_this);
                pinCodeTxt.setHint("PIN CODE");
                pinCodeTxt.setInputType(InputType.TYPE_CLASS_NUMBER);

                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setMessage(errorMsg)
                        .setTitle("Pin Code");
                builder.setView(pinCodeTxt);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        intent.setAction(Constants.ACTION_PINCODE_SUBMIT_ANSWER);
                        intent.putExtra("pinCode", pinCodeTxt.getText().toString());
                        LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intent.setAction(Constants.ACTION_PINCODE_CHALLENGE_CANCEL);
                        LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        };

        _this.runOnUiThread(run);
    }

    private void enroll(){
        Log.d(activityName, "enroll");
        updateTextView("");
        WLAuthorizationManager.getInstance().obtainAccessToken("EnrollmentUserLogin", new WLAccessTokenListener() {
            @Override
            public void onSuccess(AccessToken accessToken) {
                Log.d("enroll", "onSuccess");
                showSetPincodeDialog("");
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d("enroll", "onFailure: " + wlFailResponse.toString());
            }
        });
    }

    private void showSetPincodeDialog(final String msg) {
        Log.d(activityName, "showSetPincodeDialog");
        Runnable run = new Runnable() {
            public void run() {
                final EditText pinCodeTxt = new EditText(_this);
                pinCodeTxt.setHint("CHOOSE A PIN CODE");
                pinCodeTxt.setInputType(InputType.TYPE_CLASS_NUMBER);

                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setMessage(msg)
                        .setTitle("Set Pin Code");
                builder.setView(pinCodeTxt);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setPinCode(pinCodeTxt.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Constants.ACTION_LOGOUT);
                        LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        };

        _this.runOnUiThread(run);
    }

    private void setPinCode(String pinCode) {
        Log.d(activityName, "setPinCode");
        if (pinCode.equals("")){
            showSetPincodeDialog("Pincode is required, please try again");
        } else {
            URI adapterPath = null;
            try {
                adapterPath = new URI("/adapters/Enrollment/setPinCode/" + pinCode);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.POST);
            request.send(new WLResponseListener() {
                @Override
                public void onSuccess(WLResponse wlResponse) {
                    Log.d("setPinCode success: ", wlResponse.getResponseText());
                    changeUIState("Hello, " + displayName, "visible", true);
                }

                @Override
                public void onFailure(WLFailResponse wlFailResponse) {
                    Log.d("setPinCode failure: ", wlFailResponse.getErrorMsg());
                }
            });
        }
    }

    private void changeUIState(final String helloUser, final String buttonsState, final Boolean actionState){
        Log.d(activityName, "changeUIState: "+helloUser);
        Runnable run = new Runnable() {
            public void run() {
                resultTxt.setText("");
                if (buttonsState.equals("visible")){
                    getTransactionsBtn.setVisibility(View.VISIBLE);
                    getBalanceBtn.setVisibility(View.VISIBLE);
                } else {
                    getTransactionsBtn.setVisibility(View.INVISIBLE);
                    getBalanceBtn.setVisibility(View.INVISIBLE);
                }
                helloUserTxt.setText(helloUser);
                isEnrolled = actionState;
                _this.invalidateOptionsMenu();
            }
        };

        _this.runOnUiThread(run);
    }



    private void unenroll() {
        URI adapterPath = null;
        try {
            adapterPath = new URI("/adapters/Enrollment/unenroll");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.DELETE);
        request.send(new WLResponseListener() {
            @Override
            public void onSuccess(WLResponse wlResponse) {
                Log.d("unenroll success: ", Integer.toString(wlResponse.getStatus()));
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_LOGOUT);
                LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d("unenroll failure: ", wlFailResponse.getErrorMsg());
            }
        });
    }


    public void updateTextView(final String str) {
        Log.d(activityName, "updateTextView");
        Runnable run = new Runnable() {
            public void run() {
                resultTxt.setText(str);
            }
        };
        this.runOnUiThread(run);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.USER_LOGIN) {
            if(resultCode == Activity.RESULT_OK){
                displayName = data.getStringExtra("displayName");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(activityName, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(picodeRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(enrollmentRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(UIChangeRequiredReciver);
    }
}