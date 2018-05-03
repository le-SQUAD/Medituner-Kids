package se.medituner.app;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;

public class MojoClothingList implements Serializable {

    private ArrayList<ObjectAnimator> jumpObjectAnimators;
    private ArrayList<ObjectAnimator> fallObjectAnimators;
    private TimeInterpolator accelerateInterpolator, bounceInterpolator;

    public MojoClothingList(){
        jumpObjectAnimators = new ArrayList<>();
        fallObjectAnimators = new ArrayList<>();
        accelerateInterpolator = new AccelerateInterpolator();
        bounceInterpolator = new BounceInterpolator();
    }

    public void addClothing(ImageView resource){
        ObjectAnimator jumpAnim = ObjectAnimator.ofFloat(resource, "translationY", -500);
        jumpAnim.setInterpolator(accelerateInterpolator);
        jumpAnim.setDuration(505);
        jumpObjectAnimators.add(jumpAnim);

        ObjectAnimator fallAnim = ObjectAnimator.ofFloat(resource, "translationY", 0);
        fallAnim.setInterpolator(bounceInterpolator);
        fallAnim.setDuration(1000);
        fallObjectAnimators.add(fallAnim);
    }

    public void removeClothing(){
        jumpObjectAnimators = new ArrayList<>();
        fallObjectAnimators = new ArrayList<>();
    }

    public ArrayList<ObjectAnimator> getJumpClothingAnimations(){
        return jumpObjectAnimators;
    }

    public ArrayList<ObjectAnimator> getFallClothingAnimations(){
        return fallObjectAnimators;
    }
}
