package com.oto.edyd;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.model.UpdatePerson;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yql on 2015/9/14.
 */
public class ModifyPersonInfo extends Fragment implements View.OnClickListener {

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
    private Common common;

    private int position;
    private UpdatePerson updatePerson;
    private final static int HANDLER_SAVE_PASSWORD_CODE = 0x10; //修改密码返回成功

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle != null) {
            String order = bundle.getString("order");
            if(order.equals("first")) {
                view = inflater.inflate(R.layout.common_enter_info, null);
                initFields(view, order);
                updatePerson = (UpdatePerson)bundle.getSerializable("updatePerson");
                position = updatePerson.getPosition();

                String title = updatePerson.getTitle();
                String content = updatePerson.getContent();
                if(position == 3) { //焦点为生日时
                    enterInfoItem.setInputType(InputType.TYPE_NULL);
                    enterInfoItem.setFocusable(true);
                    enterInfoItem.setFocusableInTouchMode(true);
                    enterInfoItem.requestFocus();
                    if(content != null && content.equals("")){
                        alertDatePicker();
                    }
                }
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
                enterInfoItem.setOnClickListener(this);
//                enterInfoItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        alertDatePicker();
//                    }
//                });
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
            common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        }
    }

    public static ModifyPersonInfo newInstance(UpdatePerson updatePerson, String order) {
        ModifyPersonInfo modifyEnterInfo = new ModifyPersonInfo();
        Bundle args = new Bundle();
        args.putString("order", order);
        args.putSerializable("updatePerson", updatePerson);
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
            case  R.id.enter_info_item: //日期
                //用来获取日期和时间的
                alertDatePicker();
                break;
        }
    }

    /**
     * 保存修改信息
     */
    private void saveModifyInfo() {
        String url = "";
        int gender = 0;
        String textContent = enterInfoItem.getText().toString();
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String sex = updatePerson.getSex();
        if(sex.equals(Constant.UNKNOWN_SEX)) {
            gender = 0;
        } else if (sex.equals(Constant.MALE)) {
            gender = 1;
        } else if(sex.equals(Constant.FEMALE)) {
            gender = 2;
        } else if(sex.equals(Constant.SECRECY_SEX)) {
            gender = 3;
        }
        switch (position) {
            case 1:
                url = Constant.ENTRANCE_PREFIX + "updatePerson.json?birthday="+updatePerson.getBirthday()+"&gender="+gender+"&nickName="+textContent+"&sessionUuid="+sessionUuid;
                break;
            case 2:
                url = Constant.ENTRANCE_PREFIX + "updatePerson.json?birthday="+updatePerson.getBirthday()+"&gender="+gender+"&nickName="+updatePerson.getNikeName()+"&sessionUuid="+sessionUuid;
                break;
            case 3:
                url = Constant.ENTRANCE_PREFIX + "updatePerson.json?birthday="+textContent+"&gender="+gender+"&nickName="+updatePerson.getNikeName()+"&sessionUuid="+sessionUuid;
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
            String sOldPassword = common.getStringByKey(Constant.PASSWORD);
            if (oldPassword.equals(sOldPassword)) {
                Toast.makeText(getActivity(), "旧密码输入错误", Toast.LENGTH_SHORT).show();
                return;
            }
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


        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX + "updatePassword.json?newPassword="+newPassword+"&oldPassword="+oldPassword+"&sessionUuid=" + sessionUuid;
        OkHttpClientManager.getAsyn(url, new UpdatePasswordResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {

            }
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        Toast.makeText(getActivity(), "修改失败", Toast.LENGTH_SHORT).show();
                    } else {
                        jsonArray = jsonObject.getJSONArray("rows");
                        String sessionUUid = jsonArray.getJSONObject(0).getString("sessionUuid");
                        Map<Object, Object> map = new HashMap<Object, Object>();
                        map.put(Constant.SESSION_UUID, sessionUUid);
                        if(!common.isSave(map)) {
                            Toast.makeText(getActivity(), "用户表示更新失败", Toast.LENGTH_SHORT).show();
                        }
                        Message message = Message.obtain();
                        message.what = HANDLER_SAVE_PASSWORD_CODE;
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 检查旧密码
     */
    private void checkOldPassword() {
        String oldPassword = etOldPassword.getText().toString();
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_SAVE_PASSWORD_CODE: //修改密码成功返回
                    Toast.makeText(getActivity(), "修改成功", Toast.LENGTH_SHORT).show();
                    accountEnterFragmentManager.popBackStack();
                    break;
            }
        }
    };

    private void alertDatePicker() {
        //用来获取日期和时间的
        String dataStr = enterInfoItem.getText().toString();
        Date date = null;
        if(dataStr == null){
            date = new Date();
        } else {
            if((dataStr.trim()).equals("")){
                date = new Date();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    date = sdf.parse(dataStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                enterInfoItem.setText(year + "-" + (monthOfYear + 1) + "-"+dayOfMonth);
            }
        },calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
