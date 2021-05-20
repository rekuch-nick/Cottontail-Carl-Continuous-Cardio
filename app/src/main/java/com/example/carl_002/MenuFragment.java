package com.example.carl_002;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

/*
 * This fragment is the user menu
 *
 * it can be called from both the main activity and the game activity
 *
 *
 */


public class MenuFragment extends Fragment {

    private Button btnBackFromMenu;
    private Button btnSound;
    public Settings userSettings;
    public boolean soundOn;
    public boolean sentFromTitle;
    public MenuFragment myName;
    public GameActivity.worldControl world;
    public int level;
    public int maxLevel;
    private Button btnLevelDown;
    private Button btnLevelUp;
    private TextView txtLevel;

    SharedPreferences saveData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View thisFrag = inflater.inflate(R.layout.fragment_menu, container, false);

        saveData = this.getActivity().getSharedPreferences("SavedData", MODE_PRIVATE);
        level = saveData.getInt("level", 1);
        maxLevel = saveData.getInt("maxLevel", 1);
        txtLevel = thisFrag.findViewById(R.id.txtLevel);
        txtLevel.setText(level + " / " + maxLevel);


        btnLevelDown = thisFrag.findViewById(R.id.btnLevelDown);
        btnLevelDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level --;
                if(level < 1){level = maxLevel;}
                SharedPreferences.Editor editor = saveData.edit();
                editor.putInt("level", level); editor.commit();


                txtLevel.setText(level + " / " + maxLevel);
            }
        });

        btnLevelUp = thisFrag.findViewById(R.id.btnLevelUp);
        btnLevelUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level ++;
                if(level > maxLevel){level = 1;}
                SharedPreferences.Editor editor = saveData.edit();
                editor.putInt("level", level); editor.commit();
                txtLevel.setText(level + " / " + maxLevel);
            }
        });


        btnBackFromMenu = thisFrag.findViewById(R.id.btnBackFromMenu);
        btnBackFromMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userSettings.setMenuPause(false);

                if(world != null){
                    world.btnOptions.setVisibility(View.VISIBLE);
                    world.btnOne.setVisibility(View.VISIBLE);
                    world.btnTwo.setVisibility(View.VISIBLE);

                    world.player.level = level;

                    if(world.player.lastCurrentLevel != level){ world.setMonsterDice(level); }

                    world.startMusic();

                }

                getActivity().getSupportFragmentManager().beginTransaction().remove(myName).commit();

            }
        });


        btnSound = thisFrag.findViewById(R.id.btnSound);
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundOn = userSettings.soundOn;
                soundOn = !soundOn;
                userSettings.soundOn = soundOn;
                setButtonText(btnSound);
            }
        });


        setButtonText(btnSound);

        return thisFrag;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void setButtonText(Button btnSound)
    {
        if(userSettings.soundOn){
            btnSound.setText(R.string.btnSoundOff);
        } else {
            btnSound.setText(R.string.btnSoundON);
        }
    }



}
