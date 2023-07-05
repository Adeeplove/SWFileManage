package com.cc.fileManage.ui.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.cc.fileManage.R;
import com.cc.fileManage.entity.file.ManageFile;

import java.io.File;

public class CreateFileView {

    //创建文件
    public static void createFile(Context context, String path, OnCreateFileListener listener) {
        View view = View.inflate(context, R.layout.file_rename,null);
        //
        EditText name = view.findViewById(R.id.rename_edit);
        //
        TextView titleText = view.findViewById(R.id.rename_title);
        titleText.setText("新建");
        ///
        AlertDialog _dialog = new AlertDialog.Builder(context).create();
        _dialog.setView(view);
        _dialog.setButton(AlertDialog.BUTTON_POSITIVE,"文件夹", (p1, p2) -> {});
        _dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "文件", (p1, p2) -> {});
        _dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "取消", (p1, p2) -> {});
        _dialog.show();
        //
        _dialog.getButton(_dialog.BUTTON_POSITIVE)
                .setOnClickListener(p1 -> create(name.getText().toString(),
                        false, context, path, _dialog, listener));
        ///
        _dialog.getButton(_dialog.BUTTON_NEGATIVE)
                .setOnClickListener(p1 -> create(name.getText().toString(),
                        true, context, path, _dialog, listener));
    }

    /**
     * 创建文件/文件夹
     * @param _dialog       弹窗
     * @param fileName      文件名
     * @param isFile        是否是创建文件
     */
    private static void create(String fileName, boolean isFile, Context context, String path,
                        AlertDialog _dialog, OnCreateFileListener listener) {
        //空字符
        if(!TextUtils.isEmpty(fileName)) {
            _dialog.dismiss();
            // 判断
            String rootPath = path.endsWith(File.separator) ? path : path + File.separator;
            ManageFile file = ManageFile.create(context, rootPath + fileName);
            try {
                if(isFile ? file.createFile() : file.mkdir()) {
                    if(listener != null) {
                        listener.onCreateFile(fileName);
                    }
                } else {
                    Toast.makeText(context, "创建失败!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "文件名不能为空!", Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnCreateFileListener{
        void onCreateFile(String name);
    }
}