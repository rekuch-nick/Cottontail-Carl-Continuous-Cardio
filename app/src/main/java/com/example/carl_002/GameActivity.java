package com.example.carl_002;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

/*
 * This activity is our largest code file:
 *
 * it contains the world class, which implements game rules on the game objects
 *
 * it contains the game loop which executes about 30 times a second and updates the state
 *
 * it contains definitions of the buttons and listeners by which the user interacts with the game
 *
 */


public class GameActivity extends AppCompatActivity {

    private Button btnOne;
    private Button btnTwo;
    private ImageButton btnOptions;
    public worldControl world;
    public playerCharacter player;
    private ConstraintLayout layGameFrags;
    public SharedPreferences savedData;
    public boolean soundOn;
    public Settings userSettings;
    public boolean tabPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        layGameFrags = findViewById(R.id.layGameFrags);

        soundOn = getIntent().getBooleanExtra("sound", true);
        userSettings = new Settings();
        userSettings.soundOn = soundOn;

        // create a player and world object: player is the user's character, world is applies the rules of the game to other objects
        world = new worldControl();
        player = new playerCharacter();
        world.setUpMusic();

        // create our save object
        savedData = getSharedPreferences("SavedData", MODE_PRIVATE);
        player.savedData = savedData;

        // tell world and player how to talk to each other
        world.player = player;
        player.world = world;

        // setup the player's sprite
        ImageView makePlayerSprite = new ImageView(GameActivity.this);
        world.layScreen.addView(makePlayerSprite);
        player.imgPlayer = makePlayerSprite;

        tabPause = false;

        loadGame();
        world.startMusic();


