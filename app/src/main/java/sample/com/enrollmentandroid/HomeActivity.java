package sample.com.enrollmentandroid;

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
import com.worklight.wlclient.api.WLLogoutResponseListener;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;
import com.worklight.wlclient.auth.AccessToken;

import java.net.URI;
import java.net.URISyntaxException;

public class HomeActivity extends AppCompatActivity {
    private final String activityName = "HomeActivity";
    private boolean isEnrolled = false;
    private HomeActivity _this;
    private Button getBalanceBtn, getTransactionsBtn;
    private TextView resultTxt;
    private BroadcastReceiver loginRequiredReceiver, picodeRequiredReceiver, enrollmentRequiredReceiver, isEnrolledLogoutRequiredReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultTxt = (TextView) findViewById(R.id.resultTxt);

        Button getPublicDataBtn = (Button) findViewById(R.id.getPublicData);
        assert getPublicDataBtn != null;
        getPublicDataBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/Enrollment/publicData");
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
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/Enrollment/transactions");
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
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/Enrollment/balance");
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
                Intent loginActivity = new Intent(_this, LoginActivity.class);
                _this.startActivity(loginActivity);
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

        //Receive isEnrolled logout required requests
        isEnrolledLogoutRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isEnrolledLogout();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        getTransactionsBtn.setVisibility(View.INVISIBLE);
        getBalanceBtn.setVisibility(View.INVISIBLE);


        LocalBroadcastManager.getInstance(this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_USERLOGIN_CHALLENGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(picodeRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_CHALLENGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(enrollmentRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_CHALLENGE_FAILURE));
        LocalBroadcastManager.getInstance(this).registerReceiver(isEnrolledLogoutRequiredReceiver, new IntentFilter(Constants.ACTION_PINCODE_LOGOUT_SUCCESS));

        URI adapterPath = null;
        try {
            adapterPath = new URI("/adapters/Enrollment/isEnrolled");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
        request.send(new WLResponseListener() {
            @Override
            public void onSuccess(WLResponse wlResponse) {
                isEnrolled = Boolean.valueOf(wlResponse.getResponseText());
                if (isEnrolled){
                    Log.d("isEnrolled success: ", Integer.toString(wlResponse.getStatus()));
                    changeUIState("visible", isEnrolled);
                } else {
                    Log.d("isEnrolled success: ", wlResponse.getResponseText());
                }
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d("isEnrolled failure: ", wlFailResponse.getErrorMsg());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);

        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        MenuItem enrollItem = menu.findItem(R.id.action_enroll);

        if (isEnrolled) {
            logoutItem.setVisible(true);
            enrollItem.setVisible(false);
        } else {
            logoutItem.setVisible(false);
            enrollItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                Log.d(activityName, "action_logout");
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_LOGOUT);
                LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
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
        if (errorMsg.equals("Account blocked")){
            enroll();
        }
    }

    private void showPinCodePopup(final String errorMsg) {
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
        updateTextView("");
        WLAuthorizationManager.getInstance().obtainAccessToken("setPinCode", new WLAccessTokenListener() {
            @Override
            public void onSuccess(AccessToken accessToken) {
                Log.d("setPinCode", "onSuccess");
                showSetPincodeDialog("");
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d("setPinCode", "onFailure: " + wlFailResponse.toString());
            }
        });
    }

    private void showSetPincodeDialog(final String msg) {
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
                    changeUIState("visible", true);
                }

                @Override
                public void onFailure(WLFailResponse wlFailResponse) {
                    Log.d("setPinCode failure: ", wlFailResponse.getErrorMsg());
                }
            });
        }
    }

    private void changeUIState(final String buttonsState, final Boolean actionState){
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
                isEnrolled = actionState;
                _this.invalidateOptionsMenu();
            }
        };

        _this.runOnUiThread(run);
    }

    private void isEnrolledLogout() {
        WLAuthorizationManager.getInstance().logout("IsEnrolled", new WLLogoutResponseListener() {
            @Override
            public void onSuccess() {
                Log.d("IsEnrolled", "Logout onSuccess");
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/Enrollment/deletePinCode");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.DELETE);
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        Log.d("deletePinCode success: ", Integer.toString(wlResponse.getStatus()));
                        changeUIState("invisible", false);
                    }

                    @Override
                    public void onFailure(WLFailResponse wlFailResponse) {
                        Log.d("deletePinCode failure: ", wlFailResponse.getErrorMsg());
                    }
                });
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d("IsEnrolled", "Logout onFailure: " + wlFailResponse.toString());
            }
        });
    }

    public void updateTextView(final String str) {
        Runnable run = new Runnable() {
            public void run() {
                resultTxt.setText(str);
            }
        };
        this.runOnUiThread(run);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(picodeRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(isEnrolledLogoutRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(enrollmentRequiredReceiver);
    }
}
