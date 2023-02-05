package com.rfid.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.rfid.R;

import java.util.List;
import java.util.Map;

/**
 * Created by CLF on 2016/8/23.
 */
public class NotifyAdapter extends BaseAdapter {

    private final List<Map<String,String>> mData;
    private final LayoutInflater mInflater;

    public NotifyAdapter(Context context, List<Map<String,String>> data) {
        this.mData = data;
        mInflater = LayoutInflater.from(context);
    }

    public List<Map<String,String>> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //判断是否缓存
        if (convertView == null) {
            viewHolder = new ViewHolder();
            //通过LayoutInflater实例化布局
            convertView = mInflater.inflate(R.layout.activity_main_edit, null);
            viewHolder.mEpcTextView = convertView.findViewById(R.id.main_edit_epc);
            viewHolder.mTypeEditView = convertView.findViewById(R.id.main_edit_type);
            viewHolder.mNameEditView = convertView.findViewById(R.id.main_edit_name);
            viewHolder.mRegister_timeTextView = convertView.findViewById(R.id.main_edit_time);
            viewHolder.mTypeEditView.setTag(position);
            viewHolder.mNameEditView.setTag(position);
            convertView.setTag(viewHolder);
        } else {
            //通过Tag找到缓存的布局
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTypeEditView.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                Integer index = (Integer) view.getTag();
                viewHolder.mTypeEditView.addTextChangedListener(new TextWatcher() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mData.get(index).replace("type",charSequence.toString());
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mData.get(index).replace("type",charSequence.toString());
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void afterTextChanged(Editable editable) {
                        mData.get(index).replace("type",editable.toString());
                    }
                });
                viewHolder.mTypeEditView.setOnFocusChangeListener((view1, b) -> {
                    if(!b) {
                        mData.get(index).replace("type",viewHolder.mTypeEditView.getText().toString());
                    }
                });
            }
            return false;
        });

        viewHolder.mNameEditView.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                Integer index = (Integer) view.getTag();
                viewHolder.mNameEditView.addTextChangedListener(new TextWatcher() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mData.get(index).replace("name",charSequence.toString());
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mData.get(index).replace("name",charSequence.toString());
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void afterTextChanged(Editable editable) {
                        mData.get(index).replace("name",editable.toString());
                    }
                });
                viewHolder.mNameEditView.setOnFocusChangeListener((view12, b) -> {
                    if(!b) {
                        mData.get(index).replace("name",viewHolder.mNameEditView.getText().toString());
                    }
                });

            }
            return false;
        });

        //设置布局中控件要显示的视图
        viewHolder.mEpcTextView.setText(mData.get(position).get("epc"));
        viewHolder.mTypeEditView.setText(mData.get(position).get("type"));
        viewHolder.mNameEditView.setText(mData.get(position).get("name"));
        viewHolder.mRegister_timeTextView.setText(mData.get(position).get("register_time"));



        return convertView;
    }

    private static final class ViewHolder {
        public TextView mEpcTextView;
        public EditText mTypeEditView;
        public EditText mNameEditView;
        public TextView mRegister_timeTextView;
    }


}
