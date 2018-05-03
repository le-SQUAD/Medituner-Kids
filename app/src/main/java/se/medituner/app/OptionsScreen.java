package se.medituner.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import se.medituner.app.game.HighScore;

public class OptionsScreen extends AppCompatActivity {

    /**
     * Options menu with buttons
     *
     * @param savedInstanceState Android caching
     * @author Vendela Vlk, Julia Danek
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_screen);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == android.R.id.home){
            //ends the activity
            this.finish();
        }
        return super.onOptionsItemSelected(item);
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

