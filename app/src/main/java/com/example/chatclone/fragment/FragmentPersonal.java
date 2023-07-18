package com.example.chatclone.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.chatclone.R;
import com.example.chatclone.activity.PhoneNumberActivity;
import com.example.chatclone.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentPersonal extends Fragment {
    FirebaseAuth auth;
    ImageView avatar;
    TextView username;
    Button logoutBtn;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();

        avatar = view.findViewById(R.id.avatar);
        username = view.findViewById(R.id.username);
        logoutBtn = view.findViewById(R.id.logout);

        database = FirebaseDatabase.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null){
            database.getReference("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if(user != null){
                        username.setText(user.getName());
                        Glide.with(getActivity()).load(user.getProfileImage())
                                .placeholder(R.drawable.icon_user)
                                .error(R.drawable.icon_user)
                                .into(avatar);
                    } else {
                        Toast.makeText(getActivity(), "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Lỗi khi truy vấn dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(getActivity(), "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    public void logout(){
        if(currentUser != null){
            database.getReference().child("users").child(currentUser.getUid())
                    .child("token")
                    .setValue(null);
            auth.signOut();
            Intent intent = new Intent(getActivity(), PhoneNumberActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
