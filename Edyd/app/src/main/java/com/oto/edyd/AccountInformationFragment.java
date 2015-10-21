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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.UpdatePerson;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
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
 * Created by yql on 2015/9/11.
 */
public class AccountInformationFragment extends Fragment implements View.OnClickListener{

    private View personalInfoView;
    private LinearLayout personalInformationBack; //返回
    public FragmentManager accountInformationFragmentManager; //布局管理器

    private ListView personList; //第一个ListView
    private ListView personListSec; //第二个ListView

    private Common common; //操作偏好设置
    private UpdatePerson updatePerson;
    private int confirmStatus;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        personalInfoView = inflater.inflate(R.layout.personal_information, null);
        initFields(personalInfoView);
        requestData();
        personList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView curTitle = (TextView)view.findViewById(R.id.enterprise_info_title);
                TextView curContent = (TextView)view.findViewById(R.id.enterprise_info_content);
                updatePerson.setTitle(curTitle.getText().toString());
                updatePerson.setPosition(position);
                updatePerson.setContent(curContent.getText().toString());
                ModifyPersonInfo modifyPersonInfo = ModifyPersonInfo.newInstance(updatePerson, "first");
                FragmentTransaction transaction = accountInformationFragmentManager.beginTransaction();
                switch (position) {
                    case 0:
                        Toast.makeText(getActivity(), "手机号码不能修改", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        if (confirmStatus == 2) {
                            Toast.makeText(getActivity(), "用户已认证不能修改", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        transaction.replace(R.id.common_frame, modifyPersonInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 2:
                        ModifySex modifySex = ModifySex.newInstance(updatePerson);
                        transaction.replace(R.id.common_frame, modifySex);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 3:
                        transaction.replace(R.id.common_frame, modifyPersonInfo);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 4:
                        Toast.makeText(getActivity(), "认证状态不能修改", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        personListSec.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ModifyPersonInfo modifyPersonInfo = ModifyPersonInfo.newInstance(updatePerson, "second");
                        FragmentTransaction transaction = accountInformationFragmentManager.beginTransaction();
                        switch (position) {
                            case 0:
                                transaction.replace(R.id.common_frame, modifyPersonInfo);
                                transaction.addToBackStack(null);
                                transaction.commit();
                                break;
                        }
                        break;
                }
            }
        });
        personalInformationBack.setOnClickListener(this);
        return personalInfoView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.personal_information_back:
                getActivity().finish();
                break;
        }
    }

    private void initFields(View view) {
        personalInformationBack = (LinearLayout)view.findViewById(R.id.personal_information_back);
        personList = (ListView)view.findViewById(R.id.personal_list);
        personListSec = (ListView)view.findViewById(R.id.personal_list_sec);

        accountInformationFragmentManager = ((AccountInformationActivity)getActivity()).accountInformationFragmentManager;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    List<Map<String, String>> dateSets = new ArrayList<Map<String, String>>();
                    Bundle bundle = msg.getData();
                    updatePerson = (UpdatePerson)bundle.get("updatePerson");
                    String personContent[] = new String[] {updatePerson.getPhoneNumber(), updatePerson.getNikeName(), updatePerson.getSex(), updatePerson.getBirthday(), updatePerson.getConfirmStatus()};
                    String personTitle[] = getResources().getStringArray(R.array.personal_info_title);
                    int resourcesID[] = new int[] {R.id.enterprise_info_title, R.id.enterprise_info_content};

                    for(int i = 0; i < personContent.length; i++) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("enterprise_info_title", personTitle[i]);
                        map.put("enterprise_info_content", personContent[i]);
                        dateSets.add(map);
                    }
                    SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), dateSets, R.layout.enterprise_information_list_item, new String[]{"enterprise_info_title", "enterprise_info_content"},resourcesID);
                    personList.setAdapter(simpleAdapter);

                    List<Map<String, String>> dateSetsSec = new ArrayList<Map<String, String>>();
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("enterprise_info_title", "修改密码");
                    map.put("enterprise_info_content", "");
                    dateSetsSec.add(map);
                    String personTitleSec[] = getResources().getStringArray(R.array.enter_info_content_sec);
                    SimpleAdapter simpleAdapterSec = new SimpleAdapter(getActivity(), dateSetsSec, R.layout.modify_password_item,
                            new String[]{"enterprise_info_title", "enterprise_info_content"}, resourcesID); //ListView适配器
                    personListSec.setAdapter(simpleAdapterSec);
                    break;
            }
        }
    };

    private void requestData(){
        common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterprisesId = common.getStringByKey(Constant.ENTERPRISE_ID);

        String url = Constant.ENTRANCE_PREFIX + "inquirePersonDetail.json?sessionUuid="+sessionUuid+"&enterprisesId="+enterprisesId;
        OkHttpClientManager.getAsyn(url, new LoginResultCallback<String>() {
            JSONObject accountInfoJson;
            JSONArray accountInfoArray;

            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(String response) {

                try {
                    accountInfoJson = new JSONObject(response);
                    String status = accountInfoJson.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "个人信息获取失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    accountInfoArray = accountInfoJson.getJSONArray("rows");
                    JSONObject rowsAccountInfoJson = accountInfoArray.getJSONObject(0);
                    //tvBirthday.setText(rowsAccountInfoJson.getString("birthday"));
                    UpdatePerson updatePerson = new UpdatePerson();
                    updatePerson.setPhoneNumber(common.getStringByKey(Constant.USER_NAME)); //手机号码
                    updatePerson.setNikeName(rowsAccountInfoJson.getString("nickName")); //用户名

                    int gender = rowsAccountInfoJson.getInt("gender"); //性别
                    if (gender == 0) {
                        updatePerson.setSex("未知");
                    } else if (gender == 1) {
                        updatePerson.setSex("男");
                    } else if (gender == 2) {
                        updatePerson.setSex("女");
                    } else if (gender == 3) {
                        updatePerson.setSex("保密");
                    }
                    updatePerson.setBirthday(rowsAccountInfoJson.getString("birthday")); //生日
                    confirmStatus = rowsAccountInfoJson.getInt("confirmStatus"); //认证状态
                    if (confirmStatus == 0) {
                        updatePerson.setConfirmStatus(Constant.NOT_AUTHENTICATED);
                    } else if (confirmStatus == 1) {
                        updatePerson.setConfirmStatus(Constant.HAS_BEEN_AUTHENTICATED);
                    } else if (confirmStatus == 2) {
                        updatePerson.setConfirmStatus(Constant.FAIL_AUTHENTICATED);
                    }
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("updatePerson", updatePerson); //传递序列号对象
                    message.what = 1;
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public abstract class LoginResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            //loadingDialog = new CusProgressDialog(getActivity(), "正在登录...");
            //loadingDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            //loadingDialog.getLoadingDialog().dismiss();
        }
    }
}
