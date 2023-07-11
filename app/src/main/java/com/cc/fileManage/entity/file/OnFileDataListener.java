package com.cc.fileManage.entity.file;

public interface OnFileDataListener {

    void onData(MFile file, String readPath);

    void onNoPermission();

    void onNoExist();
}
