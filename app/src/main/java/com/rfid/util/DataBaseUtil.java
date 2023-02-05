package com.rfid.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//SQLite数据库类封装
public class DataBaseUtil extends SQLiteOpenHelper {
    private static final String db_name = "rfid_demo.db"; //数据库名

    // TODO ------> 创建 table_a 表
    private static final String sql_table_a =
            "CREATE TABLE IF NOT EXISTS \"table_a\" (\n" +
            "\t\"epc_id\"\tTEXT PRIMARY KEY,\n" +          // EPC ID
            "\t\"register_time\"\tTEXT\n" +    // 扫描时间
            ");";
    // TODO ------> 创建 table_b 表
    private static final String sql_table_b =
            "CREATE TABLE IF NOT EXISTS \"table_b\" (\n" +
            "\t\"epc_id\"\tTEXT PRIMARY KEY,\n" +          // EPC ID
            "\t\"class\"\tTEXT,\n" +           // 类别
            "\t\"item\"\tTEXT,\n" +            // 名称
            "\t\"register_time\"\tTEXT\n" +    // 扫描时间
            ");";
    // TODO ------> 创建 table_dev 表
    private static final String sql_table_dev =
            "CREATE TABLE IF NOT EXISTS \"table_dev\" (\n" +
                    "\t\"epc_full\"\tTEXT,\n" +          // EPC ID
                    "\t\"time\"\tTEXT\n" +    // 扫描时间
                    ");";

    // TODO ------> 构建连接池
    public DataBaseUtil(@Nullable Context context, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, db_name, factory, version);
    }

    // TODO ------> 当表不存在时进行创建
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sql_table_a);
        db.execSQL(sql_table_b);
        db.execSQL(sql_table_dev);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {}

    // 编辑页逻辑：a表左关联b表进行查询
    public List<Map<String,String>> getDatas() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Map<String,String>> list = new ArrayList<>();
        // 左关联选取
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(
                "SELECT TABLE_A.epc_id,TABLE_B.class,TABLE_B.item,TABLE_A.register_time FROM TABLE_A LEFT JOIN TABLE_B ON TABLE_A.epc_id = TABLE_B.epc_id ORDER BY TABLE_A.epc_id",
                null);
        while (cursor.moveToNext()) {
            list.add(new HashMap<String,String>(){{
                put("epc",cursor.getString(cursor.getColumnIndex("epc_id")));
                put("type",cursor.getString(cursor.getColumnIndex("class")));
                put("name",cursor.getString(cursor.getColumnIndex("item")));
                put("register_time",CommonUtil.convertTimestamp2Date(cursor.getString(cursor.getColumnIndex("register_time"))));
            }});
        }
        db.close();
        return list;
    }

    // 获取表内所有数据
    public Map<String,String> getDatasByEpc(String epc) {
        SQLiteDatabase db = this.getWritableDatabase();
        Map<String, String> result_map = new HashMap<>();
        // 主键查询
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT * FROM TABLE_B WHERE epc_id = '"+epc+"'",null);
        // 缓存 class 和 item 数据
        while (cursor.moveToNext()) {
            result_map.put("type",cursor.getString(cursor.getColumnIndex("class")));
            result_map.put("name",cursor.getString(cursor.getColumnIndex("item")));
        }
        db.close();
        return result_map;
    }

    public List<Map<String,String>> searchCondition(String queryStr) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Map<String,String>> result_list = new ArrayList<>();
        // 查询键并存入
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT * FROM TABLE_B WHERE class LIKE '%"+queryStr+"%' OR item LIKE '%"+queryStr+"%'",null);
        // 指针移动
        while(cursor.moveToNext()) {
            result_list.add(new HashMap<String,String>(){{
                put("epc",cursor.getString(cursor.getColumnIndex("epc_id")));
                put("type",cursor.getString(cursor.getColumnIndex("class")));
                put("name",cursor.getString(cursor.getColumnIndex("item")));
                put("register_time",CommonUtil.convertTimestamp2Date(cursor.getString(cursor.getColumnIndex("register_time"))));
            }});
        }
        return result_list;
    }

    // TODO ------> 插入数据库
    public void insert(String epc, String dbTable) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 对 EPC 进行截断后插入
        if (dbTable.equals("table_a")) {
            // 更新时间戳
            db.execSQL(
                    "INSERT OR REPLACE INTO " + dbTable + "(epc_id, register_time) VALUES(?, ?)",
                    new Object[]{epc, System.currentTimeMillis()});
        } else if (dbTable.equals("table_b")) {
            // 更新备注数据
            db.execSQL(
                    "INSERT OR IGNORE INTO " + dbTable + "(epc_id, register_time) VALUES(?, ?)",
                    new Object[]{epc, System.currentTimeMillis()});
        }
        db.close();
    }

    /**
     * 更新数据库信息(默认更新table_b)
     */
    public void update(Map<String,String> map) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "UPDATE TABLE_B SET epc_id = '" + map.get("epc") +
                "', class = '" + (map.get("type")==null||map.get("type").equals("")?"":map.get("type")) +
                "', item = '" + (map.get("name")==null||map.get("name").equals("")?"":map.get("name")) +
                "' WHERE epc_id = '" + map.get("epc") + "';"
        );
        db.close();
    }

    // TODO ------> dev tool
    public void devTool(String epc) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "INSERT INTO TABLE_DEV(epc_full, time) VALUES(?, ?)",
                new Object[]{epc, System.currentTimeMillis()});
        db.close();
    }

    //TODO ------> go db
    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Map<String,String>> queryPerSecStream() {
        long l = System.currentTimeMillis();
        SQLiteDatabase db = this.getWritableDatabase();
        List<Map<String,String>> final_result = new ArrayList<>();
        //并发SQL ---> 左关联
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(
                "SELECT TABLE_A.epc_id,TABLE_B.class,TABLE_B.item,TABLE_A.register_time FROM TABLE_A LEFT JOIN TABLE_B ON TABLE_A.epc_id = TABLE_B.epc_id ORDER BY TABLE_A.register_time DESC",
                null);
        while(cursor.moveToNext()) {
            if(l - Long.parseLong(cursor.getString(cursor.getColumnIndex("register_time"))) <= 3000) {
                final_result.add(new HashMap<String,String>(){{
                    put("epc",cursor.getString(cursor.getColumnIndex("epc_id")));
                    put("type",cursor.getString(cursor.getColumnIndex("class")));
                    put("name",cursor.getString(cursor.getColumnIndex("item")));
                    put("register_time",cursor.getString(cursor.getColumnIndex("register_time")));
                }});
            }
        }
        //返回结果
        return final_result;
    }

}