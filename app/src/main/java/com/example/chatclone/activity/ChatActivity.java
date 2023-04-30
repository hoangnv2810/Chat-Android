package com.example.chatclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.chatclone.adapter.MessageAdapter;
import com.example.chatclone.databinding.ActivityChatBinding;
import com.example.chatclone.model.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessageAdapter messageAdapter;
    List<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;
        database = FirebaseDatabase.getInstance();

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages, senderRoom, receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(messageAdapter);
        binding.recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                binding.recyclerView.scrollToPosition(binding.recyclerView.getAdapter().getItemCount() - 1);
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
                    binding.send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String messageTxt = binding.messageBox.getText().toString();

                            String key = database.getReference().push().getKey();

                            Date date = new Date();
                            Message message = new Message(messageTxt, senderUid, date.getTime());
                            binding.messageBox.setText("");
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

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}