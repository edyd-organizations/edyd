package com.oto.edyd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.oto.edyd.module.common.activity.NoticeActivity;
import com.oto.edyd.module.usercenter.activity.*;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：主Activity侧滑界面
 * 文件名：com.oto.edyd.LeftSlidingFragment.java
 * 创建时间：2015/12/14
 * 作者：yql
 */
public class LeftSlidingFragment extends Fragment implements View.OnClickListener{
    //----------------基础View控件-----------------
    private ListView listView; //左侧划ListView
    private LinearLayout userLogin; //用户登入
    public LinearLayout exit; //退出登入
    public TextView userAlias; //用户名
    public TextView accountType; //账户类型
    public TextView roleType; //角色类型
    public View slidingBottomLine; //侧滑底部线条
    //----------------变量-----------------
    List<SlideInnerMessage> slideInnerMessageList = new ArrayList<SlideInnerMessage>(); //侧边栏ListView数据源
    private Common common; //共享文件 LOGIN_PREFERENCES_FILE
    private SimpleAdapter simpleAdapter; //ListView适配器
    private Context context; //上下文对象


    //ListView资源
    public String[] textResources; //文字资源
    public int[] imageResources; //图片资源
    public int[] idResources; //ID资源

    private CusProgressDialog exitProgressDialog;
    private Intent intent;

