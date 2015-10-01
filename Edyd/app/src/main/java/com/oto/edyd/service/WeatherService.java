package com.oto.edyd.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.oto.edyd.model.FutureWeatherBean;
import com.oto.edyd.model.HoursWeatherBean;
import com.oto.edyd.model.PMBean;
import com.oto.edyd.model.WeatherBean;
import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Yanql on 2015/7/14.
 */
public class WeatherService extends Service {

    private String defaultCityName;
    private WeatherServiceBinder binder = new WeatherServiceBinder();
    private boolean isRunning = false;
    private List<HoursWeatherBean> hoursWeatherBeanList;
    private PMBean pmBean;
    private WeatherBean weatherBean;
    private OnParserCallBack callBack;
    private Context sContext;

    private final int REPEAT_MSG = 0x01;
    private final int CALLBACK_OK = 0x02;
    private final int CALLBACK_ERROR = 0x04;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        mHandler.sendEmptyMessage(REPEAT_MSG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }





    /**
     * 绑定方法
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        defaultCityName = intent.getExtras().get("city").toString();
        return binder;
    }

    public class WeatherServiceBinder extends Binder {
        public WeatherService getService() {
            return WeatherService.this;
        }
    }
    public interface OnParserCallBack {
        public void OnParserComplete(List<HoursWeatherBean> hoursWeatherBeanList, PMBean pmBean, WeatherBean weatherBean);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPEAT_MSG:
                    if(defaultCityName != null) {
                        loadData(defaultCityName);
                    }
                    sendEmptyMessageDelayed(REPEAT_MSG, 60 * 60 * 1000);
                    break;
                case CALLBACK_OK:
                    if (callBack != null) {
                        callBack.OnParserComplete(hoursWeatherBeanList, pmBean, weatherBean);
                    }
                    isRunning = false;
                    break;
                case CALLBACK_ERROR:
                    Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

    };

    /**
     * 加载数据
     * @param defaultCityName 默认城市
     */
    public void loadData(String defaultCityName){
        if(defaultCityName.indexOf("市") != -1) {
            defaultCityName = defaultCityName.substring(0, defaultCityName.indexOf("市")).trim();
        }
        this.defaultCityName = defaultCityName;
        if(isRunning) {
            return;
        }
        isRunning = true;
        final CountDownLatch countDownLatch = new CountDownLatch(3);

        /**
         * 根据城市名/id查询天气
         *
         * 名称       类型      必填      说明
         * cityname   string    Y         城市名或城市ID，如："苏州"，需要utf8 urlencode
         * dtype      string    N         返回数据格式：json或xml,默认json
         * format     int       N         未来6天预报(future)两种返回格式，1或2，默认1
         * key        string    Y         你申请的key
         */
        Parameters params_city = new Parameters();
        params_city.add("cityname", defaultCityName);
        params_city.add("key", "ee749676b4246fdd3cd1f9566868b00e");

        /**
         * 数据ID：39
         * 接口地址：http://v.juhe.cn/weather/index
         * 支持格式：JSON/XML
         * 请求方式：GET
         */
        JuheData.executeWithAPI(sContext, 39, "http://v.juhe.cn/weather/index", JuheData.GET, params_city, new DataCallBack() {

            /**
             * 成功返回执行
             * @param statusCode 状态码
             * @param responseString 返回数据
             */
            @Override
            public void onSuccess(int statusCode, String responseString) {
                try {
                    JSONObject weatherJson = new JSONObject(responseString);
                    weatherBean = parserWeather(weatherJson);
                    countDownLatch.countDown(); //计数减一
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /**
             * 执行成功与否都执行
             */
            @Override
            public void onFinish() {

            }

            /**
             * 返回失败执行
             * @param statusCode
             * @param responseString
             * @param throwable
             */
            @Override
            public void onFailure(int statusCode, String responseString, Throwable throwable) {

            }
        });

        /**
         * 根据城市名称查询未来每3小时预报
         *
         * 名称       类型      必填      说明
         * cityname   string    Y         城市名或城市ID，如："苏州"，需要utf8 urlencode
         * dtype      string    N         返回数据格式：json或xml,默认json
         * key        string    Y         你申请的key
         **/
        Parameters params_forecast3h = new Parameters();
        params_forecast3h.add("cityname", defaultCityName);
        params_forecast3h.add("key", "ee749676b4246fdd3cd1f9566868b00e");

        /**
         * 数据ID：39
         * 接口地址：http://v.juhe.cn/weather/forecast3h
         * 支持格式：JSON/XML
         * 请求方式：HTTP POST/GET
         */
        JuheData.executeWithAPI(sContext, 39, "http://v.juhe.cn/weather/forecast3h", JuheData.GET, params_forecast3h, new DataCallBack() {
            @Override
            public void onSuccess(int statusCode, String responseString) {
                try {
                    JSONObject forecast3hJson = new JSONObject(responseString);
                    hoursWeatherBeanList = parserForecast3h(forecast3hJson);
                    countDownLatch.countDown();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onFailure(int statusCode, String responseString, Throwable throwable) {

            }
        });

        /**
         * 城市空气PM2.5指数
         * 名称   类型     必填   说明
         * city   String   是     城市名称的中文名称或拼音，如：上海 或 shanghai
         *  key   String   是     APP Key
         */
        Parameters params_pm = new Parameters();
        params_pm.add("city", defaultCityName);
        params_pm.add("key", "a0356dc89e60940ce50102a876ad3c35");
        /**
         * 数据ID：33
         * 接口地址：http://web.juhe.cn:8080/environment/air/pm
         * 支持格式：JSON/XML
         * 请求方式：HTTP POST/GET
         */
        JuheData.executeWithAPI(sContext, 33, "http://web.juhe.cn:8080/environment/air/pm", JuheData.GET, params_pm, new DataCallBack() {
            @Override
            public void onSuccess(int statusCode, String responseString) {
                try {
                    JSONObject pmJson = new JSONObject(responseString);
                    pmBean = parserPM(pmJson);
                    countDownLatch.countDown();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onFailure(int statusCode, String responseString, Throwable throwable) {

            }
        });

        new Thread() {
            @Override
            public void run() {
                try {
                    countDownLatch.await(); //调用此方法会一直阻塞当前线程，直到计时器的值为0
                    mHandler.sendEmptyMessage(CALLBACK_OK);
                } catch (InterruptedException ex) {
                    mHandler.sendEmptyMessage(CALLBACK_ERROR);
                    return;
                }
            }
        }.start();
    }

    /**
     * 解析城市天气
     * @param weatherJson
     */
    public WeatherBean parserWeather(JSONObject weatherJson) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        WeatherBean weatherBean = null;
        try {
            int resultCode = weatherJson.getInt("resultcode"); //结果码，200代表成功
            int errorCode = weatherJson.getInt("error_code"); //错误码，0代表成功

            if(resultCode == 200 && errorCode == 0) { //代表数据返回成功，做相应处理
                JSONObject resultJson = weatherJson.getJSONObject("result");
                weatherBean = new WeatherBean();

                //today
                JSONObject todayWeather = resultJson.getJSONObject("today");
                weatherBean.setCity(todayWeather.getString("city"));
                weatherBean.setData(todayWeather.getString("date_y"));
                weatherBean.setWeek(todayWeather.getString("week"));
                weatherBean.setTemperature(todayWeather.getString("temperature"));
                weatherBean.setWeather(todayWeather.getString("weather"));
                weatherBean.setWeather_id(todayWeather.getJSONObject("weather_id").getString("fa"));
                weatherBean.setWeather_id_b(todayWeather.getJSONObject("weather_id").getString("fb"));
                weatherBean.setWind(todayWeather.getString("wind"));
                weatherBean.setDressing_index(todayWeather.getString("dressing_index"));
                weatherBean.setDressing_advice(todayWeather.getString("dressing_advice"));
                weatherBean.setUv_index(todayWeather.getString("uv_index"));
                weatherBean.setComfort_index(todayWeather.getString("comfort_index"));
                weatherBean.setWash_index(todayWeather.getString("wash_index"));
                weatherBean.setTravel_index(todayWeather.getString("travel_index"));
                weatherBean.setExercise_index(todayWeather.getString("exercise_index"));
                weatherBean.setDrying_index(todayWeather.getString("drying_index"));

                //sk
                JSONObject skJson = resultJson.getJSONObject("sk");
                weatherBean.setTemp(skJson.getString("temp"));
                weatherBean.setWind_direction(skJson.getString("wind_direction"));
                weatherBean.setWind_strength(skJson.getString("wind_strength"));
                weatherBean.setHumidity(skJson.getString("humidity"));
                weatherBean.setRelease(skJson.getString("time")); //更新时间

                //future
                List<FutureWeatherBean> futureWeatherBeanList = new ArrayList<FutureWeatherBean>();
                Date date = new Date(System.currentTimeMillis());
                Calendar calendar = new GregorianCalendar();
                JSONObject futureJson= resultJson.getJSONObject("future");
                Iterator it = futureJson.keys();
                int index = 1;
                while (it.hasNext()) { //检查是否有数据
                    String keyDay = getFutureDate(date, calendar, format, index);
                    index = index+1;
                    JSONObject futureJosnDay = futureJson.getJSONObject(keyDay);
                    FutureWeatherBean futureWeatherBean = new FutureWeatherBean();
                    futureWeatherBean.setTemperature(futureJosnDay.getString("temperature"));
                    futureWeatherBean.setWeather(futureJosnDay.getString("weather"));
                    futureWeatherBean.setWeather_id(futureJosnDay.getJSONObject("weather_id").getString("fa"));
                    futureWeatherBean.setWind(futureJosnDay.getString("wind"));
                    futureWeatherBean.setWeek(futureJosnDay.getString("week"));
                    futureWeatherBean.setDate(futureJosnDay.getString("date"));
                    futureWeatherBeanList.add(futureWeatherBean);
                    if(futureWeatherBeanList.size() == 3) {
                        index = 1;
                        break;
                    }
                }
                weatherBean.setFutureList(futureWeatherBeanList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weatherBean;
    }

    /**
     * 解析未来三小时数据
     * @param forecast3hJson
     */
    private List<HoursWeatherBean> parserForecast3h(JSONObject forecast3hJson) {
        List<HoursWeatherBean> forecast3hWeatherBeanList = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        Date date = new Date(System.currentTimeMillis());
        try {
            int resultCode = forecast3hJson.getInt("resultcode");//结果码，200代表成功
            int errorCode = forecast3hJson.getInt("error_code"); //错误码，0代表成功
            if(resultCode == 200 && errorCode == 0) { //代表数据返回成功，做相应处理
                Calendar calendar = new GregorianCalendar();
                forecast3hWeatherBeanList = new ArrayList<HoursWeatherBean>();
                JSONArray forecast3hArray = forecast3hJson.getJSONArray("result");
                for(int i = 0; i < forecast3hArray.length(); i++) {
                    HoursWeatherBean hoursWeatherBean = new HoursWeatherBean();
                    JSONObject hourJson = forecast3hArray.getJSONObject(i);
                    Date hDate = format.parse(hourJson.getString("sfdate"));
                    if(!hDate.after(date)){
                        continue;
                    }
                    hoursWeatherBean.setWeather_id(hourJson.getString("weatherid"));
                    hoursWeatherBean.setWeather(hourJson.getString("weather"));
                    hoursWeatherBean.setTemp_a(hourJson.getString("temp1") + "º");
                    hoursWeatherBean.setTemp_b(hourJson.getString("temp2") + "º");
                    hoursWeatherBean.setSh(hourJson.getString("sh"));
                    hoursWeatherBean.setEh(hourJson.getString("eh"));
                    hoursWeatherBean.setDate(hourJson.getString("date"));
                    hoursWeatherBean.setSfdate(hourJson.getString("sfdate"));
                    hoursWeatherBean.setEfdate(hourJson.getString("efdate"));
                    calendar.setTime(hDate);
                    hoursWeatherBean.setTime(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
                    forecast3hWeatherBeanList.add(hoursWeatherBean);
                    if(forecast3hWeatherBeanList.size() == 5) {
                        break;
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return forecast3hWeatherBeanList;
    }

    /**
     * 解析PM
     * @param pmJson
     * @return
     */
    private PMBean parserPM(JSONObject pmJson) {
        PMBean pmBean = new PMBean();
        try {
            int resultCode = resultCode = pmJson.getInt("resultcode"); //结果码，200代表成功
            int errorCode = pmJson.getInt("error_code"); //错误码，0代表成功
            if(resultCode == 200 && errorCode == 0) { //代表数据返回成功，做相应处理
                JSONObject resultJson = pmJson.getJSONArray("result").getJSONObject(0);
                pmBean.setAqi(resultJson.getString("AQI"));
                pmBean.setQuality(resultJson.getString("quality"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pmBean;
    }

    /**
     * 返回未来日期
     * @param date
     * @param calendar
     * @param format
     * @param index
     * @return
     */
    public String getFutureDate(Date date,Calendar calendar,SimpleDateFormat format, int index) {
        calendar.setTime(date);
        calendar.add(calendar.DATE, index);
        date=calendar.getTime(); //这个时间就是日期往后推一天的结果
        String dateString = "day_"+format.format(date);
        return dateString;
    }

    public void setCallBack(OnParserCallBack callback) {
        this.callBack = callback;
    }
    public void removeCallBack() {
        callBack = null;
    }


}
