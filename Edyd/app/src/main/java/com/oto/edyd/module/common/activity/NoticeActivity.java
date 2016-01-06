package com.oto.edyd.module.common.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oto.edyd.R;
import com.oto.edyd.module.common.model.Notice;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 功能：公告通知列表
 * 文件名：com.oto.edyd.module.common.activity.NoticeActivity.java
 * 创建时间：2016/1/4
 * 作者：yql
 */
public class NoticeActivity extends Activity implements View.OnClickListener, AbsListView.OnScrollListener{

    //-----------基本View控件--------------
    private LinearLayout back; //返回
    private ListView list; //列表
    private SwipeRefreshLayout swipeRefreshLayout; //下拉刷新

    private Common common;
    private Context context;
    private List<Notice> noticeList = new ArrayList<Notice>();
    private NoticeAdapter noticeAdapter; //列表适配器
    private CusProgressDialog dialog; //过渡框
    private int visibleLastIndex = 0; //最后可视项索引
    private boolean loadFlag = false;
    private final static int ROWS = 10; //分页加载数据每页10

    private final static int FIRST_LOAD = 1; //首次加载
    private final static int REFRESH_LOAD = 2; //首次加载
    private final static int HANDLER_MESSAGE_SUCCESS_CODE = 0x10; //消息返回成功码
    private final static int HANDLER_MESSAGE_REFRESH_CODE = 0x11; //消息返回成功码
    private final static int HANDLER_MESSAGE_PAGE_CODE = 0x12; //消息返回成功码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        init();
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        requestNoticeListData(1, ROWS, FIRST_LOAD);
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        list = (ListView) findViewById(R.id.notice_list);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        common= new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
        list.setOnScrollListener(this);
        context = NoticeActivity.this;
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NoticeActivity.this, NoticeDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("notice", noticeList.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            /**
             * 刷新要做的操作
             */
            @Override
            public void onRefresh() {
                requestNoticeListData(1, ROWS, REFRESH_LOAD);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                finish();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = noticeAdapter.getCount(); //数据集最后一项的索引
        //int lastIndex = itemsLastIndex + 1; //加上底部的loadMoreIndex项
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE  && visibleLastIndex ==lastIndex){
            //如果是自动加载,可以在这里放置异步加载数据的代码
            if(loadFlag) {
                loadFlag = false;
                if(lastIndex % ROWS == 0) {
                    int page = lastIndex / ROWS + 1;
                    pageRequestNoticeListData(page, ROWS);
                }
            }

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount;
    }

    /**
     * 请求通知列表
     * * @param page 第几页
     * @param rows 每页几条
     * @param loadType 加载类型
     */
    private void requestNoticeListData(int page, int rows, int loadType){
        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);
        String url = Constant.ENTRANCE_PREFIX_v1 + "inquirePhoneMessageAPPList.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseId + "&page=" + page + "&rows=" + rows;
        OkHttpClientManager.getAsyn(url, new NoticeResultCallback<String>(loadType) {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "消息获取异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "消息获取失败");
                        return;
                    }
                    loadFlag = true;
                    noticeList.clear();
                    jsonArray = jsonObject.getJSONArray("rows");
                    if(jsonArray.length() <= 0) {
                        common.showToast(context, "暂无数据");
                    }
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);
                        Notice notice = new Notice();
                        notice.setSender(item.getString("enterpriseName"));
                        long spaceTime = countTimeSpace(item.getString("effTime"));
                        if(spaceTime <= 24) {
                            notice.setTime(spaceTime + "小时前");
                        } else {
                            notice.setTime(getDate(item.getString("effTime")));
                        }
                        notice.setTitle(item.getString("messageTitle"));
                        notice.setContent(item.getString("messageContent"));
                        noticeList.add(notice);
                    }

