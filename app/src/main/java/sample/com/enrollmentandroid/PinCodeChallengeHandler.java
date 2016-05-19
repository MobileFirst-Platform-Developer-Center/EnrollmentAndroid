package sample.com.enrollmentandroid;

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
import com.worklight.wlclient.api.challengehandler.WLChallengeHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class PinCodeChallengeHandler extends WLChallengeHandler{
    private Context context;
    private LocalBroadcastManager broadcastManager;
    private String challengeHandlerName = "PinCodeChallengeHandler";
    private static String securityCheckName = "EnrollmentPinCode";

    private PinCodeChallengeHandler() {
        super(securityCheckName);
        context = WLClient.getInstance().getContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        //Receive submit answer requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String credentials = intent.getStringExtra("pinCode");
                submitAnswer(credentials);
            }
        },new IntentFilter(Constants.ACTION_PINCODE_SUBMIT_ANSWER));

        //Receive cancel challenge requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(challengeHandlerName, "cancel");
                submitFailure(null);
            }
        },new IntentFilter(Constants.ACTION_PINCODE_CHALLENGE_CANCEL));

        //Receive logout requests
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               logout();
            }
        }, new IntentFilter(Constants.ACTION_USERLOGIN_LOGOUT_SUCCESS));
    }

    public static PinCodeChallengeHandler createAndRegister(){
        PinCodeChallengeHandler challengeHandler = new PinCodeChallengeHandler();
        WLClient.getInstance().registerChallengeHandler(challengeHandler);
        return challengeHandler;
    }

    public void submitAnswer(String credentials) {
        Log.d(challengeHandlerName, "submitAnswer");
        try {
            submitChallengeAnswer(new JSONObject().put("pin", credentials));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleChallenge(JSONObject response) {
        Log.d(challengeHandlerName, "handleChallenge" + response.toString());
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_CHALLENGE_RECEIVED);
        try{
            if (response.isNull("errorMsg")){
                intent.putExtra("msg", "Enter PIN code:");
            } else {
                intent.putExtra("msg", response.getString("errorMsg") + "\nRemaining attempts: " + response.getString("remainingAttempts"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    public void handleSuccess(JSONObject identity) {
        super.handleSuccess(identity);
        Log.d(challengeHandlerName, "handleSuccess" + identity.toString());
    }

    @Override
    public void handleFailure(JSONObject error) {
        super.handleFailure(error);
        Log.d(challengeHandlerName, "handleFailure" + error.toString());
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_PINCODE_CHALLENGE_FAILURE);
        try {
            if (!error.isNull("failure")) {
                intent.putExtra("errorMsg", error.getString("failure"));
            } else {
                intent.putExtra("errorMsg", "Unknown error");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        broadcastManager.sendBroadcast(intent);
    }


    private void logout() {
        Log.d(challengeHandlerName, "logout");
        WLAuthorizationManager.getInstance().logout(securityCheckName, new WLLogoutResponseListener() {
            @Override
            public void onSuccess() {
                Log.d(challengeHandlerName, "logout onSuccess");
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_PINCODE_LOGOUT_SUCCESS);
                broadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d(challengeHandlerName, "logout onFailure: " + wlFailResponse.toString());
            }
        });
    }
}
