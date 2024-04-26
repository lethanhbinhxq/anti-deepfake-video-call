package com.example.onetoonevideocallsample;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.onetoonevideocallsample.R.drawable;
import com.example.onetoonevideocallsample.R.id;
import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.call.StringeeCall2.StringeeCallListener;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideoTrack;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class CallActivity extends AppCompatActivity  {
    private TextView tvStatus;
    private View vIncoming;
    private FrameLayout vLocal;
    private FrameLayout vRemote;
    private ImageButton btnSpeaker;
    private ImageButton btnMute;
    private ImageButton btnSwitch;
    private ImageButton btnVideo;
    private ImageButton btnAnswer;
    private ImageButton btnCapture;

    private ImageButton btnSms;
    private ImageButton buttonUpload;
    private ImageButton btnReject;
    private ImageButton btnEnd;

    private StringeeCall2 call;
    private boolean isIncomingCall = false;
    private String to;
    private String callId;
    private SignalingState mSignalingState;
    private MediaState mMediaState;
    private StringeeAudioManager audioManager;
    private boolean isSpeaker = false;
    private boolean isMicOn = true;
    private boolean isVideoOn = true;

    private static final int PICK_IMAGE_REQUEST = 1;

//    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        tvStatus = findViewById(id.tv_status);
        vIncoming = findViewById(id.v_incoming);
        vLocal = findViewById(id.v_local);
        vRemote = findViewById(id.v_remote);
        btnSpeaker = findViewById(id.btn_speaker);
        loadDogImage();


        btnSpeaker.setOnClickListener(view -> {
            runOnUiThread(() -> {
                if (audioManager != null) {
                    audioManager.setSpeakerphoneOn(!isSpeaker);
                    isSpeaker = !isSpeaker;
                    btnSpeaker.setBackgroundResource(isSpeaker ? drawable.btn_speaker_on : drawable.btn_speaker_off);
                }
            });
        });

        btnCapture = findViewById(id.capture_image);
        btnCapture.setOnClickListener(view -> {
            runOnUiThread(() -> {
                showFileChooser();
                uploadImage();
                displayDeepfakeWarning();

            });
        });

        btnSms = findViewById(id.sms);
        btnSms.setOnClickListener(view -> {
            runOnUiThread(() -> {
                // Display "SMS sent" message
                Toast.makeText(getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT).show();
            });
        });


//        buttonUpload = findViewById(id.btn_send);
//        btnCapture.setOnClickListener(view -> {
//            runOnUiThread(() -> {
//                uploadImage();
//            });
//        });




        btnMute = findViewById(id.btn_mute);
        btnMute.setOnClickListener(view -> {
            runOnUiThread(() -> {
                if (call != null) {
                    call.mute(isMicOn);
                    isMicOn = !isMicOn;
                    btnMute.setBackgroundResource(isMicOn ? drawable.btn_mic_on : drawable.btn_mic_off);
                }
            });
        });
        btnSwitch = findViewById(id.btn_switch);
        btnSwitch.setOnClickListener(view -> {
            runOnUiThread(() -> {
                if (call != null) {
                    call.switchCamera(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                }
            });
        });
        btnVideo = findViewById(id.btn_video);
        btnVideo.setOnClickListener(view -> {
            runOnUiThread(() -> {
                if (call != null) {
                    call.enableVideo(!isVideoOn);
                    isVideoOn = !isVideoOn;
                    btnVideo.setBackgroundResource(isVideoOn? drawable.btn_video_on: drawable.btn_video_off);
                }
            });
        });
        btnAnswer = findViewById(id.btn_answer);
        btnAnswer.setOnClickListener(view -> {
            runOnUiThread(() -> {
                if (call != null) {
                    call.answer(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                    vIncoming.setVisibility(View.GONE);
                    btnEnd.setVisibility(View.VISIBLE);
                }
            });
        });
        btnReject = findViewById(id.btn_reject);
        btnReject.setOnClickListener(view -> {
            runOnUiThread(() -> {
                if (call != null) {
                    call.reject(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                    audioManager.stop();
                    finish();
                }
            });
        });
        btnEnd = findViewById(id.btn_end);
        btnEnd.setOnClickListener(view -> {
            if (call != null) {
                call.hangup(new StatusListener() {
                    @Override
                    public void onSuccess() {

                    }
                });
                audioManager.stop();
                finish();
            }
        });

        if (getIntent() != null) {
            isIncomingCall = getIntent().getBooleanExtra("isIncomingCall", false);
            to = getIntent().getStringExtra("to");
            callId = getIntent().getStringExtra("callId");
        }

        vIncoming.setVisibility(isIncomingCall ? View.VISIBLE : View.GONE);
        btnEnd.setVisibility(isIncomingCall ? View.GONE : View.VISIBLE);

        List<String> lstPermission = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            lstPermission.add(permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            lstPermission.add(permission.CAMERA);
        }
        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                lstPermission.add(permission.BLUETOOTH_CONNECT);
            }
        }
        if (lstPermission.size() > 0) {
            String[] permissions = new String[lstPermission.size()];
            for (int i = 0; i < lstPermission.size(); i++) {
                permissions[i] = lstPermission.get(i);
            }
            ActivityCompat.requestPermissions(this, permissions, 0);
            return;
        }

        initCall();
    }

    private void displayDeepfakeWarning() {
        new AlertDialog.Builder(CallActivity.this)
                .setTitle("Warning")
                .setMessage("Warning: The opposite person may not be your relative, please take care of deepfake technology.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    private ImageView imageView;

    private EditText editTextName;

    private Bitmap bitmap;

    private Bitmap selectedBitmap;
    private String UPLOAD_URL ="https://fefc886d-d832-4914-a230-a85be20cb9b9-00-3rmr3sqnunpfa.riker.replit.dev/uploadImage";

    private String KEY_IMAGE = "files";
    private String KEY_NAME = "name";
    private void uploadImage() {
        //Showing the progress dialog

        final ProgressDialog loading = ProgressDialog.show(CallActivity.this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(CallActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                },
                volleyError -> {
                    //Dismissing the progress dialog
                    loading.dismiss();

                    //Showing toast
                    Toast.makeText(CallActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Getting Image Name
                String name = editTextName.getText().toString().trim();

                //Creating parameters
                Map<String, String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void loadDogImage() {

        // getting a new volley request queue for making new requests
        RequestQueue volleyQueue = Volley.newRequestQueue(CallActivity.this);
        // url of the api through which we get random dog images
        String url = "https://fefc886d-d832-4914-a230-a85be20cb9b9-00-3rmr3sqnunpfa.riker.replit.dev/uploadImage";

        // since the response we get from the api is in JSON, we
        // need to use `JsonObjectRequest` for parsing the
        // request response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                // we are using GET HTTP request method
                Request.Method.GET,
                // url we want to send the HTTP request to
                url,
                // this parameter is used to send a JSON object to the
                // server, since this is not required in our case,
                // we are keeping it `null`
                null,

                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String dogImageUrl;
                    try {
                        dogImageUrl = response.getString("relative");
                        // load the image into the ImageView using Glide.
//                        Glide.with(CallActivity.this).load(dogImageUrl).into(mDogImageView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user
                    // that something went wrong
                    Toast.makeText(CallActivity.this, "Some error occurred! Cannot fetch dog image", Toast.LENGTH_LONG).show();
                    // log the error message in the error stream
                    Log.e("MainActivity", "loadDogImage error: ${error.localizedMessage}");
                }
        );

        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isGranted = false;
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                } else {
                    isGranted = true;
                }
            }
        }
        if (requestCode == 0) {
            if (!isGranted) {
                finish();
            } else {
                initCall();
            }
        }
    }

    private void initCall() {
        if (isIncomingCall) {
            call = MainActivity.call2Map.get(callId);
            if (call == null) {
                finish();
                return;
            }
        } else {
            call = new StringeeCall2(MainActivity.client, MainActivity.client.getUserId(), to);
            call.setVideoCall(true);
        }

        call.setCallListener(new StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall2 stringeeCall, StringeeCall2.SignalingState signalingState, String s, int i, String s1) {
                runOnUiThread(() -> {
                    mSignalingState = signalingState;
                    switch (signalingState) {
                        case CALLING:
                            tvStatus.setText("Calling");
                            break;
                        case RINGING:
                            tvStatus.setText("Ringing");
                            break;
                        case ANSWERED:
                            tvStatus.setText("Starting");
                            if (mMediaState == MediaState.CONNECTED) {
                                tvStatus.setText("Started");
                            }
                            break;
                        case BUSY:
                            tvStatus.setText("Busy");
                            audioManager.stop();
                            finish();
                            break;
                        case ENDED:
                            tvStatus.setText("Ended");
                            audioManager.stop();
                            finish();
                            break;
                    }
                });
            }

            @Override
            public void onError(StringeeCall2 stringeeCall, int i, String s) {
                runOnUiThread(() -> {
                    finish();
                    audioManager.stop();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall, StringeeCall2.SignalingState signalingState, String s) {

            }

            @Override
            public void onMediaStateChange(StringeeCall2 stringeeCall, StringeeCall2.MediaState mediaState) {
                runOnUiThread(() -> {
                    mMediaState = mediaState;
                    if (mediaState == MediaState.CONNECTED) {
                        if (mSignalingState == SignalingState.ANSWERED) {
                            tvStatus.setText("Started");
                        }
                    } else {
                        tvStatus.setText("Retry to connect");
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall2 stringeeCall) {
                runOnUiThread(()->{
                    vLocal.removeAllViews();
                    vLocal.addView(stringeeCall.getLocalView());
                    stringeeCall.renderLocalView(true);
                });
            }

            @Override
            public void onRemoteStream(StringeeCall2 stringeeCall) {
                runOnUiThread(()->{
                    vRemote.removeAllViews();
                    vRemote.addView(stringeeCall.getRemoteView());
                    stringeeCall.renderRemoteView(false);
                });
            }

            @Override
            public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onCallInfo(StringeeCall2 stringeeCall, JSONObject jsonObject) {

            }

            @Override
            public void onTrackMediaStateChange(String s, StringeeVideoTrack.MediaType mediaType, boolean b) {

            }
        });

        audioManager = new StringeeAudioManager(this);
        audioManager.start((audioDevice, set) -> {

        });
        audioManager.setSpeakerphoneOn(true);

        if (isIncomingCall) {
            call.ringing(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        } else {
            call.makeCall(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }
    }
}