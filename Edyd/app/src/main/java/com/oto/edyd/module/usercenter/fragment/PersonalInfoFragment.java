package com.oto.edyd.module.usercenter.fragment;

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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.ModifyPersonInfo;
import com.oto.edyd.ModifySex;
import com.oto.edyd.R;
import com.oto.edyd.module.usercenter.model.PersonalInfoItem;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能：个人信息
 * 文件名：com.oto.edyd.module.usercenter.fragment.PersonalInfoFragment.java
 * 创建时间：2016/1/27
 * 作者：yql
 */
public class PersonalInfoFragment extends Fragment implements View.OnClickListener {
    //---------------基本View控件-------------
    private LinearLayout back; //返回
    private ListView profileList; //listView
    //---------------变量-------------
    private Common common; //偏好文件
    private Context context; //上下文对象
    private FragmentManager eFragmentManager; //Fragment管理器
    private CusProgressDialog transitionDialog; //过度对话框
    private PersonalInfoAdapter adapter; //适配器
    private List<PersonalInfoItem> personalInfoItemList = new ArrayList<PersonalInfoItem>(); //list数据源
    private int confirmStatus; //认证状态
    private final static int HANDLER_PERSON_INFO_CODE = 0x10; //个人信息返回成功码

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.commom_profile, null);
        init(view);
        return view;
    }

    /**
     * 初始化数据
     * @param view 布局视图
     */
    private void init(View view) {
        initFields(view);
        initListener();
        requestPersonalInfo();
    }

    /**
     * 初始化字段
     * @param view 布局视图
     */
    private void initFields(View view) {
        context = getActivity();
        back = (LinearLayout) view.findViewById(R.id.back);
        profileList = (ListView) view.findViewById(R.id.common_profile_list);
        eFragmentManager = getActivity().getSupportFragmentManager();
        common = new Common(context.getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                PersonalInfoItem personalInfoItem = personalInfoItemList.get(position);
//                ModifyPersonInfo modifyPersonInfo = ModifyPersonInfo.newInstance(personalInfoItem, "first");
//                FragmentTransaction transaction = eFragmentManager.beginTransaction();
//                switch (position) {
//                    case 0:
//                        common.showToast(context, "手机号码不能修改");
//                        break;
//                    case 1:
//                        if (confirmStatus == 2) {
//                            common.showToast(context, "用户已认证不能修改");
//                            return;
//                        }
//                        transaction.replace(R.id.common_frame, modifyPersonInfo);
//                        transaction.addToBackStack(null);
//                        transaction.commit();
//                        break;
//                    case 2:
//                        ModifySex modifySex = ModifySex.newInstance(personalInfoItem);
//                        transaction.replace(R.id.common_frame, modifySex);
//                        transaction.addToBackStack(null);
//                        transaction.commit();
//                        break;
//                    case 3:
//                        transaction.replace(R.id.common_frame, modifyPersonInfo);
//                        transaction.addToBackStack(null);
//                        transaction.commit();
//                        break;
//                    case 4:
//                        Toast.makeText(getActivity(), "认证状态不能修改", Toast.LENGTH_SHORT).show();
//                        break;
//                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                eFragmentManager.popBackStack();
                getActivity().finish();
                break;
        }
    }
    /**
     * 请求个人信息数据
     */
    private void requestPersonalInfo() {
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String enterprisesId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String url = Constant.ENTRANCE_PREFIX_v1 + "appGetPersonInfo.json?sessionUuid="+sessionUuid+"&enterprisesId="+enterprisesId;

        OkHttpClientManager.getAsyn(url, new PersonalInfoResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "个人信息获取异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                JSONObject item;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "个人信息获取失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    jsonArray = jsonObject.getJSONArray("rows");
                    item = jsonArray.getJSONObject(0);
                    PersonalInfoBean personalInfoBean = new PersonalInfoBean();
                    personalInfoBean.setMobilePhoneNumber(common.getStringByKey(Constant.USER_NAME)); //手机号码
                    personalInfoBean.setAliasName(item.getString("aliasName")); //昵称
                    int gender = item.getInt("sex"); //性别
                    switch (gender) {
                        case 0:
                            personalInfoBean.setSex("未知");
                            break;
                        case 1:
                            personalInfoBean.setSex("男");
                            break;
                        case 2:
                            personalInfoBean.setSex("女");
                            break;
                        case 3:
                            personalInfoBean.setSex("保密");
                            break;
                    }
                    personalInfoBean.setBirthday(item.getString("birthday")); //生日
                    confirmStatus = item.getInt("authenticationStatus"); //认证状态
                    switch (confirmStatus) {
                        case 0:
                            personalInfoBean.setAuthenticationStatus(Constant.NOT_AUTHENTICATED);
                            break;
                        case 1:
                            personalInfoBean.setAuthenticationStatus(Constant.HAS_BEEN_AUTHENTICATED);
                            break;
                        case 2:
                            personalInfoBean.setAuthenticationStatus(Constant.FAIL_AUTHENTICATED);
                            break;
                    }

                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("personalInfoBean", personalInfoBean); //传递序列号对象
                    message.what = HANDLER_PERSON_INFO_CODE;
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private abstract class PersonalInfoResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            //请求之前操作
            transitionDialog = new CusProgressDialog(context, "正在加载...");
            transitionDialog.showDialog();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            transitionDialog.dismissDialog();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_PERSON_INFO_CODE: //个人信息成功返回码
                    Bundle bundle = msg.getData();
                    PersonalInfoBean personalInfoBean = (PersonalInfoBean) bundle.getSerializable("personalInfoBean");
                    String personTitle[] = getResources().getStringArray(R.array.personal_info_title);
                    String personContent[] = new String[] {personalInfoBean.getMobilePhoneNumber(), personalInfoBean.getAliasName(), personalInfoBean.getSex(), personalInfoBean.getBirthday(), personalInfoBean.getAuthenticationStatus()};
                    for(int i = 0; i < personTitle.length; i++) {
                        PersonalInfoItem infoItem = new PersonalInfoItem();
                        infoItem.setTitle(personTitle[i]);
                        infoItem.setContent(personContent[i]);
                        personalInfoItemList.add(infoItem);
                    }
                    adapter = new PersonalInfoAdapter(context);
                    profileList.setAdapter(adapter);
                    break;
            }
        }
    };

    private class PersonalInfoAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public PersonalInfoAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return personalInfoItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return personalInfoItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.commom_profile_item, null);
            PersonalInfoItem personalInfoItem = personalInfoItemList.get(position);
            TextView profileTitle = (TextView) view.findViewById(R.id.common_profile_title);
            TextView contentTitle = (TextView) view.findViewById(R.id.common_profile_content);
            profileTitle.setText(personalInfoItem.getTitle());
            contentTitle.setText(personalInfoItem.getContent());
            return view;
        }
    }

    /**
     * 个人信息实体
     */
    private class PersonalInfoBean implements Serializable {
        private String mobilePhoneNumber; //手机号
        private String aliasName; //昵称
        private String authenticationStatus; //认证状态
        private String birthday; //生日
        private String sex; //性别

        public String getMobilePhoneNumber() {
            return mobilePhoneNumber;
        }

        public void setMobilePhoneNumber(String mobilePhoneNumber) {
            this.mobilePhoneNumber = mobilePhoneNumber;
        }

        public String getAliasName() {
            return aliasName;
        }

        public void setAliasName(String aliasName) {
            this.aliasName = aliasName;
        }

        public String getAuthenticationStatus() {
            return authenticationStatus;
        }

        public void setAuthenticationStatus(String authenticationStatus) {
            this.authenticationStatus = authenticationStatus;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }
    }
}
