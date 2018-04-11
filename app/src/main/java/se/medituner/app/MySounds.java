package se.medituner.app;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import static se.medituner.app.R.id.button_jumpsound;

public class MySounds extends AppCompatActivity {

    private SoundPool soundPool;
    private int jumping, blink, happy, cough, sad, star1, star2, star3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mojo_screen);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

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

        jumping = soundPool.load(this, R.raw.jumping, 1);
        blink = soundPool.load(this, R.raw.blink, 1);
        happy = soundPool.load(this, R.raw.happy, 1);
        cough = soundPool.load(this, R.raw.cough, 1);
        sad = soundPool.load(this, R.raw.sad, 1);
        star1 = soundPool.load(this, R.raw.star1, 1);
        star2 = soundPool.load(this, R.raw.star2, 1);
        star3 = soundPool.load(this, R.raw.star3, 1);

    }

    public void playsound(View v) {
        switch (v.getId()){

            case R.id.button_jumping:
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

             case R.id.button_blink:
                 soundPool.play(blink, 1, 1, 0, 0, 1);
                break;

            case R.id.button_happy:
                soundPool.play(happy, 1, 1, 0, 0, 1);
                break;

            case R.id.button_cough:
                soundPool.play(cough, 1, 1, 0, 0, 1);
                break;

            case R.id.button_sad:
                soundPool.play(sad, 1, 1, 0, 0, 1);
                break;

            case R.id.button_star1:
                soundPool.play(star1, 1, 1, 0, 0, 1);
                break;

            case R.id.button_star2:
                soundPool.play(star2, 1, 1, 0, 0, 1);
                break;

            case R.id.button_star3:
                soundPool.play(star3, 1, 1, 0, 0, 1);
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }
}

