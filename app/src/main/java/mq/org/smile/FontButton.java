package mq.org.smile;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by Michael on 1/25/16.
 */
public class FontButton extends Button {

    public FontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/PleaseWriteMeASong.ttf");
        setTypeface(tf);
    }
}
