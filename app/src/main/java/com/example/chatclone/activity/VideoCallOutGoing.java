package com.example.chatclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatclone.R;
import com.example.chatclone.service.ApiClient;
import com.example.chatclone.service.ApiService;
import com.example.chatclone.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoCallOutGoing extends AppCompatActivity {
    String receiverUid, receiverName, profileImage, receiverToken, senderUid, inviterToken;
    FirebaseDatabase database;
    TextView username;
    ImageView imageUser;
    FloatingActionButton decline;
    String meetingRoom = null;
    String meetingType = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_out_going);

        username = findViewById(R.id.usernameReceiver);
        imageUser = findViewById(R.id.imageUser);
        decline = findViewById(R.id.decline);
        database = FirebaseDatabase.getInstance();


        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        senderUid = currentUser.getUid();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            receiverUid = bundle.getString("receiverUid");
            meetingType = bundle.getString("type");
        } else {
            Toast.makeText(this, "Data missing", Toast.LENGTH_SHORT).show();
        }

        if(currentUser != null){
            database.getReference().child("users").child(senderUid).child("token").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    inviterToken = snapshot.getValue(String.class);
                    Log.d("Inviter Token", inviterToken);
                    database.getReference().child("users").child(receiverUid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                receiverName = snapshot.child("name").getValue().toString();
                                profileImage = snapshot.child("profileImage").getValue().toString();
                                receiverToken = snapshot.child("token").getValue().toString();
                                username.setText(receiverName);
                                if(!profileImage.equalsIgnoreCase("No Image")){
                                    Glide.with(VideoCallOutGoing.this).load(profileImage)
                                            .into(imageUser);
                                }
                                initCall(meetingType, receiverToken);
                            } else {
                                Toast.makeText(VideoCallOutGoing.this, "Không thể tạo cuộc gọi", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelInviteResponse(receiverToken);
            }
        });



    }

    private void initCall(String meetingType, String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            data.put("senderUid", senderUid);
            meetingRoom = senderUid + "_" + UUID.randomUUID().toString().substring(0,5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            Toast.makeText(this, "inviter " + inviterToken, Toast.LENGTH_SHORT).show();
            Log.d("TEG", body.toString());
            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);
        } catch (Exception e){
            Toast.makeText(this, "Failed Init Call" + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Toast.makeText(VideoCallOutGoing.this, "Invatation send success", Toast.LENGTH_SHORT).show();
                    } else if(type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(VideoCallOutGoing.this, "Invatation cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(VideoCallOutGoing.this, "Response Failed" + response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call,@NotNull Throwable t) {
                Toast.makeText(VideoCallOutGoing.this, "Failure send remote" + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    private void cancelInviteResponse(String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCEL);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            Log.d("TEGR", body.toString());

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);
        } catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver inviteResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    try {
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(new URL("https://meet.jit.si"));
                        builder.setRoom(meetingRoom);
                        builder.setFeatureFlag("add-people.enabled", false);
                        builder.setFeatureFlag("invite.enabled", false);
                        builder.setFeatureFlag("kick-out.enabled", false);
                        builder.setFeatureFlag("meeting-name.enabled",false);
                        builder.setFeatureFlag("notifications.enabled", false);
                        builder.setFeatureFlag("chat.enabled", false);
                        builder.setFeatureFlag("prejoinpage.enabled", false);
                        builder.setFeatureFlag("overflow-menu.enabled", false);
                        if(meetingType.equals("voice")){
                            builder.setVideoMuted(true);
                        }

                        JitsiMeetActivity.launch(VideoCallOutGoing.this, builder.build());
                        finish();
                    } catch (Exception e){
                        Toast.makeText(VideoCallOutGoing.this, "Response accepted" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                inviteResponseReceiver, new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }


    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                inviteResponseReceiver
        );
    }
}