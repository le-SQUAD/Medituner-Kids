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
    private int jumping, blink, happy, cough, sad, star1, star2, star3;

    public enum Sound {
        S_JUMP,
        S_BLINK,
        S_HAPPY,
        S_COUGH,
        S_SAD,
        S_STAR1,
        S_STAR2,
        S_STAR3
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
                //first 3 = amout of different sounds
                soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
            }

            jumping = soundPool.load(context, R.raw.jumping, 1);
            blink = soundPool.load(context, R.raw.blink, 1);
            happy = soundPool.load(context, R.raw.happy, 1);
            cough = soundPool.load(context, R.raw.cough, 1);
            sad = soundPool.load(context, R.raw.sad, 1);
            star1 = soundPool.load(context, R.raw.star1, 1);
            star2 = soundPool.load(context, R.raw.star2, 1);
            star3 = soundPool.load(context, R.raw.star3, 1);
        }
    }

    public void playSound(Sound sound) {
        switch (sound){
            case S_JUMP:
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(jumping, 1, 1, 0, 0, 1);
                    }
                }, 500);


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(jumping, 1, 1, 0, 0, 1);
                    }
                }, 1000);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        soundPool.play(jumping, 1, 1, 0, 0, 1);
                    }
                }, 1100);

                break;

            case S_BLINK:
                soundPool.play(blink, 1, 1, 0, 0, 1);
                break;

            case S_HAPPY:
                soundPool.play(happy, 1, 1, 0, 0, 1);
                break;

            case S_COUGH:
                soundPool.play(cough, 1, 1, 0, 0, 1);
                break;

            case S_SAD:
                soundPool.play(sad, 1, 1, 0, 0, 1);
                break;

            case S_STAR1:
                soundPool.play(star1, 1, 1, 0, 0, 1);
                break;

            case S_STAR2:
                soundPool.play(star2, 1, 1, 0, 0, 1);
                break;

            case S_STAR3:
                soundPool.play(star3, 1, 1, 0, 0, 1);
                break;
        }
    }

}