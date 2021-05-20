package com.example.carl_002;

import android.content.SharedPreferences;
import android.widget.ImageView;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/*
 * The player class keeps track of everything the player character is doing and the methods
 * for the actions it can take
 *
 *
 *
 */

public class playerCharacter {

    public playerCharacter player;
    public GameActivity.worldControl world;
    public ImageView imgPlayer;


    Random rand = new Random();

    int animateCD = 10;
    int MAX_ANIMATE_CD = 8;
    int frame = 2;
    int runningFrame = 2;
    int setFrame = 0;
    int ySpeed = 0;
    int GROUND_PIXEL = 0;
    int RIGHT_PIXEL = 0;
    int jumpPower = -30;
    int GRAVITY = 1;
    int yDirection = 1;
    int DIVE_POWER = 40;
    int jumpsLeft = 0;
    int MAX_JUMPS = 2;
    int WIDTH = 120;
    int HEIGHT = 180;
    int scootTime = 0;
    int SCOOT_POWER = 16;

    int score = 0;
    int level = 1;
    int maxLevel = 1;
    int maxScore = 300;

    int hurtTime = 0;

    int carrots = 0;
    public int carrotTime = 0;
    public int lastCurrentLevel;


    boolean firstCallOver = false;
    boolean flying = false;
    boolean scraming = false;

    SharedPreferences savedData;


    public void movement()
    {
        if(!firstCallOver){firstCall();} // call setup stuff on the first frame then never again

        // slide back if recently hit
        if(hurtTime > 0){
            hurtTime --;
            for(int i=0; i<10; i++) { if (xSpot() > 0) {imgPlayer.setTranslationX(xSpot() - 1); } }
        }

        //slide back slowly if past 1/3 of the screen
        if(xSpot() > world.layScreen.getWidth() / 3){
            for(int i=0; i<4; i++) { if (xSpot() > 0) {imgPlayer.setTranslationX(xSpot() - 1); } }
        }

        //slide forward slowly if not yet at 1/3 of the screen
        if(xSpot() < world.layScreen.getWidth() / 3){
            imgPlayer.setTranslationX(xSpot() + 2);
            scraming = false;
        }

        // move forward quickly if scooting
        if(scootTime > 0){
            scootTime --;
            imgPlayer.setTranslationX(xSpot() + 22);
        }

        // move back quickly if scramming
        if(scraming){
            imgPlayer.setTranslationX(xSpot() - 22);
        }

        ySpeed += GRAVITY; // apply gravity

        if(flying && ySpeed > 2){ySpeed = 2;}

        // set a direction variable so we can move up and down one pixel at a time with a single loop
        if(ySpeed < 0){yDirection = -1;} else {yDirection = 1;}

        // move one step for each point of speed we have, setting everything back to fresh if we hit the ground
        for(int i=0; i<Math.abs(ySpeed); i++){
            imgPlayer.setTranslationY(ySpot() + yDirection);
            if(ySpot() > ground()){
                imgPlayer.setTranslationY(ground());
                if(ySpeed >= 2){world.playSFX(R.raw.land);}
                ySpeed = 0;
                jumpsLeft = MAX_JUMPS;
                imgPlayer.setRotation(0);
                flying = false;
            }
        }


    }

    // onLoad method
    private void firstCall()
    {
        imgPlayer.setImageResource(R.drawable.carl_0001);
        GROUND_PIXEL = Math.round(world.progScore.getY()); // remember where the ground is. this doesn't work if we run it on the first frame but does here
        RIGHT_PIXEL = Math.round(world.progScore.getX() + world.progScore.getWidth());
        firstCallOver = true;
    }

