package com.example.carl_002;

import android.widget.ImageView;

import java.util.Random;

/*
 * This class will be used for each of the monster objects
 *
 * I decided not to use inheritance in favor of readability: the code will be much easier to
 * maintain and edit with one overly long switch setting monster variables than with tabing between
 * 10 more classes
 *
 */

public class badGuy {
    // declare our outside variables that will be passed in on creation
    private ImageView imgSelf;
    private GameActivity.worldControl world;
    private playerCharacter player;

    Random rand = new Random(); //enable random numbers

    //set default values to lots of variables
    // many of these will be overwritten in the createImage method which is called when the monster objected is created
    boolean active = false;
    String type = "";
    int speed = 4;
    int slot = 101;
    int objectID;
    boolean hasHit = false;
    int WIDTH = 120;
    int HEIGHT = 120;
    int damage = 150;

    int gravity = 0; //basic movement variables
    int ySpeed = 0;
    int GROUND = 0;

    int yDirectionChangeMaxCD = 0; //variables for moving in a zig-zag pattern
    int yDirectionChangeCD = 0;

    int bouncePower = 0; //variables for auto jumping when landing on the ground
    int bounceLoss = 0;

    //can't reference the passed objects at this scope, so wait to set hitRange till we're inside a method
    int hitRange; // = (int) (Math.round(player.imgPlayer.getWidth()) * .8);

    int xAnchor = 0; //variables for random x movement
    int xDanceRange = 0;

    boolean watchPlayer = false; //will it turn and point at the player
    boolean poundPlayer = false; //will it dive at the player when close
    int blowUp = 0; //number of sparks that appear when this hits the player

    int backUpTime = 0; //number of ticks to move reverse of speed (right instead of left)
    int maxBackUpTime = 0; //
    int backUps = 0; //number of times to enter backup time instead of leaving the screen

    int actCD = 0;
    int maxActCD = 0;
    String specialMove = "";

    int sky = 160;

    String rangeInfo;

    // constructor
    public badGuy(GameActivity.worldControl world, playerCharacter player, ImageView imgSelf) {
        this.world = world;
        this.player = player;
        this.imgSelf = imgSelf;
    }

    // this method is called when the monster object is created and sets various parameters based
    // on its type
    public void createImage(int slotNumber, String monsterType, boolean setStartPosition, int spawnX, int spawnY)
    {
        hitRange =  (int) (Math.round(player.imgPlayer.getWidth()) * .8);

        int xStart = 800;
        int yStart = rand.nextInt(400) + 801;

        // monster type properties are set here //////////////////////////////////////////////////
        switch (monsterType) {
            case "fireball":
                imgSelf.setImageResource(R.drawable.badthing);
                gravity = 1;
                bouncePower = -24;
                bounceLoss = 6;
                yStart = rand.nextInt(500) + 501;
                blowUp = 10;
                break;
            case "ninja":
                imgSelf.setImageResource(R.drawable.ninja_01);
                speed = 10;
                yStart = ground();
                break;
            case "eye":
                imgSelf.setImageResource(R.drawable.eye_crop);
                yStart = rand.nextInt(400) + 801;
                watchPlayer = true;
                if(rand.nextInt(2) == 1 && player.level > 3){poundPlayer = true;}
                break;
            case "bomb":
                imgSelf.setImageResource(R.drawable.bombbox);
                xDanceRange = 10;
                blowUp = 20;
                break;
            case "firewave":
                imgSelf.setImageResource(R.drawable.badthing);
                yDirectionChangeMaxCD = 40;
                ySpeed = 4;
                speed = 5;
                blowUp = 10;
                break;
            case "goblin":
                imgSelf.setImageResource(R.drawable.goblin_crop);
                bouncePower = (rand.nextInt(12) * -1) -14;
                bounceLoss = rand.nextInt(2);
                speed = rand.nextInt(4) + 3;
                gravity = 1;
                break;
            case "bird":
                imgSelf.setImageResource(R.drawable.goobird_01);
                yStart = rand.nextInt(500) + 501;
                yDirectionChangeMaxCD = 10;
                ySpeed = 1;
                backUps = rand.nextInt(3);
                if(player.level > 10){backUps = rand.nextInt(3) + 1;}
                maxBackUpTime = (rand.nextInt(4) + 3) * 30;
                break;
            case "fireman":
                imgSelf.setImageResource(R.drawable.fireman);
                gravity = 1;
                bouncePower = -5;
                maxActCD = 30 * 2;
                actCD = 45;
                specialMove = "fire toss";
                break;
            case "cloud":
                imgSelf.setImageResource(R.drawable.cloud);
                gravity = -1;
                maxActCD = 22;
                actCD = 22;
                specialMove = "stop and shoot";
        } // end of monsterType switch ////////////////////////////////////////////////////

        if(setStartPosition){
            xStart = spawnX;
            yStart = spawnY;
        }


        xAnchor = xStart; // set xBound, should always default to wherever the monster is spawned
        yDirectionChangeCD = yDirectionChangeMaxCD;

        //define positional constants
        WIDTH =  imgSelf.getWidth();
        HEIGHT = imgSelf.getHeight();
        GROUND = player.GROUND_PIXEL - HEIGHT;

        active = true;
        slot = slotNumber; // this is the index number where the object is saved in the world.badGuys array
        type = monsterType;

        imgSelf.setTranslationX(xStart); //initial placement
        imgSelf.setTranslationY(yStart);
    }



