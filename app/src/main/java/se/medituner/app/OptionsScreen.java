package se.medituner.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import se.medituner.app.game.HighScore;
import se.medituner.app.MojoScreen;

public class OptionsScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_screen);
    }

    public void onButtonResetHighScore(View view) {
        Persistence persistence = new Persistence(this);

        HighScore.resetHighScore(persistence);
    }

    public void onButtonResetQueue(View view) {

        MojoScreen mojoScreen = (MojoScreen) getParent();
        mojoScreen.onButtonResetQueue(view);
    }
}

