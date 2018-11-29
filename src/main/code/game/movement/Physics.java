package main.code.game.movement;

import main.code.game.audio.AudioHandler;
import main.code.utils.Player;

import static main.code.utils.GameConstants.*;

public class Physics {
    private final float boxSize;
    private AudioHandler audioHandler = new AudioHandler();

    private boolean soundJump = false;
    private boolean energySound = true;

    public Physics(float boxSize) {
        this.boxSize = boxSize;
    }


    public Player takeAction(Player player, int[][] grid, boolean[] actions) {

        int k = 0;
        boolean sprint = false;

        //Jumping
        if (player.isJumping()) {
            k=2;
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
            soundJump = true;
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

                }
            }
        }

        if (player.isJumping() && soundJump) {
            audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
            audioHandler.playSoundEffect(audioHandler.jump);
            soundJump = false;
        }


        //Move left
        if (actions[0]) {

            if(k==0){
                k=1;
            }

            boolean collision = false;

            if (actions[1] && (player.getEnergy() > MIN_ENERGY || player.isSprinting())) {

                for (int i = 0; i < PLAYER_SPRINT_SPEED; i++) {
                    float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X - 1) * 1.0f) / boxSize;
                    float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y) * 1.0f) / boxSize;
                    float playerBoxPosY2 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT) * 1.0f) / boxSize;
                    float playerBoxPosY3 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT / 2) * 1.0f) / boxSize;

                    if (playerBoxPosX <= 0) {
                        collision = true;
                    } else {

                        if (grid[(int) playerBoxPosY][(int) playerBoxPosX] != 0)
                            collision = true;

                        if (grid[(int) playerBoxPosY2][(int) playerBoxPosX] != 0)
                            collision = true;

                        if (grid[(int) playerBoxPosY3][(int) playerBoxPosX] != 0)
                            collision = true;
                    }

                    if (!collision)
                        player.setCoord((short) ((player.getXPos() - 1)), player.getYPos());


                    collision = false;
                    sprint = true;
                    player.setSprint(true);
                }
            } else {
                for (int i = 0; i < PLAYER_SPEED; i++) {
                    float playerBoxPosX = ((player.getXPos() + PLAYER_ORIGIN_X - 1) * 1.0f) / boxSize;
                    float playerBoxPosY = ((player.getYPos() + PLAYER_ORIGIN_Y) * 1.0f) / boxSize;
                    float playerBoxPosY2 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT) * 1.0f) / boxSize;
                    float playerBoxPosY3 = ((player.getYPos() + PLAYER_ORIGIN_Y + PLAYER_HEIGHT / 2) * 1.0f) / boxSize;

                    if (playerBoxPosX <= 0) {
                        collision = true;
                    } else {

                        if (grid[(int) playerBoxPosY][(int) playerBoxPosX] != 0)
                            collision = true;

                        if (grid[(int) playerBoxPosY2][(int) playerBoxPosX] != 0)
                            collision = true;

                        if (grid[(int) playerBoxPosY3][(int) playerBoxPosX] != 0)
                            collision = true;
                    }


                    if (!collision)
                        player.setCoord((short) ((player.getXPos() - 1)), player.getYPos());

                    collision = false;

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
            energySound = true;
        } else if (player.getEnergy() < 1000){
            player.addEnergy(1);
            if (player.getEnergy() == 1000 && energySound){
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                System.out.println("volume for energy sound " + audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.energySound);
                energySound = false;
            }
        }

        if (player.getEnergy() == 0) {
            player.setSprint(false);
        }


        if (player.getEnergy() == 1000) {
            if(energySound) {
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.energySound);
                energySound = false;
            }
        }else{
            energySound = true;
        }

        player.setStateOfMovement(k);
        return player;
    }

}
