/**
 * Sounds class - sounds ready to be implemented by different occations
 *
 *  @author Julia Danek
 */

package se.medituner.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;

public class Sounds {

    private static final Sounds instance = new Sounds();

    private SoundPool soundPool = null;

    private int jumping, blink, happy, cough, sad, star1, star2, star3, gamejump, gamesong, click, sideways;


    public enum Sound {
        S_JUMP,
        S_BLINK,
        S_HAPPY,
        S_COUGH,
        S_SAD,
        S_STAR1,
        S_STAR2,
        S_STAR3,
        S_GJUMP,
        S_GSONG,
        S_CLICK,
        S_SIDEWAYS


    }

    public static Sounds getInstance() {
        return instance;
    }

    private Sounds() {}

    public void loadSounds(Context context) {
        if (soundPool == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                soundPool = new SoundPool.Builder()
                        // number of different sounds
                        .setMaxStreams(8)
                        .setAudioAttributes(audioAttributes)
                        .build();

            } else {

                //3 = amout of different sounds were able to play at the same time
                soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);


            }

            jumping = soundPool.load(context, R.raw.jumping, 1);
            blink = soundPool.load(context, R.raw.blink, 1);
            happy = soundPool.load(context, R.raw.happy, 1);
            cough = soundPool.load(context, R.raw.cough, 1);
            sad = soundPool.load(context, R.raw.sad, 1);
            star1 = soundPool.load(context, R.raw.star1, 1);
            star2 = soundPool.load(context, R.raw.star2, 1);
            star3 = soundPool.load(context, R.raw.star3, 1);
            gamejump = soundPool.load(context, R.raw.gamejump, 1);
            gamesong = soundPool.load(context, R.raw.gamesong, 1);
            click = soundPool.load(context, R.raw.gamesong, 1);
            sideways = soundPool.load(context, R.raw.gamesong, 1);


        }
    }

    public void playSound(Sound sound) {
        playSound(sound, 0);
    }

    public void playSound(Sound sound, final int loopCount) {
        switch (sound){
            case S_JUMP:
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(jumping, 1, 1, 0, loopCount, 1);
                    }
                }, 500);


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(jumping, 1, 1, 0, loopCount, 1);
                    }
                }, 1000);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(jumping, 1, 1, 0, loopCount, 1);
                    }
                }, 1100);

                break;

            case S_BLINK:
                soundPool.play(blink, 1, 1, 0, loopCount, 1);
                break;

            case S_HAPPY:
                soundPool.play(happy, 1, 1, 0, loopCount, 1);
                break;

            case S_COUGH:
                soundPool.play(cough, 1, 1, 0, loopCount, 1);
                break;

            case S_SAD:
                soundPool.play(sad, 1, 1, 0, loopCount, 1);
                break;

            case S_STAR1:
                soundPool.play(star1, 1, 1, 0, loopCount, 1);
                break;

            case S_STAR2:
                soundPool.play(star2, 1, 1, 0, loopCount, 1);
                break;

            case S_STAR3:
                soundPool.play(star3, 1, 1, 0, loopCount, 1);
                break;

            case S_GJUMP:
                soundPool.play(gamejump,1,1,0,loopCount,1);
                break;

            case S_GSONG:
                soundPool.play(gamesong,1,1,0,loopCount,1);
                break;

            case S_CLICK:
                soundPool.play(click,1,1,0,loopCount,1);
                break;

            case S_SIDEWAYS:
                soundPool.play(sideways,1,1,0,loopCount,1);
                break;
        }

    }

}
