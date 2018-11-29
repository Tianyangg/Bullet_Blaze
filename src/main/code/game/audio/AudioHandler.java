package main.code.game.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.Random;

/**
 * Implements the AudioHandler
 *
 * @author Maria Antonia Badarau
 */

public class AudioHandler {

    //public String path = "out//production//knightshade//main//resources//audio//";
    public String path = "src//main//resources//audio//";


    public static boolean isBackgroundOn = false;
    public static boolean isEffectOn = true;

    //default values of the audio
    private static double backgroundVolume = 0.5;
    private static double soundEffectVolume = 0.5;

    //background music in Game
    public AudioClip backgroundMusic = new AudioClip(new File(path + "background.mp3").toURI().toString());

    // sound effects for in-game
    public AudioClip jump = new AudioClip(new File(path + "jump.wav").toURI().toString());
    public AudioClip energySound = new AudioClip(new File(path + "energySound.wav").toURI().toString());
    public AudioClip clickSound = new AudioClip(new File(path + "clickSound.mp3").toURI().toString());
    public AudioClip gameOverSound = new AudioClip(new File(path + "game_over.mp3").toURI().toString());
    public AudioClip fightSound = new AudioClip(new File(path + "fight.mp3").toURI().toString());
    public AudioClip prepareYourself = new AudioClip(new File(path + "prepare_yourself.mp3").toURI().toString());


    // weapons sound effects
    public AudioClip pistolSound = new AudioClip(new File(path + "gunSound.mp3").toURI().toString());
    public AudioClip machineGunSound = new AudioClip(new File(path + "machineGunSound.mp3").toURI().toString());
    public AudioClip shotgunSound = new AudioClip(new File(path + "shotgunSound.mp3").toURI().toString());


    public AudioHandler() {
    }

    /**
     * Updates the value of the background music
     *
     * @param bgroundVolume
     */
    public void updateBackgroundVolume(double bgroundVolume) {
        backgroundVolume = bgroundVolume;
    }

    /**
     * Returns the value of the volume of the background music
     *
     * @return backgroundVolumes
     */

    public double getBackgroundVolume() {
        return backgroundVolume;
    }

    /**
     * Returns the value of the volume of the sound effect music
     *
     * @return
     */

    public double getSoundEffectVolume() {
        return soundEffectVolume;
    }

    /**
     * Plays the sound effect
     * Uses AudioClip
     *
     * @param audioClip
     */

    public void playSoundEffect(AudioClip audioClip) {
        audioClip.play();
    }

    /**
     * Plays the background music
     * Sets the value of the background music
     * Sets the boolean isBackgroundOn to true as the music is playing
     *
     * @param audioClip
     */

    public void playBackgroundMusicInGame(AudioClip audioClip) {
        audioClip.setVolume(getBackgroundVolume());
        audioClip.play();
        isBackgroundOn = true;
    }

    /**
     * Updates the volume of the sound effects
     *
     * @param SFXvolume
     */

    public void updateSFXVolume(double SFXvolume) {
        pistolSound.setVolume(SFXvolume);
        machineGunSound.setVolume(SFXvolume);
        shotgunSound.setVolume(SFXvolume);
        jump.setVolume(SFXvolume);
        shotgunSound.setVolume(SFXvolume);
        machineGunSound.setVolume(SFXvolume);
        pistolSound.setVolume(SFXvolume);
        energySound.setVolume(SFXvolume);
        energySound.setVolume(SFXvolume);
        gameOverSound.setVolume(SFXvolume);
        fightSound.setVolume(SFXvolume);
        prepareYourself.setVolume(SFXvolume);
        soundEffectVolume = SFXvolume;
    }

    /**
     * Returns the value of the boolean isBackgroundOn
     * Used to check if the music is on
     *
     * @return
     */

    public static boolean isBackgroundOn() {
        return isBackgroundOn;
    }

    /**
     * Returns the value of the boolean isEffectOn
     * Used to check if the sound effects are enabled
     *
     * @return
     */

    public static boolean isEffectOn() {
        return isEffectOn;
    }

    /**
     * Sets the value of the boolean isBackgroundOn
     *
     * @param backgroundOn
     */

    public static void setIsBackgroundOn(boolean backgroundOn) {
        isBackgroundOn = backgroundOn;
    }

    /**
     * Sets the value of the boolean isEffectOn
     *
     * @param effectOn
     */

    public static void setIsEffectOn(boolean effectOn) {
        isEffectOn = effectOn;
    }
}
