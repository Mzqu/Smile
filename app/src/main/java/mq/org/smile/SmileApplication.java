package mq.org.smile;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Michael on 11/6/15.
 */
public class SmileApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "u1Dizultpc8yxJaXMJ7vbAY4hu6xFcNrPV2x13bw", "rl1JRHhHJsvL3mlhHJfdgRfwPzeS9rNKKOQ1hjZ8");

    }
}
