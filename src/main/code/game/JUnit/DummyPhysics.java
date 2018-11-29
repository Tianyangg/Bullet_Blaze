package main.code.game.JUnit;

import main.code.game.audio.AudioHandler;
import main.code.utils.Player;

import static main.code.utils.GameConstants.*;

//A physics class without the AudioHandler, as it would require a lot of prerequisite fiddling. Works the same.

public class DummyPhysics {
    private final float boxSize;
    //private AudioHandler audioHandler = new AudioHandler();

    private boolean soundJump = false;

    public DummyPhysics(float boxSize) {
        this.boxSize = boxSize;
    }

    private boolean energySound;

    public Player takeAction(Player player, int[][] grid, boolean[] actions) {

        int k = 0;
        boolean sprint = false;

        //Jumping
        if (player.isJumping()) {
            k = 2;
            for (int i = 0; i < PLAYER_JUMP_SPEED; i++) {
                float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y - 1) * 1.0f) / boxSize;
                float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X) * 1.0f) / boxSize;
                float playerBoxPosX2 = ((player.getXPos() + PLAYER_ORIGIN_X + PLAYER_WIDTH) * 1.0f) / boxSize;

                if ((playerBoxPosY > 0 && grid[(int) playerBoxPosY][(int) playerBoxPosX] == 0 && grid[(int) playerBoxPosY][(int) playerBoxPosX2] == 0 && (player.getInitial_y() - boxSize * PLAYER_JUMP_HEIGHT < player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT))) {
                    player.setCoord(player.getXPos(), (short) (player.getYPos() - 1));
                } else {
                    player.setJumping(false);
                }
            }
        }
        //Falling
        else {
            player.setFalling(true);
            for (int i = 0; i < PLAYER_JUMP_SPEED; i++) {
                float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT + 1) * 1.0f) / boxSize;
                float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X) * 1.0f) / boxSize;
                float playerBoxPosX2 = ((player.getXPos() + PLAYER_ORIGIN_X + PLAYER_WIDTH) * 1.0f) / boxSize;

                if (((playerBoxPosY) < grid.length) && grid[(int) playerBoxPosY][(int) playerBoxPosX2] == 0 && grid[(int) playerBoxPosY][(int) playerBoxPosX] == 0) {
                    k = 3;
                    player.setCoord(player.getXPos(), (short) (player.getYPos() + 1));
                } else {
                    player.setInitial_y(player.getYPos());
                    player.setFalling(false);
                    soundJump = true;

                }
            }
        }

        /*if (player.isJumping() && soundJump) {
            audioHandler.playSoundEffect(audioHandler.audioInput.jump);
            soundJump = false;
        }*/


        //Move left
        if (actions[0]) {

            if(k==0){
                k=1;
            }

            boolean colision = false;
            /*float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y) * 1.0f) / boxSize;
            float playerBoxPosY2 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT) * 1.0f) / boxSize;
            float playerBoxPosY3 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT/2) * 1.0f) / boxSize;*/

            if (actions[1] && (player.getEnergy() > MIN_ENERGY || player.isSprinting())) {

                for (int i = 0; i < PLAYER_SPRINT_SPEED; i++) {
                    float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X - 1) * 1.0f) / boxSize;
                    float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y) * 1.0f) / boxSize;
                    float playerBoxPosY2 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT) * 1.0f) / boxSize;
                    float playerBoxPosY3 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT / 2) * 1.0f) / boxSize;

                    if(playerBoxPosX <= 0){
                        colision = true;
                    }else{

                        if(grid[(int) playerBoxPosY][(int) playerBoxPosX] != 0)
                            colision = true;

                        if(grid[(int) playerBoxPosY2][(int) playerBoxPosX] != 0)
                            colision = true;

                        if(grid[(int) playerBoxPosY3][(int) playerBoxPosX] != 0)
                            colision = true;
                    }

                    if(!colision)
                        player.setCoord((short) ((player.getXPos() - 1)), player.getYPos());


                    colision = false;
                    sprint = true;
                    player.setSprint(true);
                }
            } else {
                for (int i = 0; i < PLAYER_SPEED; i++) {
                    float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X - 1) * 1.0f) / boxSize;
                    float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y) * 1.0f) / boxSize;
                    float playerBoxPosY2 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT) * 1.0f) / boxSize;
                    float playerBoxPosY3 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT / 2) * 1.0f) / boxSize;

                    if(playerBoxPosX <= 0){
                        colision = true;
                    }else{

                        if(grid[(int) playerBoxPosY][(int) playerBoxPosX] != 0)
                            colision = true;

                        if(grid[(int) playerBoxPosY2][(int) playerBoxPosX] != 0)
                            colision = true;

                        if(grid[(int) playerBoxPosY3][(int) playerBoxPosX] != 0)
                            colision = true;
                    }


                    if(!colision)
                        player.setCoord((short) ((player.getXPos() - 1)), player.getYPos());

                    colision = false;

                }
            }
        }

        //Move right
        if (actions[2]) {

            if(k==0){
                k=1;
            }

            boolean colision = false;
            float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y) * 1.0f) / boxSize;
            float playerBoxPosY2 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT) * 1.0f) / boxSize;
            float playerBoxPosY3 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT/2) * 1.0f) / boxSize;

            if (actions[1] && (player.getEnergy() > MIN_ENERGY || player.isSprinting())) {

                for (int i = 0; i < PLAYER_SPRINT_SPEED; i++) {
                    float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X + PLAYER_WIDTH + 1) * 1.0f) / boxSize;

                    if(playerBoxPosX >= grid[0].length){
                        colision = true;
                    }else {

                        if (grid[(int) playerBoxPosY][(int) playerBoxPosX] != 0) {
                            colision = true;
                        }

                        if (grid[(int) playerBoxPosY2][(int) playerBoxPosX] != 0) {
                            colision = true;
                        }

                        if (grid[(int) playerBoxPosY3][(int) playerBoxPosX] != 0) {
                            colision = true;
                        }
                    }

                    if (!colision) {
                        player.setCoord((short) (player.getXPos() + 1), player.getYPos());
                    }

                    colision = false;

                }
                actions[0] = false;
                sprint = true;
                player.setSprint(true);

            } else {
                for (int i = 0; i < PLAYER_SPEED; i++) {
                    float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X + PLAYER_WIDTH + 1) * 1.0f) / boxSize;

                    if(playerBoxPosX >= grid[0].length){
                        colision = true;
                    }else {

                        if (grid[(int) playerBoxPosY][(int) playerBoxPosX] != 0) {
                            colision = true;
                        }

                        if (grid[(int) playerBoxPosY2][(int) playerBoxPosX] != 0) {
                            colision = true;
                        }

                        if (grid[(int) playerBoxPosY3][(int) playerBoxPosX] != 0) {
                            colision = true;
                        }


                    }

                    if (!colision) {
                        player.setCoord((short) (player.getXPos() + 1), player.getYPos());
                    }

                    colision = false;

                }
            }

        }

        if (sprint) {
            player.addEnergy(ENERGY_DRAIN);
        } else {
            player.addEnergy(1);
        }

        if (player.getEnergy() == 0) {
            player.setSprint(false);
        }
        /*if (player.getEnergy() == 1000 && energySound){
            audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
            audioHandler.playSoundEffect(audioHandler.audioInput.energySound);
            energySound = false;
        }*/

        player.setStateOfMovement(k);
        return player;
    }

}
