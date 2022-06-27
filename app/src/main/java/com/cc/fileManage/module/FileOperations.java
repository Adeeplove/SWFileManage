package com.cc.fileManage.module;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.R;
import com.cc.fileManage.entity.MethodValue;
import com.cc.fileManage.entity.file.DFile;
import com.cc.fileManage.entity.file.DFileMethod;
import com.cc.fileManage.entity.file.FileComparator;
import com.cc.fileManage.entity.file.JFile;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.task.apktool.DecodeDexTask;
import com.cc.fileManage.task.fileBrowser.FileBrowserDeleteTask;
import com.cc.fileManage.task.tex.ConvertTexTask;
import com.cc.fileManage.ui.activity.EditActivity;
import com.cc.fileManage.ui.activity.LoadTexActivity;
import com.cc.fileManage.ui.activity.PhotoActivity;
import com.cc.fileManage.ui.adapter.FileOperationsAdapter;
import com.cc.fileManage.ui.browser.FileBrowserFragment;
import com.cc.fileManage.ui.views.ConvertTexDialog;
import com.cc.fileManage.ui.views.ListItemDialog;
import com.cc.fileManage.ui.views.RenameFileView;
import com.cc.fileManage.utils.ApkToolUtil;
import com.cc.fileManage.utils.CharUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import brut.androlib.AndrolibException;
import brut.common.BrutException;

public class FileOperations {

    public enum Method{
        COPY("复制"),
        MOVE("移动"),
        DELETE("删除"),
        RENAME("重命名"),
        METHOD("属性"),
        ZIP("压缩"),
        DECODE("反编译"),
        BUILD("回编译"),
        TEX("Tex转换"),

        TEXT("编辑文本"),
        BROWSER("浏览压缩包"),
        IMAGE("查看图片"),
        MUSIC("播放音乐"),
        APK("Apk信息"),
        TEX_LOOK("Tex查看");

