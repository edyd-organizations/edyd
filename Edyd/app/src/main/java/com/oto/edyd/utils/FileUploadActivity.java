package com.oto.edyd.utils;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.oto.edyd.R;
import com.squareup.okhttp.Request;

import java.io.File;

/**
 * 功能：文件上传模块
 * 文件名：com.oto.edyd.utils.FileUploadActivity.java
 * 创建时间：2016/2/16
 * 作者：yql
 */
public class FileUploadActivity extends Activity implements View.OnClickListener{

    //--------------基本View控件----------------------
    private TextView fileName; //文件名
    private Button upload; //上传
    private Button select; //选择文件

    //---------------变量------------------------
    private Uri photoUri;
    private String picPath = null;
    private static final String TAG = "uploadImage";
    private String[] fileKey; //文件名
    private File[] files; //文件流集合
    public static final int SELECT_PIC_BY_PICK_PHOTO = 2; //使用相册中的图片
    public static final String KEY_PHOTO_PATH = "photo_path";
    private Intent lastIntent ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_upload);
        init(); //初始化数据
    }

    /**
     * 初始化数据
     */
    private void init() {
        initFields();
        initListener();
        lastIntent = getIntent();
//        File file = new File("/storage/sdcard0/DCIM/Camera/IMG20160211212448.jpg");
//        files = new File[] {file};
//        fileKey = new String[] {"IMG20160211212448.jpg"};
    }

    /**
     * 初始化字段
     */
    private void initFields() {
        fileName = (TextView) findViewById(R.id.file_name);
        upload = (Button) findViewById(R.id.upload);
        select = (Button) findViewById(R.id.select);
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        upload.setOnClickListener(this);
        select.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload: //上传文件
                uploadPic();
                break;
            case R.id.select: //选择图片
                pickPhoto();
                break;

        }
    }

    /***
     * 从相册中取图片
     */
    private void pickPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
    }

    /**
     * 选择图片后，获取图片的路径
     * @param requestCode
     * @param data
     */
    private void doPhoto(int requestCode,Intent data) {
        //从相册取图片，有些手机有异常情况，请注意
        if(requestCode == SELECT_PIC_BY_PICK_PHOTO ) {
            if(data == null) {
                Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
                return;
            }
            photoUri = data.getData();
            if(photoUri == null ) {
                Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
                return;
            }
        }
        String[] pojo = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(photoUri, pojo, null, null,null);
        if(cursor != null ) {
            int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
            cursor.moveToFirst();
            picPath = cursor.getString(columnIndex);
            cursor.close();
        }
        Log.i(TAG, "imagePath = " + picPath);

        //如果是多文件上传，这里稍微再修改下
        File file = new File(picPath);
        files = new File[] {file};
        fileKey = new String[] {picPath.substring(picPath.lastIndexOf("/") + 1)};

        if(picPath != null && ( picPath.endsWith(".png") || picPath.endsWith(".PNG") ||picPath.endsWith(".jpg") ||picPath.endsWith(".JPG") )) {
            lastIntent.putExtra(KEY_PHOTO_PATH, picPath);
            setResult(Activity.RESULT_OK, lastIntent);
        }else{
            Toast.makeText(this, "选择图片文件不正确", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 图片上传
     */
    private void uploadPic() {
        String url = "http://112.74.105.205/zhizu/api/index/uploadPicture";
        OkHttpClientManager.postAsyn(url, fileKey, files, null, new OkHttpClientManager.ResultCallback<String> (){

            @Override
            public void onError(Request request, Exception e) {
                //上传失败

            }

            @Override
            public void onResponse(String response) {
                //上传成功

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            doPhoto(requestCode,data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
