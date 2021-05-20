package com.example.carl_002;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
 * This is the first activity. From it the user can access the settings and start the game.
 *
 * Lessons Learned: I have a lot of variables jumping around and lots to pass to all of
 * our classes: on my next project I want to try putting everything that's saved as a
 * primitive type in a series of shared preferences objects since they're so easy to access
 * from any point in the program.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnGotoStory, btnGotoOptions, btnGotoPlay;
    private SharedPreferences savedData;
    private boolean soundOn, menuPause;
    private Settings userSettings;
    private SharedPreferences getSavedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        soundOn = true;
        menuPause = false;
        userSettings = new Settings(soundOn, menuPause);

        savedData = getSharedPreferences("SavedData", MODE_PRIVATE);


        btnGotoStory = findViewById(R.id.btnGotoStory);
        btnGotoStory.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!userSettings.menuPause) {
                    Intent i = new Intent(MainActivity.this, StoryActivity.class);
                    startActivity(i);
                }

            }
        });

        btnGotoOptions = findViewById(R.id.btnGotoOptions);
        btnGotoOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userSettings.menuPause) {
                    userSettings.menuPause = true;


                    // open the menu fragment and send it information
                    FragmentManager fm = getSupportFragmentManager();
                    MenuFragment NewMenu = new MenuFragment();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.add(R.id.layMainFrags, NewMenu, "menu");
                    NewMenu.userSettings = userSettings;
                    NewMenu.soundOn = soundOn;
                    NewMenu.sentFromTitle = true;
                    NewMenu.myName = NewMenu;
                    NewMenu.maxLevel = savedData.getInt("maxLevel", 1);
                    NewMenu.level = savedData.getInt("level", 1);
                    ft.commit();
                }
            }
        });

        btnGotoPlay = findViewById(R.id.btnGotoPlay);
        btnGotoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userSettings.menuPause) {
                    Intent i = new Intent(MainActivity.this, GameActivity.class);
                    i.putExtra("sound", userSettings.soundOn);
                    startActivity(i);
                }
            }
        });




    } // end of onCreate



}
