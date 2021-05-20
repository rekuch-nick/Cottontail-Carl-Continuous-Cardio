
package com.example.carl_002;

import android.widget.ImageView;

/*
 * This method handles the powerup carrot objects
 * It'll slide them slowly across the screen while checking to see if they touch the player
 * then remove itself when touched or when it slides too far
 */


public class powerUp {
    public GameActivity.worldControl world;
    public playerCharacter player;
    public ImageView imgSelf;

    public powerUp(GameActivity.worldControl world, playerCharacter player, ImageView imgSelf) {
        this.world = world;
        this.player = player;
        this.imgSelf = imgSelf;

    }

    int slot = 101;
    boolean active = false;
    //Random rand = new Random();
    String type = "";
    int speed = 4;
    boolean hasHit = false;
    int hitRange; // = (int) (Math.round(player.imgPlayer.getWidth()) * .8);





    public void movement()
    {
        hitRange = (int) (Math.round(player.imgPlayer.getWidth()) * .8);

        imgSelf.setTranslationX(xSpot() - speed); // move left

        // save the location of the center of the item for hit testing
        int xPoint = xSpot() + Math.round( (float) imgSelf.getWidth() / 2);
        int yPoint = ySpot() + Math.round( (float) imgSelf.getHeight() / 2);

        // call the method to hit test on the player object
        if(hitPlayer(xPoint, yPoint)){
            world.playSFX(R.raw.pickup);
            if(player.carrotTime > 0){
                player.carrotTime += (30 * 5);

            } else {
                this.player.carrots++;
                if (player.carrots >= 10) {
                    player.carrotTime += 660;
                    world.startMusic();
                }
            }
            hasHit = true;
        }


        // remove the object if its hit the player or moves too far left
        if(xSpot() < -80 || hasHit){
            remove();
        }


    }

    // player hit test method
    private boolean hitPlayer(int x, int y)
    {
        int px = player.xSpot() + (player.imgPlayer.getWidth() / 2);
        int py = player.ySpot() + (player.imgPlayer.getHeight() / 2);

        return (x > px - hitRange && x < px + hitRange && y > py - hitRange && y < py + hitRange);
    }


    public void createImage(int slotNumber, String itemType)
    {

        imgSelf.setImageResource(R.drawable.carrot);

        int xStart = 800;
        int yStart = 450;

        active = true;
        slot = slotNumber;
        type = itemType;

        imgSelf.setTranslationX(xStart);
        imgSelf.setTranslationY(yStart);
    }

    // returns an integer of x location
    public int xSpot()
    {
        return Math.round(imgSelf.getX());
    }

    // returns an integer of y location
    public int ySpot()
    {
        return Math.round(imgSelf.getTranslationY());
    }

    private void remove()
    {
        world.removePowerUp(slot, imgSelf);
    }

}