package com.oto.edyd;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.UpdateEnterprise;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.NetWork;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/9/14.
 */
public class AccountEnterpriseInformation extends Fragment implements View.OnClickListener{

    private LinearLayout enterpriseInformationBack; //返回
    private View view;
    private FragmentManager accountEnterFragmentManager;

    private LinearLayout modifyEnterpriseInformationBack;

    private ListView enterpriseList; //ListView
    private String[] enterpriseTitle; //标题资源
    private List<String> enterpriseContent = new ArrayList<String>(); //内容资源
    private ListView enterpriseListSec; //第二个ListView
    private String[] enterpriseTitleSec; //第二个ListView标题资源
    private int idResources[];
    List<Map<String, Object>> dataSets = new ArrayList<Map<String, Object>>(); //第一个List资源
    List<Map<String, Object>> dataSetsSec = new ArrayList<Map<String, Object>>(); //第二个List资源
    private UpdateEnterprise updateEnterprise = new UpdateEnterprise();

    private int confirmStatus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.enterprise_information, null);
        initFields(view);

        requestEnterInfoData();
        enterpriseInformationBack.setOnClickListener(this);
        enterpriseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateEnterprise.setTitle(enterpriseTitle[position]);
                updateEnterprise.setPosition(position);
                updateEnterprise.setContent(enterpriseContent.get(position));
                ModifyEnterInfo modifyEnterInfo = ModifyEnterInfo.newInstance(updateEnterprise, "first");
                FragmentTransaction transaction = accountEnterFragmentManager.beginTransaction();
                switch (position) {
                    case 0:
                        if (confirmStatus == 2) {
                            Toast.makeText(getActivity(), "企业已认证不能修改", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        transaction.replace(R.id.common_frame, modifyEnterInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 1:
                        transaction.replace(R.id.common_frame, modifyEnterInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 2:
                        transaction.replace(R.id.common_frame, modifyEnterInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 3:
                        transaction.replace(R.id.common_frame, modifyEnterInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 4:
                        transaction.replace(R.id.common_frame, modifyEnterInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 5:
                        transaction.replace(R.id.common_frame, modifyEnterInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 6:
                        Toast.makeText(getActivity(), "认证状态不能修改", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        enterpriseListSec.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ModifyEnterInfo modifyEnterInfo = ModifyEnterInfo.newInstance(updateEnterprise, "second");
                FragmentTransaction transaction = accountEnterFragmentManager.beginTransaction();
                switch (position) {
                    case 0:
                        transaction.replace(R.id.common_frame, modifyEnterInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                }
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enterprise_information_back: //返回
                getActivity().finish();
                break;
        }
    }

    /**
     * 初始化字段
     */
    private void initFields(View view) {
        enterpriseInformationBack = (LinearLayout) view.findViewById(R.id.enterprise_information_back);
        enterpriseList = (ListView) view.findViewById(R.id.enterprise_list);
        enterpriseListSec = (ListView) view.findViewById(R.id.enterprise_list_sec);

        enterpriseTitle = this.getResources().getStringArray(R.array.enter_info_content);
        enterpriseTitleSec = this.getResources().getStringArray(R.array.enter_info_content_sec);

        idResources = new int[]{R.id.enterprise_info_title, R.id.enterprise_info_content};

        accountEnterFragmentManager = ((AccountInformationActivity)getActivity()).accountInformationFragmentManager;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 701:
                    SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), dataSets, R.layout.enterprise_information_list_item,
                            new String[]{"enterprise_info_title", "enterprise_info_content"}, idResources); //ListView适配器
                    enterpriseList.setAdapter(simpleAdapter);
                    SimpleAdapter simpleAdapterSec = new SimpleAdapter(getActivity(), dataSetsSec, R.layout.enterprise_information_list_item,
                            new String[]{"enterprise_info_title", "enterprise_info_content"}, idResources); //ListView适配器
                    enterpriseListSec.setAdapter(simpleAdapterSec);
                    break;
            }
        }
    };

    private void requestEnterInfoData() {

        NetWork netWork = new NetWork(getActivity());
        if(!netWork.isHaveInternet()) {
            Toast.makeText(getActivity(), Constant.NOT_INTERNET_CONNECT, Toast.LENGTH_SHORT).show();
            return;
        }
        Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        //String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String url = Constant.ENTRANCE_PREFIX + "inquireEnterpriseDetail.json?sessionUuid="+sessionUuid;
        OkHttpClientManager.getAsyn(url, new EnterInfoResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                JSONObject enterInfoJson;
                JSONArray enterInfoArray;

                try {
                    enterInfoJson = new JSONObject(response);
                    String status = enterInfoJson.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "获取企业信息失败", Toast.LENGTH_SHORT).show();
                    }
                    enterInfoArray = enterInfoJson.getJSONArray("rows");
                    JSONObject enterInfoDetailJson = enterInfoArray.getJSONObject(0);
                    enterpriseContent.clear(); //清除缓存数据
                    enterpriseContent.add(enterInfoDetailJson.getString("enterpriseName"));//企业名称
                    enterpriseContent.add(enterInfoDetailJson.getString("enMobile")); //企业联系人
                    enterpriseContent.add(enterInfoDetailJson.getString("contacts")); //联系人
                    enterpriseContent.add(enterInfoDetailJson.getString("conMobile")); //联系人电话
                    enterpriseContent.add(enterInfoDetailJson.getString("address")); //地址
                    enterpriseContent.add(enterInfoDetailJson.getString("intro")); //简介

                    updateEnterprise.setEnterpriseName(enterInfoDetailJson.getString("enterpriseName"));
                    updateEnterprise.setEnMobile(enterInfoDetailJson.getString("enMobile"));
                    updateEnterprise.setContacts(enterInfoDetailJson.getString("contacts"));
                    updateEnterprise.setConMobile(enterInfoDetailJson.getString("conMobile"));
                    updateEnterprise.setAddress(enterInfoDetailJson.getString("address"));
                    updateEnterprise.setIntro(enterInfoDetailJson.getString("intro"));

                    confirmStatus = enterInfoDetailJson.getInt("confirmStatus");
                    if(confirmStatus == 0) {
                        enterpriseContent.add("未认证"); //激活状态
                    } else if(confirmStatus == 1) {
                        enterpriseContent.add("认证失败");
                    } else if(confirmStatus == 2) {
                        enterpriseContent.add("已认证");
                    }
                    dataSets.clear(); //去除缓存数据
                    for(int i = 0 ; i < enterpriseContent.size(); i++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("enterprise_info_title", enterpriseTitle[i]);
                        map.put("enterprise_info_content", enterpriseContent.get(i));
                        dataSets.add(map);
                    }

                    dataSetsSec.clear();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("enterprise_info_title", "修改密码");
                    map.put("enterprise_info_content", "");
                    dataSetsSec.add(map);

                    Message message = new Message();
                    message.what = 701;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public abstract class EnterInfoResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
        }
    }
}
