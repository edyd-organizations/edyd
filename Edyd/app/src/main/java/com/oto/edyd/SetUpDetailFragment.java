package com.oto.edyd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by yql on 2015/9/2.
 */
public class SetUpDetailFragment extends Fragment implements View.OnClickListener{

    private View setupDetailFragmentView;
    private FragmentManager setUpFragmentManager;
    private LinearLayout setupDetailBack;
    private TextView setupDetailText;
    private TextView themeTextView;
    private TextView aboutTelephone; //电话号码
    private Button aboutNavigation; //一键导航
    private Button aboutAccess; //一键访问
    private Button aboutDial; //一键拨打

    private Intent intent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getArguments() != null) {
            int position = getArguments().getInt("position");
            switch (position) {
                case 0: //
                    setupDetailFragmentView = inflater.inflate(R.layout.setup_content_detail, null);
                    initField(setupDetailFragmentView, position);
                    break;
                case 1: //
                    setupDetailFragmentView = inflater.inflate(R.layout.setup_content_detail, null);
                    initField(setupDetailFragmentView, position);
                    break;
                case 2: //关于我们
                    setupDetailFragmentView = inflater.inflate(R.layout.about, null);
                    initField(setupDetailFragmentView, position);
                    themeTextView.setText("\u3000\u3000" + getResources().getString(R.string.about_theme_content));
                    aboutAccess.setOnClickListener(this);
                    aboutDial.setOnClickListener(this);
                    aboutNavigation.setOnClickListener(this);
                    break;
            }
            String param = getArguments().getString("param"); //设置Fragment标题
            setupDetailText.setText(param);
        }

        setupDetailBack.setOnClickListener(this);
        return setupDetailFragmentView;
    }

    /**
     * 初始化数据
     */
    private void initField(View view, int position) {
        this.setUpFragmentManager = ((SetUpActivity)getActivity()).setUpFragmentManager;
        setupDetailBack = (LinearLayout) view.findViewById(R.id.setup_detail_back);
        setupDetailText = (TextView) view.findViewById(R.id.setup_detail_text);
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:  //初始化关于我们数据
                themeTextView = (TextView) view.findViewById(R.id.about_theme_content);
                aboutNavigation = (Button) view.findViewById(R.id.about_navigation);
                aboutAccess = (Button) view.findViewById(R.id.about_access); //一键访问
                aboutDial = (Button) view.findViewById(R.id.about_dial);
                aboutTelephone = (TextView) view.findViewById(R.id.about_telephone);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setup_detail_back: //返回
                setUpFragmentManager.popBackStack();
                break;
            case R.id.about_navigation: //一键导航
                intent = new Intent((SetUpActivity)getActivity(), MarkerActivity.class);
                startActivity(intent);
                break;
            case R.id.about_access: //一键访问
                FragmentTransaction transaction = setUpFragmentManager.beginTransaction();
                transaction.replace(R.id.common_frame, new AboutAccessFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.about_dial: //一键拨打
                final String phoneNumber = aboutTelephone.getText().toString();
                new AlertDialog.Builder(getActivity()).setTitle("拨打电话")
                        .setMessage("确认拨打电话？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                break;
            default:
        }
    }

    public static SetUpDetailFragment newInstance(String text, int position) {
        SetUpDetailFragment setUpDetailFragment = new SetUpDetailFragment();
        Bundle args = new Bundle();
        args.putString("param", text);
        args.putInt("position", position);
        setUpDetailFragment.setArguments(args);
        return setUpDetailFragment;
    }
}
