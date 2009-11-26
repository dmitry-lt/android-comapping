package com.comapping.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.comapping.android.service.NotificationsChecker;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene Bakisov
 * Date: 26.11.2009
 * Time: 14:51:02
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "starting service");
        Button bindBtn = (Button) findViewById(R.id.bindBtn);
        bindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startService(new Intent(MainActivity.this,
                        NotificationsChecker.class));
            }
        });
        Button unbindBtn = (Button) findViewById(R.id.unbindBtn);
        unbindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                stopService(new Intent(MainActivity.this,
                        NotificationsChecker.class));
            }
        });
    }
}