    // this method ic called when the jump button is pressed
    // the player with either flap its ears (fly) , jump, tiny jump, or nothing
    // depending on the player's current state
    public void startJump()
    {
        if(flying && hurtTime < 1){
            world.playSFX(R.raw.miss);
            ySpeed = -3;
            return;
        }

        if(jumpsLeft == 0 && ySpeed >= DIVE_POWER){
            jumpsLeft --;
            ySpeed = -16;
            world.playSFX(R.raw.jump);
            return;
        }

        if(jumpsLeft > 0){
            imgPlayer.setRotation(0);
            jumpsLeft --;

            if(jumpsLeft == 0 && ySpeed >= DIVE_POWER){
                flying = true;
                world.playSFX(R.raw.miss);
            } else {
                ySpeed = jumpPower;
                world.playSFX(R.raw.jump);
            }



        }
    }

    // this method is called when the player presses the scoot / scram / dive button
    // if the player is in the air they will dive, if they are on the ground they will scoot or
    // scram depending on how far across the screen they are
    public void startDive()
    {
        if(ySpot() == ground() && hurtTime < 1 && xSpot() > world.layScreen.getWidth() / 3){
            scootTime = 0;
            world.playSFX(R.raw.spring);
            scraming = true;
            return;
        }
        if(ySpot() == ground() && hurtTime < 1 && xSpot() <= world.layScreen.getWidth() / 3) {
            scootTime = SCOOT_POWER;
            world.playSFX(R.raw.spring);
            return;
        }
        if(ySpot() < ground() && jumpsLeft >= 0) {
            ySpeed = DIVE_POWER;
            flying = false;
        }


    }


    // returns an integer of x location
    public int xSpot()
    {
        return Math.round(imgPlayer.getTranslationX());
    }

    // returns an integer of y location
    public int ySpot()
    {
        return Math.round(imgPlayer.getTranslationY());
    }

    public int ground()
    {
        return GROUND_PIXEL - imgPlayer.getHeight();
    }

    // reduce score and set time variable for knock back when hit, but only if we're not in carrotTime
    public void hurt(int damage)
    {
        if(carrotTime <= 0) {
            world.playSFX(R.raw.hit);
            hurtTime = 20;
            score -= damage;
        } else {
            world.playSFX(R.raw.coin);
        }
        if(score < 0){score = 0;}

    }

