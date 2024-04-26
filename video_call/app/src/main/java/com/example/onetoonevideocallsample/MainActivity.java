package com.example.onetoonevideocallsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.onetoonevideocallsample.R.id;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText etTo;
    private TextView tvStatus;
    private Button btnCall;
//    private String token = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTSy4wLlVMYm1nWE5wM3hHcGV4bzZKd01FWmRHT29SSm5HMExHLTE3MTMwMjg3ODQiLCJpc3MiOiJTSy4wLlVMYm1nWE5wM3hHcGV4bzZKd01FWmRHT29SSm5HMExHIiwiZXhwIjoxNzE1NjIwNzg0LCJ1c2VySWQiOiJ1c2VyMSJ9.P8EkKCRKmq5uEMv_ZNBxDIM3tf2or38GdBG19yf-QVM";

    private String token = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTSy4wLlVMYm1nWE5wM3hHcGV4bzZKd01FWmRHT29SSm5HMExHLTE3MTMwMjU5MzQiLCJpc3MiOiJTSy4wLlVMYm1nWE5wM3hHcGV4bzZKd01FWmRHT29SSm5HMExHIiwiZXhwIjoxNzE1NjE3OTM0LCJ1c2VySWQiOiJ1c2VyMiJ9.-vLJ5p6xZENlQ4_oRDib-Dn_beOrgTdAkl7MAkIFHeU";
    public static StringeeClient client;
    public static Map<String, StringeeCall2> call2Map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTo = findViewById(id.et_to);
        tvStatus = findViewById(id.tv_status);
        btnCall = findViewById(id.btn_call);
        btnCall.setOnClickListener(view -> {
            if (etTo.getText() == null || etTo.getText().toString().trim().length() <= 0) {
                return;
            }
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra("to", etTo.getText().toString().trim());
            intent.putExtra("isIncomingCall", false);
            startActivity(intent);
        });

        initStringeeConnection();
    }

    private void initStringeeConnection() {
        client = new StringeeClient(this);
        client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
                runOnUiThread(() -> {
                    tvStatus.setText("Connected as " + stringeeClient.getUserId());
                });
            }

            @Override
            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
                runOnUiThread(() -> {
                    tvStatus.setText("Disconnected");
                });
            }

            @Override
            public void onIncomingCall(StringeeCall stringeeCall) {
            }

            @Override
            public void onIncomingCall2(StringeeCall2 stringeeCall2) {
                runOnUiThread(()->{
                    call2Map.put(stringeeCall2.getCallId(), stringeeCall2);
                    Intent intent = new Intent(MainActivity.this, CallActivity.class);
                    intent.putExtra("callId", stringeeCall2.getCallId());
                    intent.putExtra("isIncomingCall", true);
                    startActivity(intent);
                });
            }

            @Override
            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
                runOnUiThread(() -> {
                    tvStatus.setText("Connect error: " + stringeeError.getMessage());
                });
            }

            @Override
            public void onRequestNewToken(StringeeClient stringeeClient) {

            }

            @Override
            public void onCustomMessage(String s, JSONObject jsonObject) {

            }

            @Override
            public void onTopicMessage(String s, JSONObject jsonObject) {

            }
        });
        client.connect(token);
    }
}