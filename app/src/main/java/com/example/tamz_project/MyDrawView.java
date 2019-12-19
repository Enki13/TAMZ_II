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
    private float mX, mY;
    private ArrayList<Stroke> layer1 = new ArrayList<Stroke>();
    private ArrayList<Stroke> layer2 = new ArrayList<Stroke>();
    private ArrayList<Stroke> layer3 = new ArrayList<Stroke>();
    private ArrayList<Stroke> undoLayer1 = new ArrayList<Stroke>();
    private ArrayList<Stroke> undoLayer2 = new ArrayList<Stroke>();
    private ArrayList<Stroke> undoLayer3 = new ArrayList<Stroke>();
    private int layer = 1;
    private int color;
    private int size;
    private int brushMode;

    private class Stroke {
        private Paint paint;
        private Path path;

        public Stroke(Paint paint, Path path) {
            this.paint = paint;
            this.path = path;
        }

        public Paint getPaint() {
            return paint;
        }

        public Path getPath() {
            return path;
        }
    }

    public MyDrawView(Context context) {
        super(context);
        init(context);
    }

    public MyDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    private void init(Context context) {
        brushPath = new Path();
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        color = 0xFF000000;
        size = 5;
        createNewBrush();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCanvas.drawColor(0xFFFFFFFF);
        for (Stroke s : layer1) {
            canvas.drawPath(s.getPath(), s.getPaint());
            drawCanvas.drawPath(s.getPath(), s.getPaint());
        }
        if (layer > 1) {
            for (Stroke s : layer2) {
                canvas.drawPath(s.getPath(), s.getPaint());
                drawCanvas.drawPath(s.getPath(), s.getPaint());
            }
        }
        if (layer > 2) {
            for (Stroke s : layer3) {
                canvas.drawPath(s.getPath(), s.getPaint());
                drawCanvas.drawPath(s.getPath(), s.getPaint());
            }
        }
        canvas.drawPath(brushPath, brushPaint);
        drawCanvas.drawPath(brushPath, brushPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                brushPath.reset();
                brushPath.moveTo(x, y);
                mX = x;
                mY = y;
                switch (layer) {
                    case 1:
                        undoLayer1.clear();
                        break;
                    case 2:
                        undoLayer2.clear();
                        break;
                    case 3:
                        undoLayer3.clear();
                        break;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= 5 || dy >= 5) {
                    brushPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                brushPath.lineTo(mX, mY);
                switch (layer) {
                    case 1:
                        layer1.add(new Stroke(brushPaint, brushPath));
                        break;
                    case 2:
                        layer2.add(new Stroke(brushPaint, brushPath));
                        break;
                    case 3:
                        layer3.add(new Stroke(brushPaint, brushPath));
                        break;
                }
                brushPath = new Path();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    public void setLayer(int l) {
        layer = l;
        invalidate();
    }

    public void eraseLayer() {
        switch (layer) {
            case 1:
                layer1.clear();
                break;
            case 2:
                layer2.clear();
                break;
            case 3:
                layer3.clear();
                break;
        }
        invalidate();
    }

    public void eraseAll() {
        layer1.clear();
        layer2.clear();
        layer3.clear();
        invalidate();
    }

    public void createNewBrush() {
        brushPaint = new Paint();
        brushPaint.setColor(color);
        brushPaint.setAntiAlias(true);
        brushPaint.setStrokeWidth(size);
        if(brushMode == 1)
            brushPaint.setStyle(Paint.Style.FILL);
        else
            brushPaint.setStyle(Paint.Style.STROKE);
        brushPaint.setStrokeJoin(Paint.Join.ROUND);
        brushPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setBrushSize(int size) {
        this.size = size;
        createNewBrush();
    }

    public void undo() {
        switch (layer) {
            case 1:
                if (!layer1.isEmpty()) {
                    undoLayer1.add(layer1.remove(layer1.size() - 1));
                }
                break;
            case 2:
                if (!layer2.isEmpty()) {
                    undoLayer2.add(layer2.remove(layer2.size() - 1));
                }
                break;
            case 3:
                if (!layer3.isEmpty()) {
                    undoLayer3.add(layer3.remove(layer3.size() - 1));
                }
                break;
        }
        invalidate();
    }

    public void redo() {
        switch (layer) {
            case 1:
                if (!undoLayer1.isEmpty()) {
                    layer1.add(undoLayer1.remove(undoLayer1.size() - 1));
                }
                break;
            case 2:
                if (!undoLayer2.isEmpty()) {
                    layer2.add(undoLayer2.remove(undoLayer2.size() - 1));
                }
                break;
            case 3:
                if (!undoLayer3.isEmpty()) {
                    layer3.add(undoLayer3.remove(undoLayer3.size() - 1));
                }
                break;
        }
        invalidate();
    }

    public Bitmap getCanvasBitmap(){
        return canvasBitmap;
    }

    public void setBrushColor(int color){
        this.color = color;
        createNewBrush();
    }

    public int getSize(){
        return size;
    }

    public void setMode(int i){
        brushMode = i;
        createNewBrush();
    }

    public int getBrushMode(){
        return brushMode;
    }

    public int getColor() { return color; }
}