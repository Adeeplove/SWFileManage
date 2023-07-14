package com.cc.fileManage.module.file;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.App;
import com.cc.fileManage.R;
import com.cc.fileManage.entity.MethodValue;
import com.cc.fileManage.entity.file.DFile;
import com.cc.fileManage.entity.file.MFile;
import com.cc.fileManage.task.StandardMsgTask;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
        MERGE("拼图"),
        SPLIT("切图"),

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
    private final MFile file;
    private final int fileIndex;

    public FileOperations(FileBrowserFragment fragment, MFile file, int fileIndex){
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
        //
        int icon = App.isUiMode(fragment.requireContext()) ? R.drawable.ic_link_night : R.drawable.ic_link;
        ////
        List<MethodValue<Integer, Method>> data = new ArrayList<>();
        data.add(new MethodValue<>(icon, Method.COPY));
        data.add(new MethodValue<>(icon, Method.METHOD));
        data.add(new MethodValue<>(icon, Method.MOVE));
        data.add(new MethodValue<>(icon, Method.ZIP));
        ///
        data.add(new MethodValue<>(icon, Method.DELETE));
        data.add(new MethodValue<>(icon, Method.DECODE, file.isFile()));
        data.add(new MethodValue<>(icon, Method.RENAME));
        data.add(new MethodValue<>(icon, Method.BUILD, file.isDirectory()));
        data.add(new MethodValue<>(icon, Method.TEX, file.isFile()));
        //
        data.add(new MethodValue<>(icon, Method.MERGE, file.isFile()));
        data.add(new MethodValue<>(icon, Method.SPLIT, file.isFile()));
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
                    List<MFile> check = fragment.getCheckFiles();
                    //没有选中的。就删除长按的这个
                    if(check.size() < 1) check.add(file);
                    ////删除
                    deleteFiles(fragment, check);
                    break;
                case RENAME:
                    if(fragment.isCheckItem()){
                        ToastUtils.showShort("不支持多选!");
                    } else{
                        renameFile(fragment, file);
                    }
                    break;
                case METHOD:
                    break;
                case ZIP:
                    break;
                case DECODE:
                    if(file.getName().equals("AndroidManifest.xml")) {
                        decodeAXml(fragment);
                    } else {
                        decodeDex(fragment);
                    }
                    break;
                case BUILD:
                    buildDex(fragment);
                    break;
                case TEX:
                    convertTex(fragment);
                    break;
                case MERGE:
                    List<MFile> png = fragment.getCheckFiles();
                    if(png.size() > 0) {
                        mergePng(fragment, png);
                    } else {
                        ToastUtils.showShort("至少需要一张图片!");
                    }
                    break;
                case SPLIT:
                    List<MFile> pngXml = fragment.getCheckFiles();
                    if(pngXml.size() == 2) {
                        StandardMsgTask task = new StandardMsgTask(fragment.requireContext(), new StandardMsgTask.OnCompleteListener() {
                            @Override
                            public String doMethod() {
                                SplitPng.splitPng(pngXml);
                                return null;
                            }
                            @Override
                            public void success(String msg, float time) {
                                ToastUtils.showShort("执行完成!");
                                fragment.setCheckFileNum(0);
                                fragment.setCheckItem(false);
                                fragment.updateFileData();
                            }
                        });
                        task.execute();
                    } else {
                        ToastUtils.showShort("请选择图片跟对应的xml!");
                    }
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
        int icon = App.isUiMode(fragment.requireContext()) ? R.drawable.ic_link_night : R.drawable.ic_link;
        List<MethodValue<Integer,Method>> data = new ArrayList<>();
        data.add(new MethodValue<>(icon, Method.TEXT));
        data.add(new MethodValue<>(icon, Method.BROWSER));

        data.add(new MethodValue<>(icon, Method.IMAGE));
        data.add(new MethodValue<>(icon, Method.MUSIC));
        ///
        data.add(new MethodValue<>(icon, Method.APK));
        data.add(new MethodValue<>(icon, Method.TEX_LOOK));
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
                    intent.putExtra("path", file.getPath());
                    fragment.startActivity(intent);
                    break;
                case TEX_LOOK:
                    fragment.setOpenFileIndex(fileIndex);
                    ///===========
                    intent = new Intent(fragment.requireContext(), LoadTexActivity.class);
                    intent.putExtra("path", file.getPath());
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
        String fileSuffix = file.getName().toLowerCase();
        if(fileSuffix.endsWith(".txt") || fileSuffix.endsWith(".lua")
            || fileSuffix.endsWith(".log") || fileSuffix.endsWith(".java")){
            //文本文件
            Intent intent = new Intent(fragment.requireContext(), EditActivity.class);
            intent.putExtra("path", file.getPath());
            fragment.startActivity(intent);
            return true;
        }
        else if(fileSuffix.endsWith(".tex")){
            //
            Intent intent = new Intent(fragment.requireContext(), LoadTexActivity.class);
            intent.putExtra("path", file.getPath());
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
     * 合并png图片
     * @param fragment  f
     */
    private void mergePng(FileBrowserFragment fragment, List<MFile> png) {
        RenameFileView renameFileView = new RenameFileView(fragment.requireContext());
        renameFileView.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        renameFileView.setOnRenameFileListener((newName, dialog) -> {
            dialog.dismiss();
            StandardMsgTask task = new StandardMsgTask(fragment.requireContext(), new StandardMsgTask.OnCompleteListener() {
                @Override
                public String doMethod() {
                    MergePng.mergePng(png, Integer.parseInt(newName));
                    return null;
                }
                @Override
                public void success(String msg, float time) {
                    if(msg != null) {
                        ToastUtils.showShort(msg);
                    } else {
                        ToastUtils.showShort("合并完成!");
                        fragment.setCheckFileNum(0);
                        fragment.setCheckItem(false);
                        fragment.updateFileData();
                    }
                }
            });
            task.execute();
        });
        renameFileView.rename("合并图片", "0", "合并图片");
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
        } else{
            //
            ConvertTexDialog convertTexDialog = new ConvertTexDialog(fragment.requireContext(), file.isDirectory());
            convertTexDialog.setOnStartClickListener((format, type, isGenerate, isMultiplyAlpha, isBackup) -> {
                //
                ConvertTexTask convertTexCallback =
                        new ConvertTexTask(fragment.requireContext(), file);
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

    ////反编译AndroidManifest.xml
    private void decodeAXml(FileBrowserFragment fragment) {
        ////

    }

    /**
     * 回编译
     */
    private void buildDex(FileBrowserFragment fragment) {

    }

    /**
     * 反编译
     * @param fragment  FileBrowserFragment
     */
    private void decodeDex(FileBrowserFragment fragment) {

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
        List<MFile> manageFiles = fragment.getAdapterData();
        for (MFile f : manageFiles) {
            //子文件名
            String endName = f.getName().toLowerCase();
            if(endName.endsWith(".jpeg")
                    ||endName.endsWith(".webp")
                    ||endName.endsWith(".jpg")
                    ||endName.endsWith(".png")) {
                if(f.getName().equals(file.getName())) {
                    index = images.size();
                }
                images.add(f.getPath());
            }
        }
        return index;
    }


    /**
     * 修改当前文件的文件名
     */
    public void renameFile(FileBrowserFragment fragment, MFile file){
        ///
        RenameFileView renameFileView = new RenameFileView(fragment.requireContext());
        renameFileView.setOnRenameFileListener((newName, dialog) -> {
            //// 重命名
            RenameFile.rename(fragment.requireContext(), file, fragment.getReadFilePath(), newName, (flag, msg) -> {
                if (flag) {
                    fragment.updateFileData(fragment.getReadFilePath(), msg, true, false);
                } else {
                    ToastUtils.showShort(msg);
                }
                ///
                dialog.dismiss();
            });
        });
        renameFileView.rename("重命名", file.getName(), "确定");
    }

    /**
     * 删除文件
     * @param data 文件集合
     */
    private void deleteFiles(FileBrowserFragment fragment, List<MFile> data){
        ///
        AlertDialog.Builder ad = new AlertDialog.Builder(fragment.requireContext());
        ad.setTitle("删除");
        if(data.size() > 1){
            ad.setMessage("是否删除选择的"+data.size()+"个文件?");
        }else {
            ad.setMessage("是否删除 " + data.get(0).getName() + "?");
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
