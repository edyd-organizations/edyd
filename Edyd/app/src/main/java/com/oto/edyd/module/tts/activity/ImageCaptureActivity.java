package com.oto.edyd.module.tts.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.R;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.oto.edyd.utils.CusProgressDialog;
import com.oto.edyd.utils.OkHttpClientManager;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 功能：调用系统相机照相
 * 文件名：com.example.yql.tmp.ImageCaptureActivity.java
 * 创建时间：2016/2/17
 * 作者：yql
 */
public class ImageCaptureActivity extends Activity implements View.OnClickListener {
    //-------------基本View控件--------------------
    private LinearLayout back; //返回
    private ImageView capturePicture; //捕获图像
    private TextView upload; //上传
    private TextView title; //标题

    //-------------基本变量--------------------
    private CusProgressDialog dialog;
    private int controlId; //control ID
    private Uri sysCameraUri; //系统相机URI
    private File noCompressFile; //未压缩前的File
    private String totalPath; //总路径
    String[] fileKey = new String[1];
    File[] files = new File[1];
    File file = null;
    private Common common;
    private File mediaFile; //文件file对象
    public static final int MEDIA_TYPE_IMAGE = 1; //媒体图片
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100; //照片拍照成功返回码
    private static final int HANDLER_UPLOAD_SUCCESS_CODE = 0x10; //图片上传成功
    private static final int HANDLER_UPLOAD_FAIL_CODE = 0x11; //图片上传失败

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无ActionBar
        setContentView(R.layout.intent_image_capture);
        init();
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        Intent tIntent = getIntent();
        controlId = tIntent.getIntExtra("controlId", 0);
        int orderStatus = tIntent.getIntExtra("orderStatus", 0);
        switch (orderStatus) {
            case 17:
                title.setText("接单");
                break;
            case 20:
                title.setText("到达装货");
                break;
            case 30:
                title.setText("装货完成");
                break;
            case 40:
                title.setText("送货在途");
                break;
            case 50:
                title.setText("到达收货");
                break;
            case 60:
                title.setText("收货完成");
                break;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //调用系统相机Intent
        noCompressFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if(noCompressFile == null) {
            return;
        }
        sysCameraUri = Uri.fromFile(noCompressFile);
        //file = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if(sysCameraUri !=null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, sysCameraUri); //设置图片输出路径后，onActivityResult函数参数data将不会返回数据
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        back = (LinearLayout) findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        capturePicture = (ImageView) findViewById(R.id.capture_picture);
        upload = (TextView) findViewById(R.id.upload);
        dialog = new CusProgressDialog(ImageCaptureActivity.this);
        common = new Common(getSharedPreferences(Constant.LOGIN_PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        back.setOnClickListener(this);
        upload.setOnClickListener(this);
    }

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        File file = getOutputMediaFile(type);
        if(file == null) {
            return null;
        }
        return Uri.fromFile(file);
    }


    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "EdydTmp"); //获取SD卡存储图片路径
        totalPath = mediaStorageDir.getAbsolutePath();
        //判断路径是否存在
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                getSmallBitmap(mediaFile.getAbsolutePath());
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                finish();
            } else {
                // Image capture failed, advise user
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back: //返回
                delAllFile(totalPath);
                finish();
                break;
            case R.id.upload: //上传
                updateOrderAndUploadImage();
                break;
        }
    }

    /**
     * 压缩图片
     * @param filePath
     */
    public void getSmallBitmap(String filePath) {
        long fileSize; //文件大小
        FileOutputStream fos = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        if(bm == null){
            return;
        }

        try{
            file = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            //判断文件是否大于0.5M
            fileSize = file.length() / 1048576; //文件大小
            if(fileSize > 512) {
                //Toast.makeText(ImageCaptureActivity.this, "文件大于0.5M，不能上传", Toast.LENGTH_SHORT).show();
                common.showToast(ImageCaptureActivity.this, "文件大于0.5M，不能上传");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                if(fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        capturePicture.setImageBitmap(bm);
        //文件上传file对象, fileName, filePath
        String fileName =  file.getName();
        fileKey[0] = fileName;
        files[0] = file;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    /**
     * 更新订单并上传图片
     */
    private void updateOrderAndUploadImage() {
        OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[2];
        OkHttpClientManager.Param pControlId = new OkHttpClientManager.Param("controlId", String.valueOf(controlId));
        OkHttpClientManager.Param pSessionUuid = new OkHttpClientManager.Param("sessionUuid", common.getStringByKey(Constant.SESSION_UUID));
        params[0] = pControlId;
        params[1] = pSessionUuid;
        String url = Constant.ENTRANCE + "/appUploadServlet/tts/controlPicture";
        OkHttpClientManager.postAsyn(url, fileKey, files, params, new ImageUploadResultCallback<String> (){

            @Override
            public void onError(Request request, Exception e) {
                //上传失败

            }

            @Override
            public void onResponse(String response) {
                //上传成功
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("backFlag");
                    Message message = Message.obtain();
                    if(TextUtils.isEmpty(status)) {
                        common.showToast(ImageCaptureActivity.this, "更新司机订单失败");
                        message.what = HANDLER_UPLOAD_FAIL_CODE;
                        return;
                    }
                    message.what = HANDLER_UPLOAD_SUCCESS_CODE;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    abstract class ImageUploadResultCallback<T> extends OkHttpClientManager.ResultCallback<T>{
        @Override
        public void onBefore() {
            dialog.showDialog();
        }

        @Override
        public void onAfter() {
            dialog.dismissDialog();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            delAllFile(totalPath);
            switch (msg.what) {
                case HANDLER_UPLOAD_SUCCESS_CODE: //图片上传成功
                    setResult(0x11);
                    finish();
                    break;
                case HANDLER_UPLOAD_FAIL_CODE:
                    finish();
                    break;
            }
        }
    };

    /**
     * 删除文件
     */
    private void deleteFiles(String path) {
        delAllFile(path);
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
        }
        return flag;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            delAllFile(totalPath);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
