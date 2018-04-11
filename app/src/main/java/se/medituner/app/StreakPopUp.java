package se.medituner.app;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

public class StreakPopUp extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindowstreak);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .4), (int) (height * .4));
        final Handler handler = new Handler();

        //video implementation starts here
        VideoView vv;
        vv = (VideoView)findViewById(R.id.videoView);
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        String uriPath = "android.resource://se.medituner.app/"+R.raw.flowers;
        Uri uri = Uri.parse(uriPath);
        vv.setVideoURI(uri);
        vv.requestFocus();
        vv.start();
        //video implementation ends here
        ImageView iv = (ImageView)findViewById(R.id.imageView3);
        iv.setImageResource(R.drawable.sun);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 2s = 2000ms

                //Intent intent=new Intent(StreakPopUp.this,MojoScreen.class);
                finish();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //cheks the API of the user
                    StreakPopUp.this.overridePendingTransition(R.anim.fade_in, R.anim.slide_down);
                }

                //intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               // startActivity(intent);
            }
        }, 9800);
    }


}

