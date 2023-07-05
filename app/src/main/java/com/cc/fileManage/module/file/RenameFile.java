package com.cc.fileManage.module.file;

import android.content.Context;

import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.utils.CharUtil;

import java.io.File;

public class RenameFile {

    /**
     * 重命名文件
     * @param context       上下文
     * @param file          ManageFile文件
     * @param path          父目录
     * @param newName       新名称
     * @param listener      回调接口
     */
    public static void rename(Context context, ManageFile file, String path,
                              String newName, OnRenameListener listener) {
        ///
        if(!CharUtil.isValidFileName(newName)) {
            listener.onMsg(false, "名称包含特殊字符!");
            return;
        }
        String rootPath = path.endsWith(File.separator) ? path : path + File.separator;
        ManageFile target = ManageFile.create(context, rootPath + newName);
        if(target.exists()) {
            listener.onMsg(false, "文件已存在!");
            return;
        }
        boolean success = file.rename(newName);
        listener.onMsg(success, success ? newName : "重命名失败!");
    }

    public interface OnRenameListener {
        void onMsg(boolean flag, String msg);
    }
}