        private final String name;
        Method(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    ///==============================
    private final WeakReference<FileBrowserFragment> weakReference;
    private final ManageFile file;
    private final int fileIndex;

    public FileOperations(FileBrowserFragment fragment, ManageFile file, int fileIndex){
        this.weakReference = new WeakReference<>(fragment);
        this.file = file;
        this.fileIndex = fileIndex;
    }

    //长按操作方法
    private ListItemDialog<FileOperationsAdapter> itemDialog;
    public void showLongOperationsView() {
        ///
        FileBrowserFragment fragment = weakReference.get();
        if(fragment == null || fragment.isHidden()) return;
        ////
        List<MethodValue<Integer, Method>> data = new ArrayList<>();
        data.add(new MethodValue<>(R.drawable.ic_link, Method.COPY));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.METHOD));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.MOVE));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.ZIP));
        ///
        data.add(new MethodValue<>(R.drawable.ic_link, Method.DELETE));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.DECODE, file.isFile()));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.RENAME));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.BUILD, file.isDirectory()));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.TEX, file.isFile()));
        //
        FileOperationsAdapter adapter = new FileOperationsAdapter(data);
        adapter.setOnItemListener(value -> {
            ////
            if(itemDialog != null) itemDialog.dismiss();
            fragment.setOpenFileIndex(fileIndex);
            ////
            switch (value.getValueTwo()) {
                case COPY:
                    copyOrMoveFile(fragment, false);
                    break;
                case MOVE:
                    copyOrMoveFile(fragment, true);
                    break;
                case DELETE:
                    List<ManageFile> check = fragment.getCheckFiles();
                    //没有选中的。就删除长按的这个
                    if(check.size() < 1) check.add(file);
                    ////删除
                    deleteFiles(fragment, check);
                    break;
                case RENAME:
                    if(fragment.isCheckItem()){
                        ToastUtils.showShort("不支持多选!");
                    }else{
                        renameFile(fragment, file);
                    }
                    break;
                case METHOD:
                    break;
                case ZIP:
                    break;
                case DECODE:
                    decodeDex(fragment);
                    break;
                case BUILD:
                    buildDex(fragment);
                    break;
                case TEX:
                    convertTex(fragment);
                    break;
            }
        });
        //
        if(itemDialog != null) itemDialog.dismiss();
        ///==============
        itemDialog = new ListItemDialog<>(fragment.requireContext(), adapter);
        itemDialog.setTitle("");
        itemDialog.setCancelable(true);
        itemDialog.setLayoutManager(new GridLayoutManager(fragment.requireContext(), 2));
        itemDialog.show();
    }

    //点按操作方法
    public void showOperationsView() {
        FileBrowserFragment fragment = weakReference.get();
        if(fragment == null || fragment.isHidden()) return;
        ////
        List<MethodValue<Integer,Method>> data = new ArrayList<>();
        data.add(new MethodValue<>(R.drawable.ic_link, Method.TEXT));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.BROWSER));

        data.add(new MethodValue<>(R.drawable.ic_link, Method.IMAGE));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.MUSIC));
        ///
        data.add(new MethodValue<>(R.drawable.ic_link, Method.APK));
        data.add(new MethodValue<>(R.drawable.ic_link, Method.TEX_LOOK));
        //
        FileOperationsAdapter adapter = new FileOperationsAdapter(data);
        adapter.setOnItemListener(value -> {
            ////
            if(itemDialog != null) itemDialog.dismiss();
            ///====
            Intent intent;
            switch (value.getValueTwo()) {
                case TEXT:
                    fragment.setOpenFileIndex(fileIndex);
                    //文本文件
                    intent = new Intent(fragment.requireContext(), EditActivity.class);
                    intent.putExtra("path", file.getFilePath());
                    fragment.startActivity(intent);
                    break;
                case TEX_LOOK:
                    fragment.setOpenFileIndex(fileIndex);
                    ///===========
                    intent = new Intent(fragment.requireContext(), LoadTexActivity.class);
                    intent.putExtra("path", file.getFilePath());
                    fragment.startActivity(intent);
                    break;
                case IMAGE:
                    fragment.setOpenFileIndex(fileIndex);
                    loadImage(fragment);
                    break;
            }
        });
        //
        if(itemDialog != null) itemDialog.dismiss();
        ///==============
        itemDialog = new ListItemDialog<>(fragment.requireContext(), adapter);
        itemDialog.setTitle("");
        itemDialog.setCancelable(true);
        itemDialog.setLayoutManager(new GridLayoutManager(fragment.requireContext(), 2));
        itemDialog.show();
    }

    /**
     *  打开文件
     */
    public boolean openFile(){
        FileBrowserFragment fragment = weakReference.get();
        if(fragment == null || fragment.isHidden()) return false;
        //后缀名
        String fileSuffix = file.getFileName().toLowerCase();
        if(fileSuffix.endsWith(".txt") || fileSuffix.endsWith(".lua")
            || fileSuffix.endsWith(".log") || fileSuffix.endsWith(".java")){
            //文本文件
            Intent intent = new Intent(fragment.requireContext(), EditActivity.class);
            intent.putExtra("path", file.getFilePath());
            fragment.startActivity(intent);
            return true;
        }
        else if(fileSuffix.endsWith(".tex")){
            //
            Intent intent = new Intent(fragment.requireContext(), LoadTexActivity.class);
            intent.putExtra("path", file.getFilePath());
            fragment.startActivity(intent);
            return true;
        }
        else if(fileSuffix.endsWith(".jpg") || fileSuffix.endsWith(".jpeg") || fileSuffix.endsWith(".png")){
            loadImage(fragment);
            return true;
        }
        return false;
    }

    /**
     * 复制或移动文件
     * @param fragment  FileBrowserFragment
     */
    private void copyOrMoveFile(FileBrowserFragment fragment, boolean isMove) {
        //设置为粘贴状态
        fragment.setPasteState(true, isMove, isMove ? "移动" : "粘贴", file);
        fragment.setFilesCheckState(false);
        fragment.changeButtonState();
    }

    /**
     * 转换tex图片
     * @param fragment  FileBrowserFragment
     */
    private void convertTex(FileBrowserFragment fragment) {
        if(file instanceof DFile) {
            ToastUtils.showShort("不支持的路径");
            return;
        }
        if(fragment.isCheckItem()){
            ToastUtils.showShort("不支持多选!");
        }else{
            //
            ConvertTexDialog convertTexDialog = new ConvertTexDialog(fragment.requireContext(), file.isDirectory());
            convertTexDialog.setOnStartClickListener((format, type, isGenerate, isMultiplyAlpha, isBackup) -> {
                //
                ConvertTexTask convertTexCallback =
                        new ConvertTexTask(fragment.requireContext(), new File(file.getFilePath()));
                convertTexCallback.setPixelFormat(format);
                convertTexCallback.setTextureType(type);
                convertTexCallback.setGenerateMipmaps(isGenerate);
                convertTexCallback.setPreMultiplyAlpha(isMultiplyAlpha);
                convertTexCallback.setConvertListener(succeed -> {
                    if(succeed) {
                        fragment.updateFileData();
                    }else {
                        ToastUtils.showShort("失败!");
                    }
                });
                //执行
                convertTexCallback.execute();
            });
            convertTexDialog.show();
        }
    }

    /**
     * 回编译
     */
    private void buildDex(FileBrowserFragment fragment) {
        DecodeDexTask decodeDexTask = new DecodeDexTask(fragment.requireContext(), new DecodeDexTask.OnCompleteListener() {
            @Override
            public void doMethod() throws BrutException {
                ApkToolUtil.buildDex(fragment.getReadFilePath(), file.getFileName(), Build.VERSION.SDK_INT);
            }
            @Override
            public void success(String msg, float time) {
                if (msg == null) {
                    showMessage(fragment.requireContext(), "完成", "回编译完成 耗时: " + time + "ms");
                    fragment.updateFileData(fragment.getReadFilePath(), "classes.dex", true, false);
                } else {
                    showMessage(fragment.requireContext(), "错误", msg + "\n耗时: " + time + "ms");
                }
            }
        });
        decodeDexTask.execute();
    }

    /**
     * 反编译
     * @param fragment  FileBrowserFragment
     */
    private void decodeDex(FileBrowserFragment fragment) {
        if(file instanceof DFile) {
            ToastUtils.showShort("不支持的路径");
            return;
        }
        DecodeDexTask decodeDexTask = new DecodeDexTask(fragment.requireContext(), new DecodeDexTask.OnCompleteListener() {
            @Override
            public void doMethod() throws AndrolibException {
                File m = ((JFile)file).getFile() == null ? new File(file.getFilePath()) : ((JFile)file).getFile();
                ApkToolUtil.decodeDex(m, m.getParentFile(), Build.VERSION.SDK_INT);
            }
            @Override
            public void success(String msg, float time) {
                if(msg == null) {
                    showMessage(fragment.requireContext(), "完成", "反编译完成 耗时: "+time+"ms");
                    String filename = ((JFile)file).getFile().getName();
                    if (filename.equalsIgnoreCase("classes.dex")) {
                        filename = "smali";
                    } else {
                        filename = "smali_" + filename.substring(0, filename.indexOf("."));
                    }
                    fragment.updateFileData(fragment.getReadFilePath(), filename, true, false);
                }else {
                    showMessage(fragment.requireContext(), "错误", msg+"\n耗时: "+time+"ms");
                }
            }
        });
        decodeDexTask.execute();
    }

    /**
     * 图片浏览器
     * @param fragment  FileBrowserFragment
     */
    private void loadImage(FileBrowserFragment fragment){
        ArrayList<String> images = new ArrayList<>();
        int index = getImageFile(fragment, images);
        /////
        Intent intent = new Intent(fragment.requireContext(), PhotoActivity.class);
        intent.putStringArrayListExtra("image", images);
        intent.putExtra("index", index);
        fragment.startActivity(intent);
    }

    /**
     * 获取图片文件
     * @param fragment  FileBrowserFragment
     * @param images    图片集合
     * @return          首张图片下标
     */
    private int getImageFile(FileBrowserFragment fragment, List<String> images) {
        int index = 0;
        if(file instanceof JFile) {
            File j = ((JFile) file).getFile();
            if(j.getParentFile() != null) {
                File[] listFiles = j.getParentFile().listFiles();
                if(listFiles != null && listFiles.length > 0) {
                    Arrays.sort(listFiles, new FileComparator<>());
                    ////
                    int i = 0;
                    //遍历子文件
                    for(File child : listFiles){
                        //子文件名
                        String endName = child.getName().toLowerCase();
                        if(endName.endsWith(".jpeg")
                                ||endName.endsWith(".webp")
                                ||endName.endsWith(".jpg")
                                ||endName.endsWith(".png")){
                            if(file.getFilePath().equals(child.getPath())){
                                index = i;
                            }
                            images.add(child.getPath());
                            i++;
                        }
                    }
                }
            }
        }else {
            ///父目录
            String path = new File(file.getFilePath()).getParent();
            ///
            List<ManageFile> manageFiles = DFileMethod.documentFileLists(fragment.requireContext(), path);
            if(manageFiles != null && manageFiles.size() > 0) {
                //排序
                Collections.sort(manageFiles, new FileComparator<>());
                ////
                int i = 0;
                //遍历子文件
                for(ManageFile child : manageFiles){
                    //子文件名
                    String endName = child.getFileName().toLowerCase();
                    if(endName.endsWith(".jpeg")
                            ||endName.endsWith(".webp")
                            ||endName.endsWith(".jpg")
                            ||endName.endsWith(".png")){
                        if(file.getFilePath().equals(child.getFilePath())){
                            index = i;
                        }
                        images.add(child.getFilePath());
                        i++;
                    }
                }
            }
        }
        return index;
    }


    /**
     * 修改当前文件的文件名
     */
    public void renameFile(FileBrowserFragment fragment, ManageFile file){
        ///
        RenameFileView renameFileView = new RenameFileView(fragment.requireContext());
        renameFileView.setOnRenameFileListener((newName, dialog) -> {
            //
            if(CharUtil.isValidFileName(newName)){
                File f = new File(fragment.getReadFilePath() + newName);
                if(f.exists()){
                    ToastUtils.showShort("文件已存在!");
                    return;
                }
                if(file instanceof JFile){
                    boolean re = ((JFile) file).getFile().renameTo(f);
                    if(re){
                        fragment.updateFileData(fragment.getReadFilePath(), newName, true, false);
                    }else{
                        ToastUtils.showShort("重命名失败!");
                    }
                } else {
                    //
                    DocumentFile du = DFileMethod.getDocumentFile(fragment.requireContext(), file.getFilePath());
                    try {
                        Uri uri = DocumentsContract.renameDocument(fragment.requireContext()
                                .getContentResolver(), du.getUri(), newName);
                        if(uri != null){
                            fragment.updateFileData(fragment.getReadFilePath(), newName, true, false);
                        }else{
                            ToastUtils.showShort("重命名失败!");
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                dialog.dismiss();
            }else{
                ToastUtils.showShort("名称包含特殊字符!");
            }
        });
        renameFileView.rename("重命名", file.getFileName(), "确定");
    }

    /**
     * 删除文件
     * @param data 文件集合
     */
    private void deleteFiles(FileBrowserFragment fragment, List<ManageFile> data){
        ///
        AlertDialog.Builder ad = new AlertDialog.Builder(fragment.requireContext());
        ad.setTitle("删除");
        if(data.size() > 1){
            ad.setMessage("是否删除选择的"+data.size()+"个文件?");
        }else {
            ad.setMessage("是否删除 " + data.get(0).getFileName() + "?");
        }
        ad.setPositiveButton("删除", (p1, p2) -> {
            FileBrowserDeleteTask delete = new FileBrowserDeleteTask(fragment.requireContext(), data);
            delete.setOnDeleteListener(() -> {
                fragment.setCheckFileNum(0);
                fragment.setCheckItem(false);
                fragment.updateFileData();
            });
            delete.execute();
        });
        ad.setNegativeButton("取消",null);
        ad.show();
    }

    /**
     * 弹窗
     */
    private AlertDialog dialog;
    private void showMessage(Context context, String title, String msg) {
        dismiss();
        dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", (DialogInterface.OnClickListener) null);
        dialog.show();
    }

    private void dismiss() {
        if(dialog != null)
            dialog.dismiss();
    }
}
