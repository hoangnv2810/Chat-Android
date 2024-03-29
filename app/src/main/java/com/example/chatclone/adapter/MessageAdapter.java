package com.example.chatclone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatclone.R;
import com.example.chatclone.databinding.ItemReceiveBinding;
import com.example.chatclone.databinding.ItemSentBinding;
import com.example.chatclone.model.Message;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter{

    Context context;
    List<Message> messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    String senderRoom;
    String receiverRoom;
    String senderUid;
    String receiverUid;
    String imageReceiver;
    public MessageAdapter(Context context, List<Message> messages, String senderRoom, String receiverRoom, String senderUid, String receiverUid, String imageReceiver) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.imageReceiver = imageReceiver;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiveViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions).build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if (pos < 0) return false;

            if (holder.getClass() == SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }

            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId())
                    .setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId())
                    .setValue(message);

            return true;
        });

        if(holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder) holder;

            if(message.getMessage().equals("[Hình ảnh]")){
                viewHolder.binding.imageView.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.timeSentImage.setVisibility(View.VISIBLE);
                viewHolder.binding.timeSent.setVisibility(View.GONE);
                Glide.with(context).load(message.getImage())
                        .into(viewHolder.binding.imageView);
            }

            viewHolder.binding.message.setText(message.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            viewHolder.binding.timeSent.setText(sdf.format(new Date(message.getTimestamp())));
            viewHolder.binding.timeSentImage.setText(sdf.format(new Date(message.getTimestamp())));


            if(position == getItemCount() - 1){
                FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(receiverRoom)
                        .child("seen").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Boolean seen = snapshot.getValue(Boolean.class);
                                if(seen){
                                    ((SentViewHolder) holder).binding.seen.setVisibility(View.VISIBLE);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } else {
                ((SentViewHolder) holder).binding.seen.setVisibility(View.GONE);
            }

            if (message.getFeeling() >= 0){
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });
        } else {
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
            Glide.with(context).load(imageReceiver)
                    .placeholder(R.drawable.icon_user)
                    .error(R.drawable.icon_user)
                    .into(((ReceiveViewHolder) holder).binding.avatar);

            if(message.getMessage().equals("[Hình ảnh]")){
                viewHolder.binding.imageView.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.timeSentImage.setVisibility(View.VISIBLE);
                viewHolder.binding.timeSent.setVisibility(View.GONE);
                Glide.with(context).load(message.getImage())
                        .into(viewHolder.binding.imageView);
            }

            viewHolder.binding.message.setText(message.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            viewHolder.binding.timeSent.setText(sdf.format(new Date(message.getTimestamp())));
            viewHolder.binding.timeSentImage.setText(sdf.format(new Date(message.getTimestamp())));

            if (message.getFeeling() >= 0){
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding binding;

        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
