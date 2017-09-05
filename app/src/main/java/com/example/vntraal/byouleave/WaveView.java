package com.example.vntraal.byouleave;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by vntants on 9/4/17.
 */

public class WaveView extends View {

    private Handler handler;
    private Context mContext = null;

    private int width = 0;

    /**
     * x, y are used to plot the path for wave
     */
    float x;
    float y;

    private Paint firstWaveColor;

    int lineThickness = 5;
    int amplitudeFactor = 12;
    int period = 50;
    int amplitude = 80;
    private float shift = 0;

    private int quadrant;

    Path firstWavePath = new Path();

    private float speed = (float) 0.1;

    public WaveView(Context context) {
        super(context);
        init(context);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        mContext = context;

        firstWaveColor = new Paint();
        firstWaveColor.setAntiAlias(true);
        firstWaveColor.setStrokeWidth(2);
        firstWaveColor.setColor(Color.parseColor("#FFFFFF"));

        handler = new Handler();
        handler.postDelayed(new WaveRunnable(), 16);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.alpha(0));
        quadrant = getHeight()/2;
        width = canvas.getWidth();

        firstWavePath.lineTo(0, quadrant);

        int i;

        for (i = 0; i < width + 10; i = i + 10) {
            x = (float) i;

            y = quadrant + amplitudeFactor * (float) Math.sin(((i + 10) * Math.PI / period) + shift);

            firstWavePath.lineTo(x, y);
        }
        firstWavePath.lineTo(getWidth(), lineThickness);

        for (i = i + 0; i >= 0; i = i - 10) {
            x = (float) i;

            y = quadrant + lineThickness + amplitudeFactor * (float) Math.sin(((i + 10) * Math.PI / period) + shift);

            firstWavePath.lineTo(x, y);
        }

        canvas.drawPath(firstWavePath, firstWaveColor);
    }

    private class WaveRunnable implements Runnable {

        /**
         * This runnable helps to run animation in an infinite loop
         */
        @Override
        public void run() {
            firstWavePath.reset();
            shift = shift + speed;
            invalidate();
            handler.postDelayed(new WaveRunnable(), 16);
        }
    }
}