        // create listener objects for the buttons and call their methods when first touched
        // i decided to use on touch listeners rather than on click listeners to make the jump
        // begin slightly faster creating a more responsive feel
        btnOne = findViewById(R.id.btnOne);
        btnOne.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    player.startJump();
                }
                return false;
            }

        });

        btnTwo = findViewById(R.id.btnTwo);
        btnTwo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    player.startDive();
                }
                return false;
            }
        });

        btnOptions = findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.lastCurrentLevel = player.level;
                world.playSFX(R.raw.pause);

                // code to open the menu fragment and pass it values
                FragmentManager fm = getSupportFragmentManager();
                MenuFragment NewMenu = new MenuFragment();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.layGameFrags, NewMenu, "menu");
                NewMenu.userSettings = userSettings;
                NewMenu.soundOn = soundOn;
                NewMenu.myName = NewMenu;
                NewMenu.world = world;
                //NewMenu.setButtonText();
                layGameFrags.bringToFront();
                ft.commit();

                btnOptions.setVisibility(View.INVISIBLE);
                btnOne.setVisibility(View.INVISIBLE);
                btnTwo.setVisibility(View.INVISIBLE);

                userSettings.setMenuPause(true);
                world.startMusic();
            }
        }); /// end of all the listeners





        //
        // This is the game loop. It will execute about 30 times a second as long as the program is running
        //
        // I chose to run everything on a single thread for two reasons:
        // 1) it's slightly easier on the processor
        // 2) this lets me control the exact order everything happens in. there's nothing in this game
        // where that truly matters, but it could: for example, the game is SLIGHTLY easier if we
        // test to see if the user has touched a carrot (possibly turning invincible) before testing
        // to see they've touched a monster- if those were are different threads they might be checked
        // in either order
        //
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 33);
                if(!userSettings.menuPause && !tabPause) {
                    player.movement();
                    world.moveStuff();
                    world.spawn();
                    player.updateAnimation();
                }
            }
        }, 33);  //end of game loop
        //
        //
        //


    } // end of onCreate ///////////////////////////////////////////////////////////////////////////////////////////////////


    // code to save when the app loses focus
    @Override
    public void onPause() {

        SharedPreferences.Editor editor = savedData.edit();
        editor.putInt("level", player.level);
        editor.putInt("maxLevel", player.maxLevel);
        editor.putBoolean("sound", userSettings.soundOn);
        editor.putInt("score", player.score);
        editor.putInt("maxScore", player.maxScore);
        editor.putInt("carrots", player.carrots);

        editor.commit();
        world.music.pause();
        tabPause = true;
        super.onPause();
    } // end of on pause

    // code to load data when the app receives focus
    @Override
    public void onResume() {
        super.onResume();
        tabPause = false;
        loadGame();
        world.startMusic();

    }

    // method that loads data
    private void loadGame() {
        player.level = savedData.getInt("level", 1);
        player.maxLevel = savedData.getInt("maxLevel", 1);
        player.score = savedData.getInt("score", 0);
        player.maxScore = savedData.getInt("maxScore", 360);
        userSettings.soundOn = savedData.getBoolean("sound", true);
        player.carrots = savedData.getInt("carrots", 0);

        world.setMonsterDice(player.level);
    }


    // the class for our world object
    // this is the place we'll house our game rules like what monsters spawn when and remember our other objects
    public class worldControl
    {
        // we'll want to define all of our widgets here so that other classes can talk to them through the world object
        ConstraintLayout layScreen = findViewById(R.id.layScreen);
        private ImageView imgMill = findViewById(R.id.imgMill);
        public Button btnOne = findViewById(R.id.btnOne);
        public Button btnTwo = findViewById(R.id.btnTwo);
        public ImageButton btnOptions = findViewById(R.id.btnOptions);
        public ProgressBar progScore = findViewById(R.id.progScore);
        public worldControl world = this;
        public playerCharacter player;
        TextView txtTest = findViewById(R.id.txtTest);
        public TextView txtLevel = findViewById(R.id.txtLevel);
        public ImageView imgCarrotBar = findViewById(R.id.imgCarrotBar);
        public ImageView imgWindow = findViewById(R.id.imgWindow);


        // be able to generate random numbers
        Random rand = new Random();

        // use this variable for neylw created images until they're stored in their object
        private ImageView newImageView;

        // to self police possible performance issues, we're using a hard cap on the total number of monster objects
        // they'll be remembered in this array, and arrays get accessed faster anyways
        private badGuy[] badGuys = new badGuy[100];
        private int badGuySlot = 0;
        private int badGuysActive = 0;
        //if we use an arraylist instead , we can't talk to the object by its index number without addingan
        // adding an additional variable to the monster object
        // meaning we have to assign it an id number

        // track powerup objects
        private powerUp[] powerUps = new powerUp[100];
        private int powerUpSlot = 0;
        private int powerUpsActive = 0;

        // track particle effects
        private particleEffect[] effects = new particleEffect[100];
        private int effectSlot = 0;
        private int effectsActive = 0;

        // powerup CDs
        private int carrotCD = 5;
        private int MAX_CARROT_CD = 5;

        // timer variables, each second is *about* 30 ticks long
        private int eventCD = 10;
        private int maxEventCD = 30 * 3;
        private int groundCD = 1;
        private int maxGroundCD = 5;
        private int groundFrame = 1;

        // monsters that spawn, chosen randomly (changed each stage)
        private String[] monsterDice = {"fireball", "fireball", "fireball", "eye", "eye", "eye"};

        // monster creation method runs every tick, only makes a new monster if the CD is up
        private void spawn()
        {
            eventCD --;
            if(eventCD < 1){
                eventCD = maxEventCD;

                // roll a random monster from our options
                int r = rand.nextInt(6);
                String monsterType = monsterDice[r];

                makeMonsterObject(monsterType, false, 0, 0);

                // possibly spawn a carrot
                // 50% chance to increment the counter down from 5 ~~ 1 in 10
                if(rand.nextInt(2) == 1){carrotCD --;}
                if(player.level >= 10 && rand.nextInt(3) == 1){carrotCD --;}
                if(player.level >= 20 && rand.nextInt(4) == 1){carrotCD --;}
                //carrotCD = 0; //// use to spawn carrots constantly for testing. comment out.
                if(carrotCD < 1){
                    carrotCD = MAX_CARROT_CD;
                    spawnItem();
                }
            }
        }

        public void makeMonsterObject(String monsterType, boolean setStartPosition,int x, int y)
        {
            if(badGuysActive < 100) { // do not create a new badGuy object unless we have less than X. change this magic number to a constant at some point.


                //create new monster object and imageView
                ImageView newImageView = new ImageView(GameActivity.this);
                layScreen.addView(newImageView);
                badGuy s = new badGuy(world, player, newImageView);
                s.createImage(badGuySlot, monsterType, setStartPosition, x, y);
                badGuys[badGuySlot] = s; // store newly created badGuy object in an array here
                badGuysActive++; //keep track of total number of monsters at any given time

                // to pick the next empty array slot, iterate through until we find an empty one
                // this will break if we have too many (which shouldn't be possible with the if we're nested in)
                do {
                    badGuySlot++;
                    if (badGuySlot > 99) {
                        badGuySlot = 0;
                    }
                } while (badGuys[badGuySlot] != null);
            }
        }

        //powerup creation method, only called when it is time to make a powerup
        private void spawnItem()
        {
            //create new powerup object and imageView
            newImageView = new ImageView(GameActivity.this);
            layScreen.addView(newImageView);
            powerUp s = new powerUp(world, player, newImageView);
            s.createImage(powerUpSlot, "carrot");
            powerUps[powerUpSlot] = s; // store newly created badGuy object in an array here
            powerUpsActive ++; // keep track of total number of powerup objects

            // to pick the next empty array slot, iterate through until we find an empty one
            // this will break if we have too many, but it shouldn't ever happen
            do {
                powerUpSlot ++;
                if(powerUpSlot > 99) {
                    powerUpSlot = 0;
                }
            } while (powerUps[powerUpSlot] != null);
        }

        public void spawnParticleEffect(String type, int x, int y)
        {
            if(effectsActive < 99) { // sanity check, we could easily have too many effectParticles
                newImageView = new ImageView(GameActivity.this);
                layScreen.addView(newImageView);
                particleEffect s = new particleEffect(world, player, newImageView);
                s.createImage(effectSlot, type, x, y);
                effects[effectSlot] = s;
                effectsActive++;

                do {
                    effectSlot++;
                    if (effectSlot > 99) {
                        effectSlot = 0;
                    }
                } while (effects[effectSlot] != null);
            }
        }

        // call each active object's move method
        private void moveStuff()
        {
            for (int i=0; i < 100; i++){
                if(badGuys[i] != null){
                    if(badGuys[i].active) {
                        badGuys[i].movement();
                    }
                }

                if(powerUps[i] != null){
                    if(powerUps[i].active) {
                        powerUps[i].movement();
                    }
                }

                if(effects[i] != null){
                    if(effects[i].active){
                        effects[i].movement();
                    }
                }
            }

            // dynamic control of the treadmill lets us change its speed during carrot time
            if(player.carrotTime > 0){groundCD --;}
            groundCD --; if(groundCD < 1){
                groundCD = maxGroundCD;
                if(groundFrame == 1) {
                    groundFrame = 2;
                    imgMill.setImageResource(R.drawable.mill_02);
                } else {
                    groundFrame = 1;
                    imgMill.setImageResource(R.drawable.mill_01);
                }
            }
        }

        // remove monster image then clear its spot in the array and reduce the count of active monsters by 1
        public void removeBadGuy(int n, ImageView i)
        {
            layScreen.removeView(i);
            badGuys[n].active = false;
            badGuys[n] = null;
            badGuysActive --;
        }

        // remove powerup image then clear its spot in the array and reduce the count of active powerups by 1
        public void removePowerUp(int n, ImageView i)
        {
            layScreen.removeView(i);
            powerUps[n].active = false;
            powerUps[n] = null;
            powerUpsActive --;
        }

        // remove particle effect image then clear its spot in the array and reduce the count of active effects by 1
        public void removeParticleEffect(int n, ImageView i)
        {
            layScreen.removeView(i);
            effects[n].active = false;
            effects[n] = null;
            effectsActive --;
        }

        // method to play sound effects
        public void playSFX(int soundNumber) {
            if (userSettings.soundOn) {
                final MediaPlayer sound = MediaPlayer.create(GameActivity.this, soundNumber);
                sound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.reset();
                        mp.release();
                    }
                });
                sound.start();
            }
        }

        // method to create the music object
        final MediaPlayer music = MediaPlayer.create(GameActivity.this, R.raw.mainmusic);
        private void setUpMusic()
        {
            music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    startMusic();
                }
            });

            music.start();
            music.pause();

        }

        // method to play music
        public void startMusic() {

            if (userSettings.soundOn && !userSettings.menuPause) {
                music.setVolume(.5f, .5f);
                music.start();
            } else {
                music.pause();
            }

        }


        // code for setting the possible monsters and backgrounds on each level
        // scripted for a while then random at high levels
        public void setMonsterDice(int level)
        {

            ///set window image
            if(level ==  2){world.imgWindow.setImageResource(R.drawable.window_02);}
            if(level ==  3){world.imgWindow.setImageResource(R.drawable.window_03);}
            if(level ==  4){world.imgWindow.setImageResource(R.drawable.window_04);}
            if(level ==  5){world.imgWindow.setImageResource(R.drawable.window_05);}
            if(level ==  6){world.imgWindow.setImageResource(R.drawable.window_06);}
            if(level ==  7){world.imgWindow.setImageResource(R.drawable.window_07);}
            if(level ==  8){world.imgWindow.setImageResource(R.drawable.window_08);}
            if(level ==  9){world.imgWindow.setImageResource(R.drawable.window_09);}
            if(level == 10){world.imgWindow.setImageResource(R.drawable.window_10);}
            if(level == 11){world.imgWindow.setImageResource(R.drawable.window_11);}
            if(level == 12){world.imgWindow.setImageResource(R.drawable.window_12);}
            if(level == 13){world.imgWindow.setImageResource(R.drawable.window_13);}
            if(level == 14){world.imgWindow.setImageResource(R.drawable.window_14);}
            if(level == 15){world.imgWindow.setImageResource(R.drawable.window_15);}
            if(level == 16){world.imgWindow.setImageResource(R.drawable.window_16);}
            if(level == 17){world.imgWindow.setImageResource(R.drawable.window_17);}
            if(level == 18){world.imgWindow.setImageResource(R.drawable.window_18);}
            if(level == 19){world.imgWindow.setImageResource(R.drawable.window_19);}
            if(level == 20){world.imgWindow.setImageResource(R.drawable.window_20);}
            if(level == 21){world.imgWindow.setImageResource(R.drawable.window_21);}
            if(level == 22){world.imgWindow.setImageResource(R.drawable.window_22);}
            if(level == 23){world.imgWindow.setImageResource(R.drawable.window_23);}
            if(level == 24){world.imgWindow.setImageResource(R.drawable.window_24);}
            if(level == 25){world.imgWindow.setImageResource(R.drawable.window_25);}
            if(level == 26){world.imgWindow.setImageResource(R.drawable.window_26);}
            if(level >= 27){world.imgWindow.setImageResource(R.drawable.window_27);}



            maxEventCD = 90;
            switch (level){
                case 1:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "fireball";
                    monsterDice[3] = "eye"; monsterDice[4] = "eye"; monsterDice[5] = "eye";
                    break;
                case 2:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "fireball";
                    monsterDice[3] = "fireball"; monsterDice[4] = "ninja"; monsterDice[5] = "ninja";
                    break;
                case 3:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "fireball";
                    monsterDice[3] = "fireball"; monsterDice[4] = "bomb"; monsterDice[5] = "bomb";
                    break;
                case 4:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "fireball";
                    monsterDice[3] = "firewave"; monsterDice[4] = "firewave"; monsterDice[5] = "firewave";
                    break;
                case 5:
                    monsterDice[0] = "eye"; monsterDice[1] = "eye"; monsterDice[2] = "eye";
                    monsterDice[3] = "eye"; monsterDice[4] = "firewave"; monsterDice[5] = "firewave";
                    break;
                case 6:
                    monsterDice[0] = "fireball"; monsterDice[1] = "goblin"; monsterDice[2] = "goblin";
                    monsterDice[3] = "goblin"; monsterDice[4] = "goblin"; monsterDice[5] = "firewave";
                    break;
                case 7:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "fireball";
                    monsterDice[3] = "bird"; monsterDice[4] = "bird"; monsterDice[5] = "fireball";
                    break;
                case 8:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "fireball";
                    monsterDice[3] = "eye"; monsterDice[4] = "ninja"; monsterDice[5] = "bird";
                    break;
                case 9:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "bomb";
                    monsterDice[3] = "fireball"; monsterDice[4] = "fireball"; monsterDice[5] = "fireman";
                    break;
                case 10:
                    monsterDice[0] = "fireball"; monsterDice[1] = "fireball"; monsterDice[2] = "fireball";
                    monsterDice[3] = "firewave"; monsterDice[4] = "goblin"; monsterDice[5] = "cloud";
                    break;


                default:
                    maxEventCD = (30 * 3) - ((level - 9) * 3);
                    if(maxEventCD < 26){maxEventCD = 26;}

                    if(level < 22){
                        String t = "fireball";
                        for(int i=0; i<6; i++){
                            int r = rand.nextInt(8);
                            if(r == 1){t = "ninja";}
                            if(r == 2){t = "eye";}
                            if(r == 3){t = "bomb";}
                            if(r == 4){t = "firewave";}
                            if(r == 5){t = "goblin";}
                            if(r == 6){t = "bird";}
                            monsterDice[i] = t;
                        }

                        int r = rand.nextInt(5);
                        if(r == 1){t = "fireman";}
                        if(r == 2 && rand.nextInt(2) == 1){t = "cloud";}
                        if(r == 3){t = "goblin";}
                        monsterDice[5] = t;



                    } else {

                        String t = "fireball";
                        for(int i=0; i<6; i++){
                            int r = rand.nextInt(9);
                            if(r == 1){t = "ninja";}
                            if(r == 2){t = "eye";}
                            if(r == 3){t = "bomb";}
                            if(r == 4){t = "firewave";}
                            if(r == 5){t = "goblin";}
                            if(r == 6){t = "bird";}
                            if(r == 7){t = "fireman";}
                            if(r == 8){t = "cloud";}

                            monsterDice[i] = t;
                        }

                    }
                    break;
            }
        }

    } // end of worldControl /////////////////////////////////////////////////////////////////////////////////////////////


}
