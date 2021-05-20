package com.example.carl_002;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/*
 * This simple activity displays the single page of story text
 * It can only be accessed from the title (main activity) and returns there
 *
 */

public class StoryActivity extends AppCompatActivity {

    private Button btnBackFromStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        btnBackFromStory = findViewById(R.id.btnBackFromStory);
        btnBackFromStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoryActivity.this, MainActivity.class));
            }
        });




    } // end of onCreate
}
