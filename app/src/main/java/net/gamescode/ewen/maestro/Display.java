package net.gamescode.ewen.maestro;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.ColorInt;

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

    public void updateOffset() {
        timeOffset = timeOffset + 5;
    }

    public void drawNotes(ArrayList<Integer[]> notes, Canvas canvas, @ColorInt int color, float size) {
        paint.setColor(color);
        int Lleft = 0, Lbottom = 0;
        for(int i = 0; i < notes.size(); i++) {
            int left = (notes.get(i)[0] - timeOffset) + screenWidth;
            int right = (int)((notes.get(i)[0] - timeOffset) + screenWidth + 5);
            int bottom = (int) (-(notes.get(i)[1] + 24 - size*2)*subDiv + screenHeight);
            int top = (int) (- (notes.get(i)[1] + 25 + size)*subDiv + screenHeight);
            //Log.d("DisplayDrawNotes", "left : " + left + " bottom : " + bottom);
            if(right > 0) {
                canvas.drawRect(left, top, right, bottom, paint);
            }
            Lleft = left;
            Lbottom = bottom;
        }
        paint.setColor(Color.BLACK);
        //canvas.drawText("left : " + Lleft + " bottom : " + Lbottom, 10, 190, paint);

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

    public void drawAmplitudes(float[] amplitudes, Canvas canvas, int type) {
        int offset = (timeOffset/5)%screenWidth;
        for(int i = 0; i < amplitudes.length; i++) {
            paint.setColor(Color.rgb(amplitudes[i],amplitudes[i],amplitudes[i]));
            if(type == 1 && amplitudes[i] > 0) {
                paint.setColor(Color.rgb(255,0,0));
                canvas.drawRect(offset, i*screenHeight/amplitudes.length + 10, offset + 1, i*screenHeight/amplitudes.length - 10, paint);
            }
            else if(type != 1) canvas.drawRect(offset, i*screenHeight/amplitudes.length, offset + 1, i*screenHeight/amplitudes.length - 1, paint);
        }
    }

    public void resetDisplay(Canvas canvas) {
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, screenHeight, screenWidth, 0, paint);
    }
}
