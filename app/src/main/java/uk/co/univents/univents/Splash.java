package uk.co.univents.univents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by RiccardoBroggi on 10/02/2017.
 * SplashScreen before loading Login
 */

public class Splash extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(Splash.this, Welcome.class));
                finish();
            }
        }, 3000);
    }
}