package com.example.chatclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.UriMatcher;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.chatclone.R;
import com.example.chatclone.adapter.MessageAdapter;
import com.example.chatclone.databinding.ActivityChatBinding;
import com.example.chatclone.model.Message;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessageAdapter messageAdapter;
    List<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    String receiverUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Đang tải");
        dialog.setCancelable(false);

        String name = getIntent().getStringExtra("name");
        binding.name.setText(name);
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages, senderRoom, receiverRoom, senderUid, receiverRoom, imageReceiver);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(messageAdapter);
        binding.recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                binding.recyclerView.scrollToPosition(binding.recyclerView.getAdapter().getItemCount() - 1);
            }
        });

        database.getReference().child("status").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    binding.status.setText(status);
                } else {
                    binding.status.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            Message message = ds.getValue(Message.class);
                            message.setMessageId(ds.getKey());
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        binding.recyclerView.scrollToPosition(messages.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > 0) {
                    binding.more.setVisibility(View.GONE);
                    binding.image.setVisibility(View.GONE);

                    binding.send.setVisibility(View.VISIBLE);
                    binding.messageBox.setTextColor(Color.BLACK);
                    binding.send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String messageTxt = binding.messageBox.getText().toString();

                            String key = database.getReference().push().getKey();

                            Date date = new Date();
                            Message message = new Message(messageTxt, senderUid, date.getTime());
                            binding.messageBox.setText("");

                            HashMap<String, Object> lastMess = new HashMap<>();
                            lastMess.put("lastMess", message.getMessage());
                            lastMess.put("lastMessTime", message.getTimestamp());

                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMess);
                            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMess);


                            database.getReference().child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(key)
                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("chats")
                                                    .child(receiverRoom)
                                                    .child("messages")
                                                    .child(key)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                        }
                                                    });

                                            HashMap<String, Object> lastMess = new HashMap<>();
                                            lastMess.put("lastMess", message.getMessage());
                                            lastMess.put("lastMessTime", message.getTimestamp());

                                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMess);
                                            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMess);


                                        }
                                    });
                        }
                    });

                } else {
                    binding.more.setVisibility(View.VISIBLE);
                    binding.image.setVisibility(View.VISIBLE);

                    binding.send.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });
            getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("status").child(FirebaseAuth.getInstance().getUid()).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("status").child(FirebaseAuth.getInstance().getUid()).setValue("Offline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25){
            if(data != null){
                if(data.getData() != null){
                    Uri img = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(img).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String path = uri.toString();
                                        String messageTxt = binding.messageBox.getText().toString();

                                        String key = database.getReference().push().getKey();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setMessage("[Hình ảnh]");
                                        message.setImage(path);
                                        binding.messageBox.setText("");

                                        HashMap<String, Object> lastMess = new HashMap<>();
                                        lastMess.put("lastMess", message.getMessage());
                                        lastMess.put("lastMessTime", message.getTimestamp());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMess);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMess);


                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(key)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        database.getReference().child("chats")
                                                                .child(receiverRoom)
                                                                .child("messages")
                                                                .child(key)
                                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                    }
                                                                });

                                                        HashMap<String, Object> lastMess = new HashMap<>();
                                                        lastMess.put("lastMess", message.getMessage());
                                                        lastMess.put("lastMessTime", message.getTimestamp());

                                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMess);
                                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMess);


                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}