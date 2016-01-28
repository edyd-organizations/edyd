package com.oto.edyd.module.common.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oto.edyd.R;

/**
 * 功能：待建设页面
 * 文件名：com.oto.edyd.module.common.fragment.WaitingBuildFragment.java
 * 创建时间：2016/1/28
 * 作者：yql
 */
public class WaitingBuildFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wait_build_fragment, null);
        return view;
    }
}
