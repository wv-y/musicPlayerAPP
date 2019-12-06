package com.example.musicplayerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.musicplayerapp.fragment.MainFragment;
import com.example.musicplayerapp.fragment.MyFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Fragment fragmentMain;
    private Fragment fragmentPlay;
    private Button buttonAllSong;
    private Button buttonMySong;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_VOICEMAIL};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);



        fragmentMain = new MainFragment();
        fragmentPlay = new MyFragment();
        //buttonAllSong = (Button) findViewById(R.id.song_all);
        //buttonMySong = (Button) findViewById(R.id.song_my);

//        buttonAllSong.setOnClickListener(this);
  //      buttonMySong.setOnClickListener(this);
        replaceFragment(fragmentMain);
    }



    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }



    /**
     * 替换碎片
     *
     * @param fragment
     */
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment, fragment);
        fragmentTransaction.commit();
    }

    /**
     * 点击事件
     *
     * @param view
     */
   @Override
    public void onClick(View view) {
        /*if (view == buttonAllSong) {
            buttonMySong.setTextSize(20);
            buttonMySong.setBackgroundColor(Color.parseColor("#ffffff"));
            buttonAllSong.setTextSize(25);
            buttonAllSong.setBackgroundColor(Color.parseColor(("#FFDEAD")));
            replaceFragment(fragmentMain);
        }
        if (view == buttonMySong) {
            buttonAllSong.setTextSize(20);
            buttonAllSong.setBackgroundColor(Color.parseColor("#ffffff"));
            buttonMySong.setTextSize(25);
            buttonMySong.setBackgroundColor(Color.parseColor("#FFDEAD"));
            replaceFragment(fragmentPlay);
        }*/
    }
}
