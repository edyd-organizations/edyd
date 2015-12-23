package com.oto.edyd.lib.imageindicator.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.oto.edyd.EdydApplication;
import com.oto.edyd.R;
import com.oto.edyd.lib.imageindicator.ImageIndicatorView;
import com.android.http.WebImageView;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.Constant;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Network ImageIndicatorView, by urls
 * 
 * @author steven-pan
 * 
 */
public class NetworkImageIndicatorView extends ImageIndicatorView {

	private List<Bitmap> bitmapList = new ArrayList<Bitmap>();
	private OkHttpClient mOkHttpClient = new OkHttpClient();
	private NetworkImageCache imageCache = new NetworkImageCache();
	private Context context = getContext();
	private Common commonFixed = new Common(getContext().getSharedPreferences(Constant.FIXED_FILE, Context.MODE_APPEND));

	private final static String LOCAL_CACHE_PATH = Environment.getExternalStorageDirectory() + "/cache/edyd/";
	private final static int HANDLER_PICTURE_CACHE_SUCCESS = 0x10; //图片缓存请求成功返回

	public NetworkImageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NetworkImageIndicatorView(Context context) {
		super(context);
	}

	/**
	 * 设置显示图片URL列表
	 * 
	 * @param urlList
	 * URL列表
	 */
	public void setupLayoutByImageUrl(final List<String> urlList) {
		String firstLaunch = commonFixed.getStringByKey("FIRST_LAUNCH");

		if (urlList == null) {
			throw new NullPointerException();
		}
		if(TextUtils.isEmpty(firstLaunch)) {
			int len = urlList.size();
			if (len > 0) {
				bitmapList.clear();
				for (int index = 0; index < len; index++) {
					getImageFromNetwork(index, urlList.size(), urlList.get(index));
				}
			}

		} else {
			int len = urlList.size();
			if (len > 0) {
				bitmapList.clear();
				for (int index = 0; index < len; index++) {
//				final WebImageView pageItem = new WebImageView(getContext());
//				pageItem.setScaleType(ImageView.ScaleType.FIT_XY);
//				pageItem.setDefaultImageResId(R.mipmap.ic_launcher);
//				pageItem.setImageUrl(urlList.get(index), EdydApplication.getImageLoader());
//				addViewItem(pageItem);

					//-------------自定义------------------
					String url = urlList.get(index);
					Bitmap memoryCacheBitmap = imageCache.getBitmap(url); //从内存缓存中获取图片
					if(memoryCacheBitmap != null) { //判断内存缓存中是否存在图片
						//------内存缓存存在------
						//bitmapList.add(index, memoryCacheBitmap);
						bitmapList.add(memoryCacheBitmap);
					} else {
						//------内存缓存不存在------
						File localCacheDirectory = new File(LOCAL_CACHE_PATH);//本地缓存目录
						if(!localCacheDirectory.exists()) { //判断目录是否存在
							//不存在创建
							localCacheDirectory.mkdirs();
						}
						//加载本地缓存
						File localCacheFile = new File(LOCAL_CACHE_PATH + url.substring(url.lastIndexOf("/") + 1)); //本地缓存图片File
						if(localCacheFile.exists()) { //判断本地缓存图片文件是否存在
							//------本地缓存图片存在------
							FileInputStream localCacheFileInputStream = null;
							try {
								localCacheFileInputStream = new FileInputStream(localCacheFile);
								Bitmap localCacheBitmap = BitmapFactory.decodeStream(localCacheFileInputStream);
								//bitmapList.add(index, localCacheBitmap);
								bitmapList.add(localCacheBitmap);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						} else {
							//------本地缓存图片文件不存在------
							//从网络上加载图片
							getImageFromNetwork(index, len, url);
						}
					}
				}
				if(!TextUtils.isEmpty(firstLaunch)) {
					mAddViewList(0); //显示View
				}
			}
		}

	}


	/**
	 * 从缓存中获取图片
	 * @param index 位置索引
	 * @param slideSize 幻灯片大小
	 * @param url 网络请求地址
	 */
	private void getImageFromNetwork(int index, int slideSize, String url) {
		Request request = new Request.Builder().url(url).build();
		Call call = mOkHttpClient.newCall(request);

		call.enqueue(new MCallback(index, slideSize, url) {
			@Override
			public void onFailure(Request request, IOException e) {

			}

			@Override
			public void onResponse(Response response) throws IOException {
				InputStream inputStream = null;
				File localCacheFile = null;
				Bitmap networkBitmap = null;
				FileOutputStream fos = null;
				String fileName = url.substring(url.lastIndexOf("/") + 1);

				try {
					inputStream = response.body().byteStream();
					networkBitmap = BitmapFactory.decodeStream(inputStream);

					try {
						localCacheFile = new File(LOCAL_CACHE_PATH + fileName); //本地缓存图片File
						if(!localCacheFile.exists()) { //判断本地缓存图片文件是否存在
							//不存在则创建文件
							localCacheFile.createNewFile();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						//将网络图片缓存到本地
						fos = new FileOutputStream(localCacheFile);
						networkBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos); // 第二个参数代表压缩率，100代表无压缩
						fos.flush();
						fos.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					//将网络图片缓存到内存
					imageCache.put(url, networkBitmap);
					String firstLaunch = commonFixed.getStringByKey("FIRST_LAUNCH");
					if(TextUtils.isEmpty(firstLaunch)) {
						bitmapList.add(networkBitmap);
						if(bitmapList.size() == slideSize) {
							Message message = Message.obtain();
							message.what = HANDLER_PICTURE_CACHE_SUCCESS;
							handler.sendMessage(message);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 添加View
	 */
	private void mAddViewList(int type) {
		int size = bitmapList.size();
		for (int index = 0; index < size; index++) {
//			final WebImageView pageItem = new WebImageView(getContext());
//			pageItem.setScaleType(ImageView.ScaleType.FIT_XY);
//			pageItem.setDefaultImageResId(R.mipmap.ic_launcher);
//			pageItem.setImageBitmap(bitmapList.get(index));
			LayoutInflater inflater = LayoutInflater.from(getContext());
			View view = inflater.inflate(R.layout.slide_imagview, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.slide_image_view);
			imageView.setImageBitmap(bitmapList.get(index));
			addViewItem(imageView);
		}
		if(type == 1) {
			this.show();
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case HANDLER_PICTURE_CACHE_SUCCESS: //缓存成功返回
					//String firstLaunch = common.getStringByKey("FIRST_LAUNCH");
					Map<Object, Object> map = new HashMap<Object, Object>();
					map.put("FIRST_LAUNCH", "1");
					if(!commonFixed.isSave(map)) {
						commonFixed.showToast(getContext(), "首次启动保存异常");
					}
					mAddViewList(1); //向幻灯片添加View
					break;
			}
		}
	};

	/**
	 *
	 */
	private class MCallback implements Callback{

		public int index; //请求次序
		public int slideSize; //幻灯片大小
		public String url; //网络请求地址

		public MCallback(int index, int slideSize, String url) {
			this.index = index;
			this.slideSize = slideSize;
			this.url = url;
		}

		@Override
		public void onFailure(Request request, IOException e) {

		}

		@Override
		public void onResponse(Response response) throws IOException {

		}
	}
}
