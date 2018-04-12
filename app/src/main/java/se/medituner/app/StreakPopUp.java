package se.medituner.app;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import org.w3c.dom.Text;

import static se.medituner.app.MojoScreen.streak;

public class StreakPopUp extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindowstreak);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .6), (int) (height * .45));
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
        String uriPath = "android.resource://se.medituner.app/"+R.raw.flydragonv2;
        Uri uri = Uri.parse(uriPath);
        vv.setVideoURI(uri);
        vv.requestFocus();
        vv.start();
        //video implementation ends here

        TextView tv= (TextView) findViewById(R.id.textView2); //text: Congrats...
        tv.setText("Current streak:"+streak);
        ImageView iv1 = (ImageView)findViewById(R.id.imageView1);
        iv1.setImageResource(R.drawable.sun);

        if(streak>4) {
            ImageView iv2 = (ImageView) findViewById(R.id.imageView2);
            iv2.setImageResource(R.drawable.sun);
        }

        if(streak>5) {
            ImageView iv3 = (ImageView) findViewById(R.id.imageView3);
            iv3.setImageResource(R.drawable.sun);
        }
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
        }, 50000);
    }


}

