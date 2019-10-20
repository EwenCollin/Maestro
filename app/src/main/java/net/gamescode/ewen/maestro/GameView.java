package net.gamescode.ewen.maestro;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private SurfaceHolder SurfaceHolder;
    private Paint paintText = new Paint();
    private long previousTime = 0;
    private GameManager gameManager;


    public GameView(Context context) {
        super(context);
        SurfaceHolder = getHolder();
        getHolder().addCallback(this);
        gameManager = new GameManager(getResources(), context);

        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        if(!thread.getRunning()) {
            thread.setRunning(true);
            thread.start();
            //Log.d("surfaceCreated", "Thread gave back the hand");
            //((Activity) this.getContext()).finish();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("SurfaceChanged", "Surface has now changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ((Activity) this.getContext()).finish();
        Log.d("surfaceDestroyed", "Surface Destroyed successfully");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
        }
        return true;
    }

    public void update() {
        gameManager.update();
    }

    @Override
    public void draw(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - previousTime;
        previousTime = System.currentTimeMillis();
        long fps = (long)1000/elapsedTime;
        //Log.d("GameView", "fps : " + fps);
        if(canvas != null) {
            super.draw(canvas);
            gameManager.draw(canvas);
            showFps(canvas, fps);
        }

    }

    public void showFps(Canvas canvas, Long fps) {
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(100);
        canvas.drawText("FPS : " + fps, 10, 100, paintText);
    }
}
