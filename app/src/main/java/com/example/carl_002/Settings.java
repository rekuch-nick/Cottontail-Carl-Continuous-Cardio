package com.example.carl_002;


import android.support.v7.app.AppCompatActivity;

/*
 * This simple class tracks user settings preferences
 * in hindsight it would have been better to just us a sharedpreferences object with other player
 * information. Live and learn!
 *
 */

public class Settings extends AppCompatActivity {
    public boolean soundOn;
    public boolean menuPause;

    public Settings(boolean soundOn, boolean menuPause) {
        this.soundOn = soundOn;
        this.menuPause = menuPause;
    }

    public Settings() {

    }

    public void setMenuPause(boolean b)
    {
        menuPause = b;
    }
}


