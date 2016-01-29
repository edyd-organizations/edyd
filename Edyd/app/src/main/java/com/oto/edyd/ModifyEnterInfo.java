package com.oto.edyd;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.UpdateEnterprise;
import com.oto.edyd.module.usercenter.activity.AccountInformationActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yql on 2015/9/14.
 */
public class ModifyEnterInfo extends Fragment implements View.OnClickListener {

    private View view;
    private LinearLayout modifyEnterInfoBack; //返回
    private TextView modifyEnterInfoTitle; //标题
    private TextView modifyEnterSave; //保存
    private EditText enterInfoItem;
    private FragmentManager accountEnterFragmentManager;

    private View updatePasswordView;
    private LinearLayout updatePasswordBack; //返回
    private EditText etOldPassword; //旧密码
    private EditText etNewPassword; //新密码
    private EditText etConfirmPassword; //确认密码
    private TextView btSave; //保存

    private int position;
    private UpdateEnterprise updateEnterprise;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null) {
            String order = bundle.getString("order");
            if(order.equals("first")) {
                view = inflater.inflate(R.layout.common_enter_info, null);
                initFields(view, order);
                updateEnterprise = (UpdateEnterprise)bundle.getSerializable("updateEnterprise");
                position = updateEnterprise.getPosition();
                String title = updateEnterprise.getTitle();
                String content = updateEnterprise.getContent();
                modifyEnterInfoTitle.setText(title);
                enterInfoItem.setText(content);
                enterInfoItem.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        modifyEnterSave.setEnabled(true);
                        modifyEnterSave.setTextColor(Color.WHITE);
                    }
                });
                modifyEnterInfoBack.setOnClickListener(this);
                modifyEnterSave.setOnClickListener(this);
            } else if(order.equals("second")) {
                view = inflater.inflate(R.layout.update_password, null);
                initFields(view, order);
                updatePasswordBack.setOnClickListener(this);
                btSave.setOnClickListener(this);
            }
        }
        return view;
    }

    private void initFields(View view, String order) {
        accountEnterFragmentManager = getActivity().getSupportFragmentManager();
        if(order.equals("first")) {
            modifyEnterInfoBack = (LinearLayout) view.findViewById(R.id.modify_enter_info_back);
            modifyEnterInfoTitle = (TextView) view.findViewById(R.id.tv_enter_info_title);
            modifyEnterSave = (TextView) view.findViewById(R.id.bt_enter_info_save);
            enterInfoItem = (EditText) view.findViewById(R.id.enter_info_item);
        } else if(order.equals("second")) {
            updatePasswordBack = (LinearLayout) view.findViewById(R.id.update_password_back);
            etOldPassword = (EditText) view.findViewById(R.id.et_old_password);
            etNewPassword = (EditText) view.findViewById(R.id.et_new_password);
            etConfirmPassword = (EditText) view.findViewById(R.id.et_confirm_password);
            btSave = (TextView) view.findViewById(R.id.personal_info_save);
        }
    }

    public static ModifyEnterInfo newInstance(String title, String content, int position) {
        ModifyEnterInfo modifyEnterInfo = new ModifyEnterInfo();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("content", content);
        args.putInt("position", position);
        modifyEnterInfo.setArguments(args);
        return modifyEnterInfo;
    }
    public static ModifyEnterInfo newInstance(UpdateEnterprise updateEnterprise, String order) {
        ModifyEnterInfo modifyEnterInfo = new ModifyEnterInfo();
        Bundle args = new Bundle();
        args.putString("order", order);
        args.putSerializable("updateEnterprise", updateEnterprise);
        modifyEnterInfo.setArguments(args);
        return modifyEnterInfo;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modify_enter_info_back:
                accountEnterFragmentManager.popBackStack();
                break;
            case R.id.bt_enter_info_save:
                saveModifyInfo();
                break;
            case R.id.update_password_back: //返回
                accountEnterFragmentManager.popBackStack();
                break;
            case R.id.personal_info_save: //保存
                updatePassword();
                break;
        }
    }

    /**
     * 保存修改信息
     */
    private void saveModifyInfo() {
        String url = "";
        String textContent = enterInfoItem.getText().toString();
        Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        switch (position) {
            case 0:
                url = Constant.ENTRANCE_PREFIX + "updateEnterprise.json?sessionUuid="+sessionUuid+"&enMobile"+"="+updateEnterprise.getEnMobile()+"&enterpriseName="+textContent
                        +"&contacts="+updateEnterprise.getContacts()+"&conMobile="+updateEnterprise.getConMobile()+"&address="+updateEnterprise.getAddress()+"&intro="+updateEnterprise.getIntro();
                break;
            case 1:
                url = Constant.ENTRANCE_PREFIX + "updateEnterprise.json?sessionUuid="+sessionUuid+"&enMobile"+"="+textContent+"&enterpriseName="+updateEnterprise.getEnterpriseName()
                +"&contacts="+updateEnterprise.getContacts()+"&conMobile="+updateEnterprise.getConMobile()+"&address="+updateEnterprise.getAddress()+"&intro="+updateEnterprise.getIntro();
                break;
            case 2:
                url = Constant.ENTRANCE_PREFIX + "updateEnterprise.json?sessionUuid="+sessionUuid+"&enMobile"+"="+updateEnterprise.getEnMobile()+"&enterpriseName="+updateEnterprise.getEnterpriseName()
                        +"&contacts="+textContent+"&conMobile="+updateEnterprise.getConMobile()+"&address="+updateEnterprise.getAddress()+"&intro="+updateEnterprise.getIntro();
                break;
            case 3:
                url = Constant.ENTRANCE_PREFIX + "updateEnterprise.json?sessionUuid="+sessionUuid+"&enMobile"+"="+updateEnterprise.getEnMobile()+"&enterpriseName="+updateEnterprise.getEnterpriseName()
                        +"&contacts="+updateEnterprise.getContacts()+"&conMobile="+textContent+"&address="+updateEnterprise.getAddress()+"&intro="+updateEnterprise.getIntro();
                break;
            case 4:
                url = Constant.ENTRANCE_PREFIX + "updateEnterprise.json?sessionUuid="+sessionUuid+"&enMobile"+"="+updateEnterprise.getEnMobile()+"&enterpriseName="+updateEnterprise.getEnterpriseName()
                        +"&contacts="+updateEnterprise.getContacts()+"&conMobile="+updateEnterprise.getConMobile()+"&address="+textContent+"&intro="+updateEnterprise.getIntro();
                break;
            case 5:
                url = Constant.ENTRANCE_PREFIX + "updateEnterprise.json?sessionUuid="+sessionUuid+"&enMobile"+"="+updateEnterprise.getEnMobile()+"&enterpriseName="+updateEnterprise.getEnterpriseName()
                        +"&contacts="+updateEnterprise.getContacts()+"&conMobile="+updateEnterprise.getConMobile()+"&address="+updateEnterprise.getAddress()+"&intro="+textContent;
                break;
        }
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
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "修改信息失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    accountEnterFragmentManager.popBackStack();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 拼接合适的URL返回
     * @param sessionUuid
     * @param fields
     * @param content
     * @return
     */
    private String spliceString(String sessionUuid, String fields, String content, String enterpriseName) {
        return Constant.ENTRANCE_PREFIX + "updateEnterprise.json?sessionUuid="+sessionUuid+"&"+fields+"="+content+"&enterpriseName="+enterpriseName;
    }

    private void updatePassword(){
        String oldPassword = etOldPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if(oldPassword != null && oldPassword.equals("")){
            Toast.makeText(getActivity(), "旧密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(newPassword != null && newPassword.equals("")){
            Toast.makeText(getActivity(), "新密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(confirmPassword != null && confirmPassword.equals("")){
            Toast.makeText(getActivity(), "确认密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!newPassword.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "updatePassword.json?newPassword="+newPassword+"&oldPassword="+oldPassword+"&sessionUuid=" + sessionUuid;
        OkHttpClientManager.getAsyn(url, new UpdatePasswordResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public abstract class UpdatePasswordResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
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
