package org.icegeneral.injector.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by linjianjun on 2017/6/5.
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.content);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new MainFragment())
                .commit();
    }

    @Override
    public void onClick(View v) {

    }
}
