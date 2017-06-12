package org.icegeneral.injector.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by linjianjun on 2017/6/5.
 */
public class MainFragment extends BaseFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        view.findViewById(R.id.buttonTest).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonTest:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Sample", "done");
                    }
                }, 6 * 1000);
                getActivity().onBackPressed();
                break;
        }
    }

}
