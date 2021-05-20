package com.example.carl_002;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
    This activity has been replaced with the Menu Fragment
    It is small enough that we don't lose anything by leaving it in for reference
 */


public class MenuActivity extends AppCompatActivity {

    private Button btnBackFromMenu;
    private Button btnSound;
    private boolean soundOn = true;
    private String calledFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        calledFrom = getIntent().getStringExtra("calledFrom");


        btnBackFromMenu = findViewById(R.id.btnBackFromMenu);
        btnBackFromMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calledFrom.equals("title")){
                    startActivity(new Intent(MenuActivity.this, MainActivity.class));
                }
                if(calledFrom.equals("play")){
                    startActivity(new Intent(MenuActivity.this, GameActivity.class));
                }
            }
        });

        btnSound = findViewById(R.id.btnSound);
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(soundOn){
                    soundOn = false;
                    btnSound.setText(R.string.btnSoundOff);
                } else {
                    soundOn = true;
                    btnSound.setText(R.string.btnSoundON);
                }
            }
        });



    } // end of onCreate
}
