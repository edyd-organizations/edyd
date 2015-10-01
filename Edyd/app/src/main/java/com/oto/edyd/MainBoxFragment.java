package com.oto.edyd;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yql on 2015/8/26.
 */
public class MainBoxFragment extends Fragment {
    private View boxView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boxView = inflater.inflate(R.layout.main_box, null);
        return boxView;
    }
}