    // this method will be called every frame and moves the monster
    public void movement()
    {
        ySpeed += gravity; // apply gravity (will be 0 for floating monsters)

        // dive at player if close and above the player and the monster does that
        if(poundPlayer && ySpot() < player.ySpot()){
            int d = Math.abs(xSpot() - player.xSpot());
            if(d < 40){ySpeed = 12;}
        }

        imgSelf.setTranslationY(ySpot() + ySpeed); // move the monster vertically steps equal to its ySpeed

        if(ySpot() > ground()){ // if below the ground get back on the ground and stop falling
            ySpeed = 0;
            imgSelf.setTranslationY(ground());

            // if bounce stats are enabled apply them here
            ySpeed += bouncePower;
            bouncePower += bounceLoss;

        }

        if(ySpot() < sky){
            imgSelf.setTranslationY(sky);
        }

        // flip ySpeed if CD is up if the monsters does that
        if(yDirectionChangeMaxCD != 0){
            yDirectionChangeCD --;
            if(yDirectionChangeCD < 1){
                yDirectionChangeCD = yDirectionChangeMaxCD;
                ySpeed *= -1;
            }
        }


        if(backUpTime > 0){
            backUpTime --;
            imgSelf.setTranslationX(xSpot() + speed); // move right
            if(backUpTime == 0){imgSelf.setRotation(0);}
        } else {
            imgSelf.setTranslationX(xSpot() - speed); // move left
        }

        // if monster has a dance range, move it randomly left or right with a bias towards right
        if(xDanceRange != 0){
            int x = rand.nextInt(xDanceRange * 2) - xDanceRange + 1;
            imgSelf.setTranslationX(xSpot() + x);

            // reduce max xSpot value so even with random movement the monster is eventually moving left
            xAnchor -= speed;
            if(xSpot() > xAnchor){ imgSelf.setTranslationX(xAnchor); }
        }

        // a few monsters have special move that creates more monsters
        if(maxActCD > 0){
            actCD --; if(actCD < 1){
                switch (specialMove) {
                    case "fire toss":
                        world.makeMonsterObject("fireball", true, xSpot(), ySpot());
                        break;
                    case "stop and shoot":
                        ySpeed = 0;
                        if(rand.nextInt(2) == 1) {
                            world.makeMonsterObject("fireball", true, xSpot(), ySpot());
                        } else {
                            world.makeMonsterObject("firewave", true, xSpot(), ySpot());
                        }
                        maxActCD *= 2;
                        break;
                }
                actCD = maxActCD;
            }
        }

        // rotate image so that it is pointing at the player
        if(watchPlayer){
            double a = player.xSpot() - xSpot();
            double b = player.ySpot() - ySpot();
            double k = Math.atan2(b, a);         //stolen code from stack overflow. idk how the math works.
            double r = ((k*360/(2*Math.PI))+90); //i'll google "atan2" someday

            imgSelf.setRotation((float) r);
        }

        // save the location of the center of the monster for hit testing
        int xPoint = xSpot() + Math.round( (float) imgSelf.getWidth() / 2);
        int yPoint = ySpot() + Math.round( (float) imgSelf.getHeight() / 2);

        // call the method that checks hit test against the player and resolve the hit if found
        if(hitPlayer(xPoint, yPoint)){
            for(int i=0; i<blowUp; i++){
                world.spawnParticleEffect("spark", xPoint, yPoint);
            }
            hasHit = true;
            player.hurt(damage);
        }

        if(xSpot() < 10 && backUps > 0){ //set backup time if monster does that
            backUps --;
            backUpTime = maxBackUpTime;
            imgSelf.setRotation(180);
        }

        // remove the object if its hit the player or moves too far left
        if(xSpot() < -80 || hasHit == true){
            remove();
        }

    }

    // player hit test method
    private boolean hitPlayer(int x, int y)
    {
        /// find values for center of player
        int px = player.xSpot() + (player.imgPlayer.getWidth() / 2);
        int py = player.ySpot() + (player.imgPlayer.getHeight() / 2);

        /// test player center against the 4 corner points of the monster and return true if it is inside
        return (x > px - hitRange && x < px + hitRange && y > py - hitRange && y < py + hitRange);
    }

    // returns an integer of x location
    private int xSpot()
    {
        return Math.round(imgSelf.getX());
    }

    // returns an integer of y location
    private int ySpot()
    {
        return Math.round(imgSelf.getTranslationY());
    }

    // returns an integer of ground location
    // i'd rather use a constant than a method, but android studio is weird with reading the dimensions of an imageView right after creating it
    // UPDATE: .height() wasn't working during onCreate because we were calling it BEFORE resources were loaded
    private int ground()
    {
        return player.GROUND_PIXEL - imgSelf.getHeight();
    }


    private void remove()
    {
        world.removeBadGuy(slot, imgSelf);
    }















}
