package com.example.chatclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoCallInComing extends AppCompatActivity {
    String senderUid, senderName, senderProfileImage, receiverUid;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    TextView username;
    ImageView imageUser;
    FloatingActionButton acceptBtn, declineBtn;
    String meetingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_in_coming);

        username = findViewById(R.id.name);
        imageUser = findViewById(R.id.imageUser);
        acceptBtn = findViewById(R.id.accept);
        declineBtn = findViewById(R.id.decline);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        receiverUid = user.getUid();


        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        if(meetingType != null){
            if(meetingType.equals("video")){
                senderUid = getIntent().getExtras().getString("senderUid");
            } else if(meetingType.equals("voice")){
                senderUid = getIntent().getExtras().getString("senderUid");
            }
        }

        database.getReference("users").child(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    senderName = snapshot.child("name").getValue().toString();
                    senderProfileImage = snapshot.child("profileImage").getValue().toString();

                    username.setText(senderName);
                   if(!senderProfileImage.equalsIgnoreCase("No Image")){
                       Glide.with(VideoCallInComing.this).load(senderProfileImage)
                               .into(imageUser);
                   }

                } else {
                    Toast.makeText(VideoCallInComing.this, "Không thể tạo cuộc gọi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInviteResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInviteResponse(Constants.REMOTE_MSG_INVITATION_REJECTED,
                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));

            }
        });

    }

    //-----------------------------------------------------------------------------------------------

    private void sendInviteResponse(String type, String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            Log.d("RESRES", body.toString());
            sendRemoteMessage(body.toString(), type);
        } catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                        Toast.makeText(VideoCallInComing.this, "Responce accepted", Toast.LENGTH_SHORT).show();
                        try {
                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(new URL("https://meet.jit.si"));
                            builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));
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
                            JitsiMeetActivity.launch(VideoCallInComing.this, builder.build());
                            finish();
                        } catch (Exception e){
                            Toast.makeText(VideoCallInComing.this, "Failed res" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                        Toast.makeText(VideoCallInComing.this, "Response rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(VideoCallInComing.this, "Response Failed" + response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                Toast.makeText(VideoCallInComing.this, "Failure send remote" + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private BroadcastReceiver inviteResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_CANCEL)){
                    Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
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