    // this method handles picking which sprite of the player to display and status bars, but it also handles score increases
    public void updateAnimation()
    {
        // each tick score will increase, possibly increasing the level as well
        score ++;
        if(score >= maxScore){
            world.playSFX(R.raw.nextstage);
            level ++;
            lastCurrentLevel = level;
            if(level > maxLevel){maxLevel ++;}

            SharedPreferences.Editor editor = savedData.edit();
            editor.putInt("maxLevel", maxLevel);
            editor.putInt("level", level);
            editor.commit();


            score = 0;
            maxScore = (30 * 10) + (30 * 2 * level);
            if(level > 18){maxScore -= (level - 18) * 15;}
            world.setMonsterDice(level); // have the world object to create a new set of monsters for the new level



        }
        // show score as level progress, and make sure the right level# is displayed
        world.progScore.setMax(maxScore);
        world.progScore.setProgress(score);
        world.txtLevel.setText(String.valueOf(level));


        //logic for which frame the player is in at any given time
        //we use two counters: running frame so that we always know which "normal" sprite to use
        //then frame that overwrites that situationally for jumping and stuff
        animateCD --;
        if(carrotTime > 0){ animateCD --;}
        if(flying){ animateCD --;}
        if(animateCD < 1){
            animateCD = MAX_ANIMATE_CD;
            if(runningFrame == 1) {runningFrame = 2; } else {runningFrame = 1; }
        }


        frame = runningFrame;
        if(imgPlayer.getTranslationY() != 0) {
            if(jumpsLeft == 1){frame = 1; imgPlayer.setRotation(imgPlayer.getRotation() + 2);}
            if(jumpsLeft <= 0 && !flying){frame = 3; imgPlayer.setRotation(imgPlayer.getRotation() - 2);}
            if(ySpeed > 39){frame = 4; imgPlayer.setRotation(0);}
        }
        if(flying){
            frame = runningFrame + 5;
        }
        if(hurtTime > 0){frame = 5;}

        // set the chosen frame
        boolean orange = false; // if we're in carrot time display the orange frame 50% of the time
        if(carrotTime > 0 && rand.nextInt(2) == 1){orange = true;}

        if(orange){
            setFrame = frame + 7;
        } else {
            setFrame = frame;
        }


        if(setFrame == 1 ){imgPlayer.setImageResource(R.drawable.carl_0001);}
        if(setFrame == 2 ){imgPlayer.setImageResource(R.drawable.carl_0002);}
        if(setFrame == 3 ){imgPlayer.setImageResource(R.drawable.carl_0003);}
        if(setFrame == 4 ){imgPlayer.setImageResource(R.drawable.carl_0004);}
        if(setFrame == 5 ){imgPlayer.setImageResource(R.drawable.carl_0005);}
        if(setFrame == 6 ){imgPlayer.setImageResource(R.drawable.carl_0006);}
        if(setFrame == 7 ){imgPlayer.setImageResource(R.drawable.carl_0007);}

        if(setFrame == 8 ){imgPlayer.setImageResource(R.drawable.orange_carl_01);}
        if(setFrame == 9 ){imgPlayer.setImageResource(R.drawable.oranger_carl_02);}
        if(setFrame == 10){imgPlayer.setImageResource(R.drawable.orange_carl_03);}
        if(setFrame == 11){imgPlayer.setImageResource(R.drawable.oranger_carl_04);}
        if(setFrame == 12){imgPlayer.setImageResource(R.drawable.orange_carl_05);}
        if(setFrame == 13){imgPlayer.setImageResource(R.drawable.orange_carl_0006);}
        if(setFrame == 14){imgPlayer.setImageResource(R.drawable.orange_carl_0007);}


        // jump button texts
        int buttonText = R.string.btnJump3;
        if(jumpsLeft == 2){buttonText = R.string.btnJump;}
        if(jumpsLeft == 1) {
            if (ySpeed >= DIVE_POWER) {
                buttonText = R.string.btnJump4;
            } else {
                buttonText = R.string.btnJump2;
            }
        }
        if(jumpsLeft == 0){
            if(ySpeed >= DIVE_POWER){
                buttonText = R.string.btnJump6;
            } else {
                buttonText = R.string.btnJump3;
            }
        }
        if(flying && hurtTime < 1){ buttonText = R.string.btnJump5;}
        world.btnOne.setText(buttonText);

        // dive button texts
        buttonText = R.string.btnDive;
        if(ySpot() < ground() && ySpeed < DIVE_POWER && jumpsLeft >= 0){buttonText = R.string.btnDive2;}
        if(ySpot() == ground() && hurtTime < 1 && xSpot() <= world.layScreen.getWidth() / 3){buttonText = R.string.btnDive3;}
        if(ySpot() == ground() && hurtTime < 1 && xSpot() > world.layScreen.getWidth() / 3){buttonText = R.string.btnDive4;}
        world.btnTwo.setText(buttonText);


        //carrot bar display
        if(carrotTime > 0){
            carrotTime --;



            carrots = Math.round( (float) carrotTime / 30);
            if(carrotTime >= 30 * 10){carrots = 10;}
            if(carrotTime == 0){
                carrots = 0;
                world.startMusic();
            }
        }


        if(carrots <= 0){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_00);}
        if(carrots == 1){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_01);}
        if(carrots == 2){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_02);}
        if(carrots == 3){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_03);}
        if(carrots == 4){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_04);}
        if(carrots == 5){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_05);}
        if(carrots == 6){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_06);}
        if(carrots == 7){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_07);}
        if(carrots == 8){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_08);}
        if(carrots == 9){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_09);}
        if(carrots >= 10){world.imgCarrotBar.setImageResource(R.drawable.carrot_bar_full);}


    }










}