    public List<Map<String, Object>> dataSets = new ArrayList<Map<String, Object>>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.left_sliding, null); //左侧滑布局文件
        init(view); //数据初始化
        return view;
    }

    /**
     * 初始化数据
     */
    private void init(View view) {
        initField(view); //初始化字段
        initListener(); //初始化监听器
        initLeftMenuInfo(); //初始化侧边信息
    }

    /**
     * 初始化数据
     */
    private void initField(View view) {
        listView = (ListView)view.findViewById(R.id.left_sliding_list);
        userLogin = (LinearLayout)view.findViewById(R.id.user_login);
        userAlias = (TextView) view.findViewById(R.id.user_alias);
        accountType = (TextView) view.findViewById(R.id.account_type);
        roleType = (TextView) view.findViewById(R.id.role_type);
        exit = (LinearLayout)view.findViewById(R.id.exit);
        slidingBottomLine =  view.findViewById(R.id.sliding_bottom_line);
        idResources = new int[]{R.id.list_image, R.id.list_text, R.id.list_arrow};
        context = getActivity();
        common = new Common(context.getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        //判断是否登录，加载不同的菜单资源
        if(common.isLogin()) {
            textResources = context.getResources().getStringArray(R.array.left_sliding_list_string);
            imageResources = new int[]{R.mipmap.my_purse, R.mipmap.select_user_type, R.mipmap.notice,
                    R.mipmap.share, R.mipmap.setting};
        } else {
            textResources = context.getResources().getStringArray(R.array.no_login_left_sliding_list_string);
            imageResources = new int[]{R.mipmap.share, R.mipmap.setting};
        }
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        userLogin.setOnClickListener(this);
        exit.setOnClickListener(this);
        listView.setOnItemClickListener(new SlidingListItemOnClickListener());
    }

    /**
     * 初始化侧边栏信息
     */
    private void initLeftMenuInfo() {
        //是否登入控制退出按钮是否可用,以及登录后更新侧边栏信息
        if(common.isLogin()) {
            userAlias.setText(common.getStringByKey(Constant.USER_NAME));
            exit.setVisibility(View.VISIBLE);
            slidingBottomLine.setVisibility(View.VISIBLE);
            String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            String txRoleType = common.getStringByKey(Constant.ROLE_NAME);
            accountType.setText(enterpriseName);
            roleType.setText(txRoleType);
        } else {
            exit.setVisibility(View.GONE);
            slidingBottomLine.setVisibility(View.GONE);
        }
        updateDataSource(); //更新数据
    }

    /**
     * 更新数据源
     */
    private void updateDataSource() {
        dataSets.clear();
        for(int i = 0; i < textResources.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("list_image", imageResources[i]);
            map.put("list_text", textResources[i]);
            map.put("list_arrow", R.mipmap.right_arrow);
            dataSets.add(map);
        }
        simpleAdapter = new SimpleAdapter(context,
                dataSets,
                R.layout.left_sliding_item,
                new String[]{"list_image", "list_text", "list_arrow"},
                idResources);
        listView.setAdapter(simpleAdapter); //设置适配器
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_login: //用户登入
                if(common.isLogin()){
                    //用户已登入跳转到个人详细信息
                    Intent intent = new Intent(context, AccountInformationActivity.class);
                    startActivity(intent);
                } else {
                    //用户未登入跳转到登入页面
                    //Toast.makeText(getActivity().getApplicationContext(), "用户未登入", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE); //启动另外一个状态码
                }
                break;
            case R.id.exit: //退出登录
                Common commonTrans = new Common(context.getSharedPreferences(Constant.GLOBAL_FILE, Context.MODE_PRIVATE));
                Common commonFixed = new Common(context.getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
                exitProgressDialog = new CusProgressDialog(context, "正在退出...");
                exitProgressDialog.getLoadingDialog().show();
                if(!common.isClearAccount()) {
                    common.showToast(context, "清除账户异常");
                    return;
                }
                if(!commonTrans.isClearAccount()) {
                    common.showToast(context, "清除司机信息异常");
                    return;
                }
                Map<Object, Object> map = new HashMap<Object, Object>();
                map.put(Constant.TRANSPORT_ROLE, Constant.DRIVER_ROLE_ID);
                map.put(Constant.TYPE_CODE, "");
                if(!commonFixed.isSave(map)) {
                    common.showToast(context, "初始化司机");
                    return;
                }
                ((MainActivity)context).exitOperate(); //退出操作
                new Thread(new ExitDialogThread(exitProgressDialog)).start(); //弹出对话框线程
                break;
            default:
                break;
        }
    }

    private class SlidingListItemOnClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView textView = (TextView) view.findViewById(R.id.list_text);
            String menuDes = textView.getText().toString();
            if(menuDes.equals("我的钱包")) {
                intent = new Intent(context, WaitBuild.class);
                intent.putExtra("wait_title", "我的钱包");
                startActivity(intent);
            } else if(menuDes.equals("账户类型")){
                intent = new Intent(context, com.oto.edyd.module.usercenter.activity.AccountTypeActivity.class);
                startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE);
            } else if(menuDes.equals("公告通知")){
                //intent = new Intent(getActivity().getApplicationContext(), WaitBuild.class);
                //intent.putExtra("wait_title", "公告通知");
                intent = new Intent(context, NoticeActivity.class);
                startActivity(intent);
            } else if(menuDes.equals("社交分享")){
                intent = new Intent(context, SocialSharedActivity.class);
                intent.putExtra("wait_title", "社交分享");
                startActivity(intent);
            } else if(menuDes.equals("系统设置")){
                intent = new Intent(context, SetUpActivity.class);
                startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE);
            }
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.loginThreadResultCode: //退出登录
                    userAlias.setText("未登录");
                    accountType.setText("");
                    roleType.setText("");
                    exit.setVisibility(View.GONE);
                    slidingBottomLine.setVisibility(View.GONE);
                    textResources = context.getResources().getStringArray(R.array.no_login_left_sliding_list_string);
                    imageResources = new int[]{R.mipmap.share, R.mipmap.setting};
                    updateDataSource();
                    simpleAdapter.notifyDataSetChanged();
                    exitProgressDialog.dismissDialog();
                    common.showToast(context, "退出成功");
                    break;
            }
        }
    };

    /**
     * 对话框线程类
     */
    private class ExitDialogThread implements Runnable {
        private CusProgressDialog exitProgressDialog;

        public ExitDialogThread(CusProgressDialog exitProgressDialog) {
            this.exitProgressDialog = exitProgressDialog;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
                Message message = new Message();
                message.what = 700;
                handler.sendMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 适配数据
     * @param userName 登录名
     */
    public void adapterData(String userName) {
        userAlias.setText(userName);
        exit.setVisibility(View.VISIBLE);
        slidingBottomLine.setVisibility(View.VISIBLE);
        accountType.setText("个人");
        textResources = context.getResources().getStringArray(R.array.left_sliding_list_string);
        imageResources = new int[]{R.mipmap.my_purse, R.mipmap.select_user_type, R.mipmap.notice,
                R.mipmap.share, R.mipmap.setting};
        updateDataSource();
        simpleAdapter.notifyDataSetChanged();
    }

    /**
     * 侧边栏资源实体类
     */
    private class SlideInnerMessage{
        public String textResources; //文字资源
        public int imageResources; //图片资源
        public int idResources; //ID资源
    }
}
