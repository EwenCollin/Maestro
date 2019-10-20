package net.gamescode.ewen.maestro;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Display {
    private Resources resources;
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private int subDiv = (int) screenWidth/30;
    private Paint paint;
    private int timeOffset = 0;

    public Display(Resources res) {
        resources = res;
        paint = new Paint();
        paint.setTextSize(subDiv);

    }

    public void drawNotes(ArrayList<Integer[]> notes, Canvas canvas) {
        paint.setColor(Color.rgb(0, 255, 255));
        int Lleft = 0, Lbottom = 0;
        for(int i = 0; i < notes.size(); i++) {
            int left = (notes.get(i)[0] - timeOffset) + screenWidth;
            int right = (int)((notes.get(i)[0] - timeOffset) + screenWidth + 5);
            int bottom = -(notes.get(i)[1] + 24)*subDiv + screenHeight;
            int top = - (notes.get(i)[1] + 25)*subDiv + screenHeight;
            //Log.d("DisplayDrawNotes", "left : " + left + " bottom : " + bottom);
            if(right > 0) {
                canvas.drawRect(left, top, right, bottom, paint);
            }
            Lleft = left;
            Lbottom = bottom;
        }
        paint.setColor(Color.BLACK);
        //canvas.drawText("left : " + Lleft + " bottom : " + Lbottom, 10, 190, paint);
        timeOffset = timeOffset + 5;
    }

    public void drawUi(Canvas canvas) {
        paint.setColor(Color.WHITE);
        int left = 0, right = screenWidth;
        for (int i = 0; i < screenHeight/subDiv; i++) {
            int height = i*subDiv + 10;
            canvas.drawRect(left, height, right, height - 1, paint);
        }
        for (int i = -25; i < 36; i++) {
            canvas.drawText(Utils.noteNToNoteSfr(i), 10, -(i+24)*subDiv + screenHeight, paint);
        }

    }

    public void resetDisplay(Canvas canvas) {
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, screenHeight, screenWidth, 0, paint);
    }
}
