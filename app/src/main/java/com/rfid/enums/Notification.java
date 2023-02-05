package com.rfid.enums;

public enum Notification {
    MSG_SUCCESS_SAVE("编辑已保存"),
    MSG_ERROR_PLUG("请先接入扫描机后再进行扫描"),
    MSG_ERROR_OUT("请接入扫描机后再进行搜索"),
    MSG_SUCCESS_CLEAR("已清空搜索条件"),
    MSG_WARN_EMPTY("未搜索到匹配结果"),
    MSG_WARN_NULL("扫描数据为空无法进行搜索"),
    MSG_WARN_EDIT("扫描数据为空无法进行编辑"),
    MSG_DEFAULT("");

    public String msg;
    Notification(String msg) {
        this.msg = msg;
    }
}
