package com.oto.edyd.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * Created by yql on 2015/10/10.
 */
public class CustomerParcelable implements Parcelable {

    private View view;

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    protected CustomerParcelable(Parcel in) {
        view = (View) in.readValue(null);
    }

    /**
     * 为了能够实现模板参数的传入，这里定义Creator嵌入接口,内含两个接口函数分别返回单个和多个继承类实例
     */
    public static final Creator<CustomerParcelable> CREATOR = new Creator<CustomerParcelable>() {
        @Override
        public CustomerParcelable createFromParcel(Parcel in) {
            return new CustomerParcelable(in);
        }

        @Override
        public CustomerParcelable[] newArray(int size) {
            return new CustomerParcelable[size];
        }
    };

    /**
     * //内容描述接口，基本不用管
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * //写入接口函数，打包
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(view); //将你的对象序列化为一个Parcel对象
    }
}
