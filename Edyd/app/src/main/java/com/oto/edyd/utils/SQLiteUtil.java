package com.oto.edyd.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库操作工具类
 * Created by yql on 2015/9/10.
 */
public class SQLiteUtil {

    private Context context; //上下文执行对象
    private SQLiteDatabase db; //数据库操作对象

    private String databaseName; //数据库名称

    public SQLiteUtil(Context context, String databaseName) {
        this.context = context;
        this.databaseName = databaseName;
        this.db = createDatabase();
    }

    /**
     * 如果数据库不存在则创建，存在则打开
     */
    public SQLiteDatabase createDatabase() {
        return context.openOrCreateDatabase(databaseName, context.MODE_PRIVATE, null);
    }

    /**
     * 更新表
     * @param statement
     */
    public void deleteTable(String statement){
        db.execSQL(statement);
    }

}
