package com.oto.edyd;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.module.usercenter.activity.AccountInformationActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yql on 2015/9/11.
 */
public class UpdatePasswordFragment extends Fragment implements View.OnClickListener{

    private View updatePasswordView;
    private LinearLayout updatePasswordBack; //返回
    private EditText etOldPassword; //旧密码
    private EditText etNewPassword; //新密码
    private EditText etConfirmPassword; //确认密码
    private TextView btSave; //保存
    private FragmentManager AccountFragmentManager; //LoginActivity布局管理器
    private CusProgressDialog updatePasswordDialog; //过度画面

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        updatePasswordView = inflater.inflate(R.layout.update_password, null);
        initFields(updatePasswordView);

        updatePasswordBack.setOnClickListener(this);
        btSave.setOnClickListener(this);

        return updatePasswordView;
    }

    private void initFields(View view) {
        updatePasswordBack = (LinearLayout) view.findViewById(R.id.update_password_back);
        etOldPassword = (EditText) view.findViewById(R.id.et_old_password);
        etNewPassword = (EditText) view.findViewById(R.id.et_new_password);
        etConfirmPassword = (EditText) view.findViewById(R.id.et_confirm_password);
        btSave = (TextView) view.findViewById(R.id.personal_info_save);
        AccountFragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_password_back: //返回
                AccountFragmentManager.popBackStack();

                break;
            case R.id.personal_info_save: //保存
                updatePassword();
                break;
        }
    }

    private void updatePassword(){
        String oldPassword = etOldPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

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
            updatePasswordDialog = new CusProgressDialog(getActivity(), "正在修改...");
            updatePasswordDialog.getLoadingDialog().show();
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            updatePasswordDialog.getLoadingDialog().dismiss();
        }
    }
}
