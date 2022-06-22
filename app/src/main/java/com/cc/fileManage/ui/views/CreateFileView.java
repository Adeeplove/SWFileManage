package com.cc.fileManage.ui.views;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import android.provider.DocumentsContract;
import android.net.Uri;

import com.cc.fileManage.R;
import com.cc.fileManage.entity.file.DFileMethod;
import com.cc.fileManage.utils.CharUtil;

import java.io.FileNotFoundException;

public class CreateFileView {

    private final Activity context;

    private String rootPath;

    private OnCreateFileListener onCreateFileListener;

    public CreateFileView(Activity context, String rootPath){
        this.context = context;
        this.rootPath = rootPath.endsWith(File.separator) ? rootPath : rootPath + File.separator;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setOnCreateFileListener(OnCreateFileListener onCreateFileListener) {
        this.onCreateFileListener = onCreateFileListener;
    }

    //创建文件
    public void createFile(){
        View  view = context.getLayoutInflater().inflate(R.layout.file_rename,null);

        final EditText name = view.findViewById(R.id.rename_edit);

        TextView titleText = view.findViewById(R.id.rename_title);
        titleText.setText("新建");

        final AlertDialog _dialog = new AlertDialog.Builder(context).create();
        _dialog.setView(view);
        _dialog.setButton(AlertDialog.BUTTON_POSITIVE,"文件夹", (p1, p2) -> {
        });
        _dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "文件", (p1, p2) -> {
        });
        _dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "取消", (p1, p2) -> {
        });
        _dialog.show();

        //
        _dialog.getButton(_dialog.BUTTON_POSITIVE).setOnClickListener(p1 -> {
            String text = name.getText() == null ? "" : name.getText().toString();
            //空字符
            if(TextUtils.isEmpty(text)){
                _dialog.dismiss();
                return;
            }
            //验证文件名是否合法
            if(CharUtil.isValidFileName(text)){
                File file = new File(getRootPath() + text);
                try{
                    if(file.exists()){
                        toast("文件已存在!");
                    }
                    //
                    File main = new File(getRootPath());
                    //可读
                    if(main.canRead()){
                        if (file.mkdir()) {
                            onCreateFileListener.onCreateFileUpdate(file.getName());
                        }
                        else{
                            toast("创建失败!");
                        }
                    }else if(DFileMethod.isAndroidDataDir(main)){
                        //android data目录
                        DocumentFile docu = DFileMethod.getDocumentFile(context, getRootPath());
                        if(docu != null){
                            Uri uri = DocumentsContract.createDocument(context.getContentResolver(), docu.getUri(), DocumentsContract.Document.MIME_TYPE_DIR,
                                    text);
                            if(uri != null){
                                onCreateFileListener.onCreateFileUpdate(file.getName());
                            }else{
                                toast("创建失败!");
                            }
                        }
                    }
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }finally{
                    _dialog.dismiss();
                }
            }else{
                toast("文件名包含特殊字符!");
            }
        });
        _dialog.getButton(_dialog.BUTTON_NEGATIVE).setOnClickListener(p1 -> {
            String text = name.getText() == null ? "" : name.getText().toString();
            //
            if(TextUtils.isEmpty(text)){
                _dialog.dismiss();
                return;
            }
            //验证文件名是否合法
            if(CharUtil.isValidFileName(text)){
                File file = new File(getRootPath() + text);
                try {
                    if(file.exists()){
                        toast("文件已存在!");
                    }
                    //
                    File main = new File(getRootPath());
                    //可读
                    if(main.canRead()){
                        System.out.println("创建路径: " + file.getAbsolutePath() );
                        if (file.createNewFile()) {
                            onCreateFileListener.onCreateFileUpdate(file.getName());
                        }
                        else{
                            toast("创建失败!");
                        }
                    }else if(DFileMethod.isAndroidDataDir(main)){
                        //android data目录
                        DocumentFile docu = DFileMethod.getDocumentFile(context, getRootPath());
                        if(docu != null){
                            Uri uri = DocumentsContract.createDocument(context.getContentResolver(), docu.getUri(), "/*",
                                    text);
                            if(uri != null){
                                onCreateFileListener.onCreateFileUpdate(file.getName());
                            }else{
                                toast("创建失败!");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    toast(e.getMessage());
                } finally{
                    _dialog.dismiss();
                }
            }else{
                toast("文件名包含特殊字符!");
            }
        });
    }

    private void toast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public interface OnCreateFileListener{
        void onCreateFileUpdate(String name);
    }
}