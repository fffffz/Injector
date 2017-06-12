package org.icegeneral.injector.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by linjianjun on 2017/6/5.
 */
public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new MainFragment())
                .commit();
    }
}
