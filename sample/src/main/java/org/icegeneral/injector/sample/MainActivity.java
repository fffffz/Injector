package org.icegeneral.injector.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by linjianjun on 2017/6/5.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("TEST", Utils.getClickText(v));
    }
}
