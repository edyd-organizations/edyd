package com.oto.edyd;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by yql on 2015/8/26.
 */
public class MainBoxFragment extends Fragment implements View.OnClickListener {


    private View boxView;
    private ImageButton violate_check;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boxView = inflater.inflate(R.layout.main_box, null);
        violate_check = (ImageButton) boxView.findViewById(R.id.violate_check);
        violate_check.setOnClickListener(this);
        return boxView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.violate_check:
                Intent intent=new Intent(getActivity(),ViolateCheckActivity.class);
                startActivity(intent);
                break;
        }
    }
}