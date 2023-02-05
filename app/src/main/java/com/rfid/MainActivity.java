package com.rfid;

import static com.rfid.util.CommonUtil.convertTimestamp2Date;
import static com.rfid.util.CommonUtil.getBBList;
import static com.rfid.util.CommonUtil.getRightEpc;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.rfid.enums.Notification;
import com.rfid.util.DataBaseUtil;
import com.rfid.util.NotifyAdapter;
import com.xpf.ch340_library.CH340Master;
import com.xpf.ch340_library.driver.InitCH340;
import com.xpf.ch340_library.inteface.CallBack;
import com.xpf.ch340_library.inteface.CallBackUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity implements InitCH340.IUsbPermissionListener, CallBack {

    //  TODO 数据类 ==> UI控件组
    private EditText searchEditText;
    private Button btnConnect, btnScan, btnEdit, btnFinish, btnSearch, btnCancel;
    private LinearLayout buttonLinearLayout1, buttonLinearLayout2;

    // TODO 数据类 ==> Flag组
    private boolean isReading;  // 是否打开扫描开关
    private boolean isFirst;    // 判断是否已打开
    private boolean isModify;   // 是否为编辑模式 true: 编辑模式 false: 默认模式
    private static final String ACTION_USB_PERMISSION = "com.linc.USB_PERMISSION";

    // TODO 数据类 ==> 数据库组
    private DataBaseUtil dbUtil;                                     // 数据库工具类调用
    private String temp_recv = new String();                         // 临时 EPC 数据集
    private List<Map<String, String>> queue = new ArrayList<>();     // 3秒内的缓存阵列

    // TODO 数据类 ==> ListView组
    private ListView listViewData;
    private List<Map<String, String>> list_view = new ArrayList<>(); // 用于前端显示的数据 list
    private List<Map<String, String>> cookie_list_view = new ArrayList<>(); // 缓存
    private List<Map<String, String>> search_list_view = new ArrayList<>(); // 搜索缓存
    private NotifyAdapter listViewAdapter;
    private String temp_EPCs;

    // TODO App启动 ==> 构造方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CallBackUtils.setCallBack(this);
        //主视图来自 activity_main.xml
        setContentView(R.layout.activity_main);
        initView();       // 初始化视图
        initData();       // 初始化数据
        initListener();   // 初始化监听器
    }

    // TODO 初始化 ==> 视图类 和 flag类
    private void initView() {
        isModify = false;                                                // 默认非编辑状态
        isReading = false;                                               // 默认非扫描状态
        btnScan = findViewById(R.id.btnScan);                            // 扫描按钮
        btnEdit = findViewById(R.id.btnEdit);                            // 编辑按钮
        btnFinish = findViewById(R.id.btnFinish);                        // 完成按钮
        btnCancel = findViewById(R.id.btnCancel);                        // 取消按钮
        btnSearch = findViewById(R.id.btnSearch);                        // 搜索按钮
        buttonLinearLayout1 = findViewById(R.id.buttonLinearLayout1);    // 非编辑页按钮层
        buttonLinearLayout2 = findViewById(R.id.buttonLinearLayout2);    // 编辑页按钮层
        listViewData = findViewById(R.id.listView_data);                 // 数据组
        searchEditText = findViewById(R.id.search_input);                // 搜索输入框
    }

    // TODO 初始化 ==> 按钮事件
    private void initListener() {
        // 按钮事件 -> 进入编辑页
        btnEdit.setOnClickListener(view -> {
            cookie_list_view = list_view;
            // 从数据表中获取所有数据 "左关联获取"
            if(InitCH340.isIsOpenDeviceCH340()) {
                if(search_list_view.isEmpty()) {
                    //必须先扫描后再编辑
                    Toast.makeText(MainActivity.this,Notification.MSG_WARN_EDIT.msg,Toast.LENGTH_SHORT).show();
                } else {
                    list_view = dbUtil.queryPerSecStream();
                }
            } else {
                list_view.clear();
            }
            // 视图架构
            listViewData = (ListView) findViewById(R.id.listView_data);
            listViewAdapter = new NotifyAdapter(this, list_view);
            listViewData.setAdapter(listViewAdapter);

            buttonLinearLayout1.setVisibility(View.GONE);
            buttonLinearLayout2.setVisibility(View.VISIBLE);
            isModify = true;
        });

        // 按钮事件 -> 退出编辑页
        btnFinish.setOnClickListener(view -> {
            cookie_list_view.clear(); // 缓存清理
            for(int i = 0; i < list_view.size(); i++) {
                cookie_list_view.add(list_view.get(i));
                dbUtil.update(list_view.get(i));
            }
            if(InitCH340.isIsOpenDeviceCH340()) {
                // 单片机连接状态
                list_view = cookie_list_view; // 抽出缓存
            } else {
                list_view.clear(); // 清空视图
            }
            // 视图架构
            adaptListView();
            buttonLinearLayout1.setVisibility(View.VISIBLE);
            buttonLinearLayout2.setVisibility(View.GONE);
            isModify = false;
            Toast.makeText(MainActivity.this, Notification.MSG_SUCCESS_SAVE.msg,Toast.LENGTH_SHORT).show();
        });

        // 取消编辑
        btnCancel.setOnClickListener(view -> {
            // 视图架构
            if(InitCH340.isIsOpenDeviceCH340()) {
                // 单片机连接状态
                cookie_list_view.clear(); // 缓存清理
                for(int i = 0; i < list_view.size(); i++) {
                    cookie_list_view.add(list_view.get(i));
                }
                list_view = cookie_list_view; // 抽出缓存
            } else {
                list_view.clear(); // 清空视图
            }
            adaptListView();
            buttonLinearLayout1.setVisibility(View.VISIBLE);
            buttonLinearLayout2.setVisibility(View.GONE);
            isModify = false;
        });

        // 按钮事件 -> 点击扫描
        btnScan.setOnClickListener(view -> {
            if(InitCH340.isIsOpenDeviceCH340()) { // 接入单片机
                // 数据更新
                list_view = queue;     // view 视图显示 queue 队列
                search_list_view = queue;
                adaptListView();
            } else { // 未接入单片机
                Toast.makeText(MainActivity.this,Notification.MSG_ERROR_PLUG.msg,Toast.LENGTH_SHORT).show();
            }
        });

        // 按钮事件 -> 搜索引擎
        btnSearch.setOnClickListener(view -> {
            if(!InitCH340.isIsOpenDeviceCH340()) { // 主页 编辑页 搜索
                Toast.makeText(MainActivity.this,Notification.MSG_ERROR_OUT.msg,Toast.LENGTH_SHORT).show();
            } else { // 扫描项搜索
                if (isModify) { // 编辑状态
                    //搜索内容为空
                    if(searchEditText.getText().toString().equals("")) {
                        list_view = search_list_view;
                        listViewData = (ListView) findViewById(R.id.listView_data);
                        listViewAdapter = new NotifyAdapter(this, list_view);
                        listViewData.setAdapter(listViewAdapter);
                        Toast.makeText(MainActivity.this,Notification.MSG_SUCCESS_CLEAR.msg,Toast.LENGTH_SHORT).show();
                    } else {
                        if(search_list_view.isEmpty()) {
                            Toast.makeText(MainActivity.this,Notification.MSG_WARN_NULL.msg,Toast.LENGTH_SHORT).show();
                        } else {
                            List<Map<String,String>> result_bounce = new ArrayList<>();
                            search_list_view.forEach((obj) -> {
                                if(obj.get("name").equals(searchEditText.getText().toString())) {
                                    result_bounce.add(new HashMap<String,String>(){{
                                        put("epc",obj.get("epc"));
                                        put("name",obj.get("name"));
                                        put("type",obj.get("type"));
                                        put("register_time",obj.get("register_time"));
                                    }});
                                }
                            });
                            if (result_bounce.isEmpty()) {
                                Toast.makeText(MainActivity.this,Notification.MSG_WARN_EMPTY.msg,Toast.LENGTH_SHORT).show();
                            } else {
                                list_view = result_bounce;
                                listViewData = (ListView) findViewById(R.id.listView_data);
                                listViewAdapter = new NotifyAdapter(this, list_view);
                                listViewData.setAdapter(listViewAdapter);
                            }
                        }
                    }
                } else {
                    //搜索内容为空
                    if(searchEditText.getText().toString().equals("")) {
                        list_view = search_list_view;
                        adaptListView();
                        Toast.makeText(MainActivity.this,Notification.MSG_SUCCESS_CLEAR.msg,Toast.LENGTH_SHORT).show();
                    } else {
                        if(search_list_view.isEmpty()) {
                            Toast.makeText(MainActivity.this,Notification.MSG_WARN_NULL.msg,Toast.LENGTH_SHORT).show();
                        } else {
                            List<Map<String,String>> result_bounce = new ArrayList<>();
                            search_list_view.forEach((obj) -> {
                                if(obj.get("name").equals(searchEditText.getText().toString())) {
                                    result_bounce.add(new HashMap<String,String>(){{
                                        put("epc",obj.get("epc"));
                                        put("name",obj.get("name"));
                                        put("type",obj.get("type"));
                                        put("register_time",obj.get("register_time"));
                                    }});
                                }
                            });
                            if (result_bounce.isEmpty()) {
                                Toast.makeText(MainActivity.this,Notification.MSG_WARN_EMPTY.msg,Toast.LENGTH_SHORT).show();
                            } else {
                                list_view = result_bounce;
                                adaptListView();
                            }
                        }
                    }
                }
            }
        });

    }

    // TODO 初始化 ==> 数据流
    private void initData() {
        // 接口部分
        InitCH340.setListener(this); //初始化CH340接口
        if (!isFirst) {
            isFirst = true;
            // 初始化 ch340-library
            CH340Master.initialize(MyApplication.getContext());
        }
        // 数据与页面交互部分
        dbUtil = new DataBaseUtil(MainActivity.this, null, 1); // 初始化数据库工具类
        list_view.clear();
        adaptListView();
    }

    // TODO 接入usb时 ==> 与 Handler 开始通信
    @Override
    public void doSomeThing(String string) {
        //实例化Message对象 -> 传递code
        Message message = new Message();
        message.what = 200;
        //实例化Bundle对象 -> 传递字符串
        Bundle bundle = new Bundle();
        bundle.putString("go", string);
        //调用Handler对象 -> 传递message给handler
        message.setData(bundle);
        handler.sendMessage(message);
    }

    // TODO 构建Handler对象 ==> 通信基础
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();  // 从 message 中获取 bundle => "go"
            if (msg.what == 200) {      // 从 message 中获取 what => 200
                table_temp(bundle.getString("go")); // 处理该数据集
            }
        }
    };

    // TODO Result ==> 接口类方法覆写
    @Override
    public void result(boolean isGranted) {
        if (!isGranted) {
            PendingIntent mPermissionIntent = PendingIntent
                    .getBroadcast(this,
                            0,
                            new Intent(ACTION_USB_PERMISSION),
                            0);
            InitCH340.getmUsbManager().requestPermission(InitCH340.getmUsbDevice(), mPermissionIntent);
        }
    }


    // TODO 工具组 ==> 类内部使用


    /**
     * @desc 更新 View 视图
     */
    private void adaptListView() {
        for(int i = 0; i < list_view.size(); i++) {
            if(list_view.get(i).get("register_time") != null) {
                list_view.get(i).put("register_time", convertTimestamp2Date(list_view.get(i).get("register_time")));
            }
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(
            MainActivity.this,
            list_view,
            R.layout.activity_main_item,
            new String[]{ "epc", "type", "name", "register_time" },
            new int[]{ R.id.main_item_epc,
                       R.id.main_item_type,
                       R.id.main_item_name,
                       R.id.main_item_time }
        );
        listViewData.setAdapter(simpleAdapter);
    }

    /**
     * @desc 将 epc 插入数据库
     * @param epc 需要存入的 EPC 字符串
     */
    private void insert(String epc) {
        dbUtil.insert(epc,"table_a");
        dbUtil.insert(epc,"table_b");
    }

    /**
     * @desc 处理单片机传递过来的 EPC 数据集
     * @param epc EPC 文段
     */
    public void table_temp(String epc) {
        dbUtil.devTool(epc);

        temp_EPCs += epc; // 不断拼接
        queue.clear();    // 清空队列
        String[] arr_str = temp_EPCs.split("\n"); // 切片换行得到每条数据
        List<String> wait_queue = new ArrayList<>(); // 将数据全部存入list
        for (String str : arr_str) {
            wait_queue.add(str + (str.endsWith("2 ")?"7e":""));
        }

        if (wait_queue.size() > 15) { // 若 list 长度超过 15 进行窗口滑动
            temp_EPCs = "";
            wait_queue.remove(0);
            wait_queue.add(epc);
            for (String s : wait_queue) {
                temp_EPCs += s;
            }
        }

        List<String> result_list = getRightEpc(getBBList(temp_EPCs));
        for (String s : result_list) {
            if (s.length() > 23) {
                Map<String, String> correct_map = dbUtil.getDatasByEpc(s);
                queue.add(new HashMap<String,String>(){{
                    put("epc",s);
                    put("type",correct_map.get("type"));
                    put("name",correct_map.get("name"));
                    put("register_time",String.valueOf(System.currentTimeMillis()));
                }});
                insert(s);
            }
        }
    }

}
