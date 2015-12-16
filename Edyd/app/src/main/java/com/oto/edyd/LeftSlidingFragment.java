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
import android.widget.Toast;

import com.oto.edyd.usercenter.activity.LoginActivity;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yql on 2015/8/25.
 * 主界面左侧滑
 */
public class LeftSlidingFragment extends Fragment implements View.OnClickListener{

    private View leftSlidingView; //取得左侧滑布局文件
    private ListView leftSlidingListView; //左侧划ListView
    private LinearLayout userLogin; //用户登入
    public LinearLayout exit; //退出登入
    public TextView userAlias;
    public TextView accountType; //账户类型
    public TextView roleType; //角色类型
    public View slidingBottomLine; //侧滑底部线条

    //ListView资源
    public String[] textResources; //文字资源
    public int[] imageResources; //图片资源
    public int[] idResources; //ID资源

    private boolean isRuning = true;
    private CusProgressDialog exitProgressDialog;
    private Intent intent;
    public SimpleAdapter simpleAdapter;
    public List<Map<String, Object>> dataSets = new ArrayList<Map<String, Object>>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        leftSlidingView = inflater.inflate(R.layout.left_sliding, null);
        initField(leftSlidingView);

        dataSets.clear();

        for(int i = 0; i < textResources.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("list_image", imageResources[i]);
            map.put("list_text", textResources[i]);
            map.put("list_arrow", R.mipmap.right_arrow);
            dataSets.add(map);
        }
        simpleAdapter = new SimpleAdapter(getActivity().getApplicationContext(), dataSets, R.layout.left_sliding_item,
                new String[]{"list_image", "list_text", "list_arrow"}, idResources); //ListView适配器
        leftSlidingListView.setAdapter(simpleAdapter); //设置适配器

        /**
         * 是否登入控制退出按钮是否可用,以及登录后更新侧边栏信息
         */
        if(isLogin()) {
            Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
            String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
            if(!sessionUuid.equals("")) {
                userAlias.setText(common.getStringByKey(Constant.USER_NAME));
                exit.setVisibility(View.VISIBLE);
                slidingBottomLine.setVisibility(View.VISIBLE);
            }
            String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
            String txRoleType = common.getStringByKey(Constant.ROLE_NAME);
            if(enterpriseName != null) {
                accountType.setText(enterpriseName);
                roleType.setText(txRoleType);
            }
        }else {
            dataSets.remove(0);
            dataSets.remove(0);
            simpleAdapter.notifyDataSetChanged();
            exit.setVisibility(View.GONE);
            slidingBottomLine.setVisibility(View.GONE);
        }