                    Message message = Message.obtain();
                    if(loadType == 1) {
                        message.what = HANDLER_MESSAGE_SUCCESS_CODE;
                    } else if(loadType == 2) {
                        message.what = HANDLER_MESSAGE_REFRESH_CODE;
                    }

                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 分页请求通知列表
     * * @param page 第几页
     * @param rows 每页几条
     */
    private void pageRequestNoticeListData(int page, int rows){

        String enterpriseId = common.getStringByKey(Constant.ENTERPRISE_ID);
        String sessionUuid = common.getStringByKey(Constant.SESSION_UUID);

        String url = Constant.ENTRANCE_PREFIX_v1 + "inquirePhoneMessageAPPList.json?sessionUuid=" + sessionUuid + "&enterpriseId=" + enterpriseId + "&page=" + page + "&rows=" + rows;
        OkHttpClientManager.getAsyn(url, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                common.showToast(context, "消息获取异常");
            }

            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(!status.equals(Constant.LOGIN_SUCCESS_STATUS)) {
                        common.showToast(context, "消息获取失败");
                        return;
                    }
                    loadFlag = true;
                    jsonArray = jsonObject.getJSONArray("rows");
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject item = jsonArray.getJSONObject(i);
                        Notice notice = new Notice();
                        notice.setSender(item.getString("enterpriseName"));
                        long spaceTime = countTimeSpace(item.getString("effTime"));
                        if(spaceTime <= 24) {
                            if(spaceTime < 1) {
                                notice.setTime("刚刚");
                            } else {
                                notice.setTime(spaceTime + "小时前");
                            }
                        } else {
                            notice.setTime(getDate(item.getString("effTime")));
                        }
                        notice.setTitle(item.getString("messageTitle"));
                        notice.setContent(item.getString("messageContent"));
                        noticeList.add(notice);
                    }

                    Message message = Message.obtain();
                    message.what = HANDLER_MESSAGE_PAGE_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_SUCCESS_CODE: //列表请求成功
                    noticeAdapter = new NoticeAdapter(context);
                    list.setAdapter(noticeAdapter);
                    break;
                case HANDLER_MESSAGE_REFRESH_CODE: //下拉刷新请求成功返回码
                    noticeAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false); //停止刷新
                    break;
                case HANDLER_MESSAGE_PAGE_CODE: //分页请求返回成功
                    noticeAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    /**
     * 消息列表适配器
     */
    private class NoticeAdapter extends BaseAdapter{
        private LayoutInflater inflater;

        public NoticeAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return noticeList.size();
        }

        @Override
        public Object getItem(int position) {
            return noticeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if(convertView == null) {
                convertView = inflater.inflate(R.layout.notice_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.sender = (TextView) convertView.findViewById(R.id.sender);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.content = (TextView) convertView.findViewById(R.id.content);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Notice notice = noticeList.get(position);
            viewHolder.sender.setText(notice.getSender());
            viewHolder.time.setText(notice.getTime());
            viewHolder.title.setText(notice.getTitle());
            viewHolder.content.setText(notice.getContent());
            return convertView;
        }
    }

    static class ViewHolder{
        TextView sender; //发件人
        TextView time; //发送时间
        TextView title; //标题
        TextView content; //正文
    }

    /**
     * 计算时间间隔
     * @param time 过去时间
     */
    private long countTimeSpace(String time) {
        Date oldDate = null;
        Date currentDate = null;
        long space = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        try {
//            oldDate = simpleDateFormat.parse(time);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        Calendar calendar = Calendar.getInstance();
//        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
//        calendar.setTime(oldDate);
//        int oldHour = calendar.get(Calendar.HOUR_OF_DAY);
//        return currentHour - oldHour;

        try {
            oldDate = simpleDateFormat.parse(time);
            currentDate = new Date();
            space = (currentDate.getTime() - oldDate.getTime()) / 3600000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return space;
    }

    /**
     * 获取日期
     * @param time 时间
     * @return
     */
    private String getDate(String time) {
        Date oldDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            oldDate = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

    public abstract class NoticeResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{

        public int loadType;

        public NoticeResultCallback(int loadType) {
            this.loadType = loadType;
        }
        @Override
        public void onBefore() {
            //请求之前操作
            if(loadType == 1) {
                dialog = new CusProgressDialog(context, "正在拼命加载...");
                dialog.getLoadingDialog().show();
            }
        }

        @Override
        public void onAfter() {
            //请求之后要做的操作
            if(loadType == 1) {
                dialog.getLoadingDialog().dismiss();
            }
        }
    }
}
