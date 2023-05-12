package com.example.chatclone.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatclone.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneNumberActivity extends AppCompatActivity {
    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();



        getSupportActionBar().hide();

        binding.ePhone.requestFocus();

        binding.buttonCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(auth.getCurrentUser() != null){
//                    Intent intent = new Intent(PhoneNumberActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                } else {
                    Intent intent = new Intent(PhoneNumberActivity.this, OPTActivity.class);
                    intent.putExtra("phoneNumber", binding.ePhone.getText().toString());
                    startActivity(intent);
//                }
            }
        });
    }
}