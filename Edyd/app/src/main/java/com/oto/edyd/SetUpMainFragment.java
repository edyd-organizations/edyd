package com.oto.edyd;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.oto.edyd.utils.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/9/2.
 */
public class SetUpMainFragment extends Fragment implements View.OnClickListener {

    private View setUpMainFragmentView;
    private LinearLayout setupBack;
    private ListView setupList;
    private FragmentManager setUpFragmentManager;

    //ListView资源
    private String[] textResources; //文字资源
    private int[] idResources; //ID资源

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setUpMainFragmentView = inflater.inflate(R.layout.setup_main_fragment, null);

        //初始化数据
        initFields(setUpMainFragmentView);

        List<Map<String, Object>> dataSets = new ArrayList<Map<String, Object>>();
        for(int i = 0; i < textResources.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("list_text", textResources[i]);
            map.put("list_arrow", R.mipmap.right_arrow);
            dataSets.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity().getApplicationContext(), dataSets, R.layout.setup_item,
                new String[]{"list_text", "list_arrow"}, idResources); //ListView适配器

        setupList.setAdapter(simpleAdapter); //设置适配器
        setupList.setOnItemClickListener(new SetUpListItemOnClickListener());
        setupBack.setOnClickListener(this);
        return setUpMainFragmentView;
    }

    private void initFields(View view) {
        this.setUpFragmentManager = ((SetUpActivity)getActivity()).setUpFragmentManager;
        setupBack = (LinearLayout)view.findViewById(R.id.setup_back);
        setupList = (ListView) view.findViewById(R.id.setup_main_list);

        textResources = this.getResources().getStringArray(R.array.setup_list_string);
        idResources = new int[]{R.id.list_text, R.id.list_arrow};
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setup_back:
                getActivity().finish(); //结束SetUpActivity
                break;
            default:
        }
    }

    private class SetUpListItemOnClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            SetUpDetailFragment setUpDetailFragment = SetUpDetailFragment.newInstance(textResources[position], position);
            FragmentTransaction transaction = setUpFragmentManager.beginTransaction();

            switch (position) {
                case 0:
                    transaction.replace(R.id.common_frame, setUpDetailFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case 1:
                    transaction.replace(R.id.common_frame, setUpDetailFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case 2:
                    transaction.replace(R.id.common_frame, setUpDetailFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case 3:
                    break;
            }
        }
    }
}
