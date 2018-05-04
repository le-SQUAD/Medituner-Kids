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

    /**
     * Add an animated ObjectAnimator to the jump and fall animator lists.
     *
     * @param resource The ImageView to animate and add to queue.
     * @author Aleksandra Soltan
     */
    public void addClothing(ImageView resource){
        //Animate resource to jump up with Mojo
        ObjectAnimator jumpAnim = ObjectAnimator.ofFloat(resource, "translationY", -500);
        jumpAnim.setInterpolator(accelerateInterpolator);
        jumpAnim.setDuration(505);
        jumpObjectAnimators.add(jumpAnim);

        //Animate resource to fall and bounce with Mojo
        ObjectAnimator fallAnim = ObjectAnimator.ofFloat(resource, "translationY", 0);
        fallAnim.setInterpolator(bounceInterpolator);
        fallAnim.setDuration(1000);
        fallObjectAnimators.add(fallAnim);
    }

    /**
     * Reset jump and fall animator lists.
     *
     * @author Aleksandra Soltan
     */
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
