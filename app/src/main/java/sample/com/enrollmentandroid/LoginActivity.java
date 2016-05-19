package sample.com.enrollmentandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput;
    private TextView errorTxt;
    private BroadcastReceiver showErrorReceiver, popLoginPageRequiredReceiver;
    private LoginActivity _this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        _this = this;

        usernameInput = (EditText) findViewById(R.id.username);
        passwordInput = (EditText) findViewById(R.id.password);

        errorTxt = (TextView) findViewById(R.id.error);

        Button loginBtn = (Button) findViewById(R.id.login);
        assert loginBtn != null;
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usernameInput.getText().toString().isEmpty() || passwordInput.getText().toString().isEmpty()){
                    errorTxt.setText("Username and password are required");
                }
                else{
                    JSONObject credentials = new JSONObject();
                    try {
                        credentials.put("username",usernameInput.getText().toString());
                        credentials.put("password",passwordInput.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent();
                    intent.setAction(Constants.ACTION_USERLOGIN_SUBMIT_ANSWER);
                    intent.putExtra("credentials",credentials.toString());
                    LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                }
            }
        });

        //Receive login required requests
        showErrorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                errorTxt.setText(intent.getStringExtra("errorMsg"));
            }
        };

        //Receive pop login page required requests
        popLoginPageRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(showErrorReceiver, new IntentFilter(Constants.ACTION_USERLOGIN_CHALLENGE_RECEIVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(popLoginPageRequiredReceiver, new IntentFilter(Constants.ACTION_USERLOGIN_CHALLENGE_SUCCESS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(popLoginPageRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showErrorReceiver);
    }
}
