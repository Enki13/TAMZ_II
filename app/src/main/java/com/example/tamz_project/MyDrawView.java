package com.example.tamz_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;


public class MyDrawView extends View {

    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Paint brushPaint;
    private Path brushPath;
    private int brushColor = 0xFF000000;
    private float mX, mY;
    private ArrayList<Path> paths = new ArrayList<Path>();

    public MyDrawView(Context context) {
        super(context);
        init();
    }

    public MyDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    private void init(){
        brushPath = new Path();

        brushPaint = new Paint();
        brushPaint.setColor(brushColor);
        brushPaint.setAntiAlias(true);
        brushPaint.setStrokeWidth(10);
        brushPaint.setStyle(Paint.Style.STROKE);
        brushPaint.setStrokeJoin(Paint.Join.ROUND);
        brushPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Path p : paths) {
            canvas.drawPath(p, brushPaint);
        }
        canvas.drawPath(brushPath, brushPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                brushPath.reset();
                brushPath.moveTo(x, y);
                mX = x;
                mY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= 5 || dy >= 5) {
                    brushPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                    mX = x;
                    mY = y;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                brushPath.lineTo(mX, mY);
                drawCanvas.drawPath(brushPath, brushPaint);
                paths.add(brushPath);
                brushPath = new Path();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }
}