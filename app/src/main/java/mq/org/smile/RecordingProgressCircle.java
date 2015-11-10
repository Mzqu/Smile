package mq.org.smile;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Michael on 11/8/15.
 */

public class RecordingProgressCircle extends View {
    int stroke=10;//set loading circle stroke thickness
    Resources r=getResources();
    int size=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());
    private static final int START_ANGLE_POINT = 270;
    CircleAngleAnimation animation = new CircleAngleAnimation(this, 360);

    private final Paint paint;
    private final RectF rect;

    private float angle;

    public RecordingProgressCircle(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        //Circle color
        paint.setColor(Color.RED);

        //size 200x200 example
        rect = new RectF(stroke, stroke, size - stroke, size - stroke);

        //Initial Angle (optional, it can be zero)
        angle = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void start(int duration){
        setVisibility(VISIBLE);
        animation.setDuration(duration);
        animation.setFillAfter(false);
        startAnimation(animation);
    }

    public void clear(){
        setVisibility(INVISIBLE);
        clearAnimation();
    }
}