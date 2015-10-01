package com.oto.edyd;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.UpdatePerson;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by yql on 2015/9/17.
 */
public class ModifySex extends Fragment implements View.OnClickListener{

    private View modifySexView;
    private ListView modifySexList;

    private LinearLayout modifyEnterInfoBack; //返回
    private TextView modifyEnterInfoTitle; //标题
    public FragmentManager accountInformationFragmentManager; //布局管理器

    private String sexType[];
    private UpdatePerson updatePerson;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        modifySexView = inflater.inflate(R.layout.select_sex, null);
        if(bundle != null) {
            updatePerson = (UpdatePerson)bundle.getSerializable("updatePerson");
            String sex = updatePerson.getSex();
            initFields(modifySexView);
            modifyEnterInfoTitle.setText(updatePerson.getTitle());
            sexType = getResources().getStringArray(R.array.sex);
            List<Map<String, Object>> dataSets = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < sexType.length; i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("tv_sex_type", sexType[i]);
                dataSets.add(map);
            }
            ModifySexListAdapter modifySexListAdapter = new ModifySexListAdapter(getActivity(), dataSets, sex);
            modifySexList.setAdapter(modifySexListAdapter);
        }
        modifySexList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView sexTextView = (TextView) view.findViewById(R.id.tv_sex_type);
                String sex = sexTextView.getText().toString();
                Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

                int gender = 0;
                if(sex.equals(Constant.UNKNOWN_SEX)) {
                    gender = 0;
                } else if (sex.equals(Constant.MALE)) {
                    gender = 1;
                } else if(sex.equals(Constant.FEMALE)) {
                    gender = 2;
                } else if(sex.equals(Constant.SECRECY_SEX)) {
                    gender = 3;
                }

                String url = Constant.ENTRANCE_PREFIX + "updatePerson.json?birthday="+updatePerson.getBirthday()+"&gender="+gender+"&nickName="+updatePerson.getNikeName()+"&sessionUuid="+sessionUuid;
                OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                                Toast.makeText(getActivity(), "修改信息失败", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            accountInformationFragmentManager.popBackStack();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        modifyEnterInfoBack.setOnClickListener(this);
        return modifySexView;
    }

    private void initFields(View modifySexView) {
        modifySexList = (ListView) modifySexView.findViewById(R.id.select_sex_list);
        modifyEnterInfoBack = (LinearLayout) modifySexView.findViewById(R.id.modify_enter_info_back);
        modifyEnterInfoTitle = (TextView) modifySexView.findViewById(R.id.tv_enter_info_title);
        accountInformationFragmentManager = ((AccountInformationActivity)getActivity()).accountInformationFragmentManager;
    }

    public static ModifySex newInstance(UpdatePerson updatePerson) {
        ModifySex modifyEnterInfo = new ModifySex();
        Bundle args = new Bundle();
        args.putSerializable("updatePerson", updatePerson);
        modifyEnterInfo.setArguments(args);
        return modifyEnterInfo;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modify_enter_info_back:
                accountInformationFragmentManager.popBackStack();
        }
    }

    private class ModifySexListAdapter extends BaseAdapter{

        private Context context;
        private List<Map<String, Object>> dataSets;
        private LayoutInflater mInflater;
        private String sex;

        public ModifySexListAdapter(Context context, List<Map<String, Object>> dataSets, String sex) {
            this.context = context;
            this.dataSets = dataSets;
            this.mInflater = LayoutInflater.from(context);
            this.sex = sex;
        }
        @Override
        public int getCount() {
            return dataSets.size();
        }

        @Override
        public Object getItem(int position) {
            return dataSets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.select_sex_item, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_sex_selected);
                TextView textView = (TextView) convertView.findViewById(R.id.tv_sex_type);
                int gender = 0;
                if(sex.equals(Constant.MALE)) {
                    gender = 0;
                } else if (sex.equals(Constant.FEMALE)) {
                    gender = 1;
                } else if(sex.equals(Constant.SECRECY_SEX)) {
                    gender = 2;
                }
                if (position == gender) {
                    imageView.setImageResource(R.mipmap.sex_selected);
                }
                Map<String, Object> map = dataSets.get(position);
                textView.setText(map.get("tv_sex_type").toString());
            }
            return convertView;
        }
    }
}
