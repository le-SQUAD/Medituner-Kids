package se.medituner.app.game;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Custom GLSurfaceView that processes touch inputs and supplies correct calls to the underlying Scene.
 *
 * @author Aleksandra Soltan
 */
public class GameSurfaceView extends GLSurfaceView{
    Scene scene;

    private int maxX;
    private int halfX;

    /**
     * Create a new GameSurfaceView with provided context that will be used to gather information about
     * device size.
     *
     * Also creates the underlying scene and sets it up for rendering.
     *
     * @param context
     * @author Aleksandra Soltan
     */
    public GameSurfaceView(Context context) {
        super(context);

        init(context);
    }

    public GameSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        init(context);
    }

    private void init(Context context) {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        scene = new Scene(context);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(scene);

        maxX = Resources.getSystem().getDisplayMetrics().widthPixels;
        System.out.println("MAX X: " + maxX);
        halfX = maxX / 2;
    }

    /**
     * Process touch event.
     *
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        int x = (int)e.getRawX();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(x >= halfX){
                    scene.setMojoLane(Lane.LANE_RIGHT);
                }
                else{
                    scene.setMojoLane(Lane.LANE_LEFT);
                }
                requestRender();
        }
        return true;
    }

}
