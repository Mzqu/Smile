package mq.org.smile;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Michael on 11/9/15.
 */
public class CircleAngleAnimation extends Animation {

    private RecordingProgressCircle circle;

    private float oldAngle;
    private float newAngle;

    public CircleAngleAnimation(RecordingProgressCircle circle, int newAngle) {
        this.oldAngle = circle.getAngle();
        this.newAngle = newAngle;
        this.circle = circle;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

        circle.setAngle(angle);
        circle.requestLayout();
    }
}