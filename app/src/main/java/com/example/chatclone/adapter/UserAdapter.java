package com.example.chatclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatclone.R;
import com.example.chatclone.activity.ChatActivity;
import com.example.chatclone.databinding.ItemConverationBinding;
import com.example.chatclone.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    Context context;
    List<User> users;
    int row_index = -1;
    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_converation, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String lastMess = snapshot.child("lastMess").getValue(String.class);
                            long lastTime = snapshot.child("lastMessTime").getValue(Long.class);

                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                            holder.binding.lastMess.setText(lastMess);
                            holder.binding.msgTime.setText(sdf.format(lastTime));

                        } else {
                            holder.binding.lastMess.setText("Chat với bạn bè");
                            holder.binding.msgTime.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        holder.binding.username.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.icon_user)
                .error(R.drawable.icon_user)
                .into(holder.binding.avatar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                row_index = position;
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", user.getName());
                intent.putExtra("uid", user.getUid());
                intent.putExtra("image", user.getProfileImage());
                intent.putExtra("token", user.getToken());
                context.startActivity(intent);
                notifyDataSetChanged();
            }
        });
        if(row_index == position){
            holder.itemView.setBackgroundColor(Color.parseColor("#f3f4f6"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        ItemConverationBinding binding;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemConverationBinding.bind(itemView);

        }
    }
}
