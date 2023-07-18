package com.example.chatclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.chatclone.R;
import com.example.chatclone.adapter.UserAdapter;
import com.example.chatclone.databinding.ActivityMainBinding;
import com.example.chatclone.fragment.FragementDiscovery;
import com.example.chatclone.fragment.FragmentAddressBook;
import com.example.chatclone.fragment.FragmentChat;
import com.example.chatclone.fragment.FragmentDiary;
import com.example.chatclone.fragment.FragmentPersonal;
import com.example.chatclone.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRegistrar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    ActivityMainBinding binding;
    FragmentChat fragmentChat;
    FragmentAddressBook fragmentAddressBook;
    FragementDiscovery fragementDiscovery;
    FragmentDiary fragmentDiary;
    FragmentPersonal fragmentPersonal;
    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);

        fragmentChat = new FragmentChat();
        fragmentAddressBook = new FragmentAddressBook();
        fragementDiscovery = new FragementDiscovery();
        fragmentDiary = new FragmentDiary();
        fragmentPersonal = new FragmentPersonal();

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(this);
        binding.bottomNavigationView.setSelectedItemId(R.id.chats);

        checkForBatteryOptimizations();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.chats:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, fragmentChat)
                        .commit();
                return true;
            case R.id.phonebook:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, fragmentAddressBook)
                        .commit();
                return true;
            case R.id.discovery:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, fragementDiscovery)
                        .commit();
                return true;
            case R.id.diary:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, fragmentDiary)
                        .commit();
                return true;
            case R.id.person:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, fragmentPersonal)
                        .commit();
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.addFriend:
                Toast.makeText(this, "Add Friend selected", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void checkForBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if(!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Enabled");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS){
            checkForBatteryOptimizations();
        }
    }
}