        userLogin.setOnClickListener(this);
        exit.setOnClickListener(this);
        leftSlidingListView.setOnItemClickListener(new SlidingListItemOnClickListener());
        return leftSlidingView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_login: //用户登入
                if(isLogin()){
                    //用户已登入跳转到个人详细信息
                    Intent intent = new Intent(getActivity(), AccountInformationActivity.class);
                    startActivity(intent);
                } else {
                    //用户未登入跳转到登入页面
                    //Toast.makeText(getActivity().getApplicationContext(), "用户未登入", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE); //启动另外一个状态码
                }
                break;
            case R.id.exit: //退出登录
                Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
                Common commonTrans = new Common(getActivity().getSharedPreferences(Constant.GLOBAL_FILE, Context.MODE_PRIVATE));
                Common commonFixed = new Common(getActivity().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_PRIVATE));
                exitProgressDialog = new CusProgressDialog(getActivity(), "正在退出...");
                exitProgressDialog.getLoadingDialog().show();
                if(!common.isClearAccount()) {
                    Toast.makeText(getActivity().getApplicationContext(), "清除账户异常", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!commonTrans.isClearAccount()) {
                    //((MainActivity)getActivity())
                    Toast.makeText(getActivity().getApplicationContext(), "清除司机信息异常", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<Object, Object> map = new HashMap<Object, Object>();
                map.put(Constant.TRANSPORT_ROLE, Constant.DRIVER_ROLE_ID);
                if(!commonFixed.isSave(map)) {
                    //((MainActivity)getActivity())
                    Toast.makeText(getActivity().getApplicationContext(), "初始化司机", Toast.LENGTH_SHORT).show();
                    return;
                }
                ((MainActivity)getActivity()).exitOperate();
                new Thread(new ExitDialogThread(exitProgressDialog)).start(); //弹出对话框线程
                break;
            default:
                break;
        }
    }

//    /**
//     * 用于接收Activity返回数据
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     */
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //登录返回
//        if(resultCode == Constant.LOGIN_ACTIVITY_RETURN_CODE){
//            String username = data.getExtras().getString("username");
//            userAlias.setText(username);
//            exit.setVisibility(View.VISIBLE);
//            slidingBottomLine.setVisibility(View.VISIBLE);
//            //Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
//            //String enterpriseName = common.getStringByKey(Constant.ENTERPRISE_NAME);
//            //if(enterpriseName != null) {
//                accountType.setText("个人");
//            //}
//
//            dataSets.clear();
//            for(int i = 0; i < textResources.length; i++) {
//                Map<String, Object> map = new HashMap<String, Object>();
//                map.put("list_image", imageResources[i]);
//                map.put("list_text", textResources[i]);
//                map.put("list_arrow", R.mipmap.right_arrow);
//                dataSets.add(map);
//            }
//            simpleAdapter.notifyDataSetChanged();
//        }
//        //注册返回
//        if(resultCode == Constant.REGISTER_ACTIVITY_RETURN_CODE) {
//            String username = data.getExtras().getString("username");
//            userAlias.setText(username);
//            accountType.setText("个人");
//            exit.setVisibility(View.VISIBLE);
//            slidingBottomLine.setVisibility(View.VISIBLE);
//            dataSets.clear();
//            for(int i = 0; i < textResources.length; i++) {
//                Map<String, Object> map = new HashMap<String, Object>();
//                map.put("list_image", imageResources[i]);
//                map.put("list_text", textResources[i]);
//                map.put("list_arrow", R.mipmap.right_arrow);
//                dataSets.add(map);
//            }
//            simpleAdapter.notifyDataSetChanged();
//        }
//        //账户类型返回
//        if(resultCode == Constant.ACCOUNT_TYPE_RESULT_CODE) {
//            String accountTypeStr = data.getExtras().getString("account_type");
//            accountType.setText(accountTypeStr);
//
//        }
//    }

    /**
     * 初始化数据
     */
    private void initField(View view) {
        leftSlidingListView = (ListView)view.findViewById(R.id.left_sliding_list);
        textResources = this.getResources().getStringArray(R.array.left_sliding_list_string);
        exit = (LinearLayout)view.findViewById(R.id.exit);
        userLogin = (LinearLayout)view.findViewById(R.id.user_login);
        userAlias = (TextView) view.findViewById(R.id.user_alias);
        accountType = (TextView) view.findViewById(R.id.account_type);
        roleType = (TextView) view.findViewById(R.id.role_type);
        slidingBottomLine = (View) view.findViewById(R.id.sliding_bottom_line);

        imageResources = new int[]{R.mipmap.my_purse, R.mipmap.select_user_type, R.mipmap.notice,
                R.mipmap.share, R.mipmap.setting};
        idResources = new int[]{R.id.list_image, R.id.list_text, R.id.list_arrow};
    }

    /**
     * 验证用户是否登入
     * @return
     */
    private boolean isLogin() {
        Common common = new Common(getActivity().getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        return common.isLogin();
    }

    private class SlidingListItemOnClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            if(!isLogin()) {
//                Toast.makeText(getActivity(), "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
//                //intent = new Intent(getActivity(), LoginActivity.class);
//                //startActivity(intent);
//                return;
//            }
            TextView textView = (TextView) view.findViewById(R.id.list_text);
            String menuDes = textView.getText().toString();
            if(menuDes.equals("我的钱包")) {
                intent = new Intent(getActivity().getApplicationContext(), WaitBuild.class);
                intent.putExtra("wait_title", "我的钱包");
            } else if(menuDes.equals("账户类型")){
                intent = new Intent(getActivity(), AccountTypeActivity.class);
                startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE);
            } else if(menuDes.equals("公告通知")){
                intent = new Intent(getActivity().getApplicationContext(), WaitBuild.class);
                intent.putExtra("wait_title", "公告通知");
                startActivity(intent);
            } else if(menuDes.equals("社交分享")){
                intent = new Intent(getActivity().getApplicationContext(), SocialSharedActivity.class);
                intent.putExtra("wait_title", "社交分享");
                startActivity(intent);
            } else if(menuDes.equals("系统设置")){
                intent = new Intent(getActivity(), SetUpActivity.class);
                startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE);
            }
//            switch (position) {
//                case 0:
//                    break;
//                case 1: //选择账户类型
//                    intent = new Intent(getActivity(), AccountTypeActivity.class);
//                    startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE);
//                    break;
//                case 2:
//                    break;
//                case 3:
//                    break;
//                case 4: //设置
//                    intent = new Intent(getActivity(), SetUpActivity.class);
//                    startActivityForResult(intent, Constant.ACTIVITY_RETURN_CODE);
//                    break;
//                case 5:
//                    break;
//            }

        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.loginThreadResultCode: //退出登录
                    userAlias.setText("未登录");
                    exit.setVisibility(View.GONE);
                    slidingBottomLine.setVisibility(View.GONE);
                    accountType.setText("");
                    roleType.setText("");
                    dataSets.remove(0);
                    dataSets.remove(0);
                    simpleAdapter.notifyDataSetChanged();
                    exitProgressDialog.dismissDialog();
                    Toast.makeText(getActivity().getApplicationContext(), "退出成功", Toast.LENGTH_SHORT).show();
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
}
