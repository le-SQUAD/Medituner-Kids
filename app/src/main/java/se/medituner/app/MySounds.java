package se.medituner.app;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
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

    public SoundPool soundPool;
    public int jump, blink, happy;


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
                    // numebr of different sounds
                    .setMaxStreams(3)
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else {
            //first 3 = amout of different sounds
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        }

        jump = soundPool.load(this, R.raw.jump, 1);
        blink = soundPool.load(this, R.raw.blink, 1);
        happy = soundPool.load(this, R.raw.blink, 1);

    }

    public void playsound(View v) {
        switch (v.getId()){
            case R.id.button_jumpsound:
                soundPool.play(jump, 1, 1, 0, 0, 1);
             case R.id.button_blink:
                 soundPool.play(jump, 1, 1, 0, 0, 1);
            case R.id.button_happy:
                soundPool.play(jump, 1, 1, 0, 0, 1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }
}

