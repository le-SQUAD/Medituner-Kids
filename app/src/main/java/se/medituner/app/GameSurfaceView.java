package se.medituner.app;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.jar.Attributes;

public class GameSurfaceView extends GLSurfaceView{
    private GameRenderer renderer;

    public GameSurfaceView(Context context) {
        super(context);

        init(context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
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

        System.out.println(e);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                renderer.flip();
                requestRender();
        }

        return true;
    }

}
