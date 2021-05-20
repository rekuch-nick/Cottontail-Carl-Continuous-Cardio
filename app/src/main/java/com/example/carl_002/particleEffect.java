package com.example.carl_002;

import android.widget.ImageView;

import java.util.Random;

/*
 * This class will be used for each of the particle effects
 *
 * it will move them across the screen and remove them when time is out
 */

public class particleEffect {

    public GameActivity.worldControl world;
    public playerCharacter player;
    public ImageView imgSelf;

    public particleEffect(GameActivity.worldControl world, playerCharacter player, ImageView imgSelf) {
        this.world = world;
        this.player = player;
        this.imgSelf = imgSelf;

    }

    int slot = 101;
    int timeRemaining = 30 * 6;
    boolean active = false;
    String type = "";
    int xSpeed = 0;
    int ySpeed = 0;
    int gravity = 0;
    int frameFrist;
    int frameSecond;

    Random rand = new Random();

    public void createImage(int slotNumber, String effectType, int xStart, int yStart)
    {

        frameFrist = R.drawable.spark_01;
        frameSecond = R.drawable.spark_02;


        xSpeed = rand.nextInt(5) + 3;
        if(rand.nextInt(2)==1){xSpeed *= -1;}

        ySpeed = rand.nextInt(13) + 7;
        ySpeed *= -1;

        gravity = 1;

        imgSelf.setImageResource(frameFrist);

        active = true;
        slot = slotNumber;
        type = effectType;

        imgSelf.setTranslationX(xStart);
        imgSelf.setTranslationY(yStart);
    }

    public void movement()
    {
        imgSelf.setTranslationX(imgSelf.getTranslationX() + xSpeed);

        ySpeed += gravity;
        imgSelf.setTranslationY(imgSelf.getTranslationY() + ySpeed);



        if(timeRemaining % 2 == 0){
            imgSelf.setImageResource(frameFrist);
        } else {
            imgSelf.setImageResource(frameSecond);
        }

        timeRemaining --;
        if(timeRemaining < 1){
            remove();
        }
    }

    private void remove()
    {
        world.removeParticleEffect(slot, imgSelf);
    }

}
