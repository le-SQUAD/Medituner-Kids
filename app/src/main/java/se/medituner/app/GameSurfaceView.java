package se.medituner.app;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.jar.Attributes;

public class GameSurfaceView extends GLSurfaceView{
    private Scene scene;

    private int maxX;
    private int halfX;
    public GameSurfaceView(Context context) {
        super(context);

        init(context);
    }
        scene = new Scene(context);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(scene);

        maxX = Resources.getSystem().getDisplayMetrics().widthPixels;
        System.out.println("MAX X: " + maxX);
        halfX = maxX / 2;

    }

   /* public GameSurfaceView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        init(context);
    }
    */
   private void init(Context context){
       // Create an OpenGL ES 2.0 context
       setEGLContextClientVersion(2);

       renderer = new GameRenderer();

       // Set the Renderer for drawing on the GLSurfaceView
       setRenderer(renderer);
   }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        int x = (int)e.getRawX();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(x >= halfX){
                    scene.flipRight();
                }
                else{
                    scene.flipLeft();
                }
                requestRender();
        }
        return true;
    }

}
