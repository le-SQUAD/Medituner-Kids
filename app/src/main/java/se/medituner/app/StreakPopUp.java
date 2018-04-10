package se.medituner.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

public class StreakPopUp extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindowstreak);
        DisplayMetrics dm =  new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
         int width= dm.widthPixels;
         int height=dm.heightPixels;
         getWindow().setLayout((int) (width*.4), (int) (height*.4));
    }
}
