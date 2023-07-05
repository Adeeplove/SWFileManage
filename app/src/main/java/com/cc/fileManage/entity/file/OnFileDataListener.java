package com.cc.fileManage.entity.file;

public interface OnFileDataListener {

    void onData(ManageFile file, String readPath);

    void onNoPermission();

    void onNoExist();
}
