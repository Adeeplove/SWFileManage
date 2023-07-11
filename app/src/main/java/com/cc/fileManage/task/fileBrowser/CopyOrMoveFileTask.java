package com.cc.fileManage.task.fileBrowser;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import com.cc.fileManage.entity.file.FileApi;
import com.cc.fileManage.entity.file.MFile;
import com.cc.fileManage.entity.file.OnFileDataListener;
import com.cc.fileManage.task.AsynchronousTask;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 复制或移动文件/夹 支持Android5 - 13 data跟obb目录
 */
public class CopyOrMoveFileTask extends AsynchronousTask<String, String, String> {

    private final WeakReference<Context> weakReference;
    // 待复制/移动文件
    private final ConcurrentLinkedDeque<MFile> data;
    // 文件重复接口
    private final OnFileExistsListener listener;
    // 写出目标路径
    private final String outFilePath;
    // 弹框
    private ProgressDialog dialog;
    // 是否空循环
    private boolean exitTraversal;
    // 是否移动文件
    private final boolean isMove;
    // 消息类型
    private final String MSG = "M", EXIST = "E", ERROR = "ER", SHOW = "S";
    // 是否覆盖 全部覆盖 全部跳过
    private boolean cover = false, coverAll = false, skipAll = false;
    // 待删除的文件
    private final LinkedList<MFile> deletedFile;

    public CopyOrMoveFileTask(Context context, List<MFile> data,
                              String outFilePath, boolean isMove, OnFileExistsListener listener) {
        this.weakReference = new WeakReference<>(context);
        this.data = new ConcurrentLinkedDeque<>(data);
        this.isMove = isMove;
        this.listener = listener;
        this.deletedFile = new LinkedList<>();
        this.outFilePath = outFilePath.endsWith(File.separator) ? outFilePath : outFilePath + File.separator;
    }

    @Override
    protected void onPreExecute() {
        Context context = weakReference.get();
        if(context == null || data.size() < 1) return;
        ///============
        dialog = new ProgressDialog(context);
        dialog.setTitle(isMove ? "移动文件" : "复制文件");
        dialog.setMessage("");
        dialog.setCancelable(false);
        dialog.getWindow().setDimAmount(0f);
        dialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        // 验证
        if(data == null || data.size() < 1 || TextUtils.isEmpty(outFilePath)) {
            return isMove ? "移动失败!" : "复制失败!";
        }
        // 循环处理
        while (!isCancelled()) {
            //// 是否停止遍历
            if(!exitTraversal) {
                MFile source = data.poll();
                if(source == null) {
                    break;
                } else {
                    Context context = weakReference.get();
                    if(context != null) {
                        // 更新消息
                        publishProgress(MSG, source.getPath());
                        // 如果是移动状态
                        if(isMove && source.getTarget() == null && source.isDirectory()) {
                            deletedFile.offer(source);
                        }
                        // 目标路径
                        MFile writeFile = source.getTarget() != null ? source.getTarget() :
                                MFile.create(context, outFilePath + source.getName());
                        // 复制/移动
                        copyOrMove(context, source, writeFile);
                    }
                }
            }
        }
        // 如果是移动状态
        if(isMove) {
            // 清除临时文件
            removeFile();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... msg) {
        switch (msg[0]) {
            case MSG:
                if (dialog != null)
                    dialog.setMessage(msg[1]);
                break;
            case EXIST:
                if (dialog != null) {
                    dialog.hide();
                }
                // 重复提示
                listener.onFileExists(this, msg[1]);
                break;
            case ERROR:
                listener.onError(msg[1]);
                break;
            case SHOW:
                if (dialog != null) {
                    dialog.show();
                }
                break;
        }
    }

    @Override
    protected void onCancelled() {
        if(dialog != null) {
            dialog.dismiss();
        }
        listener.onCancelled();
    }

    @Override
    protected void onPostExecute(String s) {
        if(dialog != null) {
            dialog.dismiss();
        }
        listener.onSuccess(s);
    }

    /**
     * 删除临时文件
     */
    private void removeFile() {
        MFile file;
        while ((file = deletedFile.poll()) != null) {
            if(file.countChild()[0] == 0) {
                file.delete();
            }
        }
    }

    /**
     * 写出部分文件路径
     * @param fullPath  写出路径
     * @return          写出部分文件路径
     */
    private String getOutName(String fullPath) {
        if(fullPath.length() > outFilePath.length()) {
            return fullPath.substring(outFilePath.length());
        }
        return fullPath;
    }

    /**
     * 空循环线程
     * @param manageFile  重复的文件
     */
    private void existPause(MFile manageFile) {
        this.exitTraversal = true;
        this.data.offerFirst(manageFile);
        publishProgress(EXIST, getOutName(manageFile.getTarget().getPath()));
    }

    /**
     * 跳过此文件并继续
     */
    public void skipRun() {
        publishProgress(SHOW);
        this.data.poll();
        this.exitTraversal = false;
    }

    /**
     * 重复文件全部跳过
     */
    public void skipAllRun() {
        this.skipAll = true;
        skipRun();
    }

    /**
     * 此文件执行覆盖操作并继续
     */
    public void coverRun() {
        publishProgress(SHOW);
        this.cover = true;
        this.exitTraversal = false;
    }

    /**
     * 重复文件全部执行覆盖操作
     */
    public void coverAllRun() {
        publishProgress(SHOW);
        this.coverAll = true;
        this.exitTraversal = false;
    }

    /**
     * 复制/移动文件/夹
     * @param source    源文件
     * @param target    目标目录
     */
    private void copyOrMove(Context context, MFile source, MFile target) {
        try {
            if(source.isFile()) {
                // 复制/移动单个文件
                copyFile(context, source, target);
            } else {
                // 是空的
                if(source.getTarget() == null) {
                    // 复制/移动文件夹
                    copyDir(context, source, target);
                } else {
                    // 创建空文件夹
                    createEmptyDir(source, target);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            publishProgress(ERROR, source.getPath());
        }
    }

    /**
     * 复制/移动文件
     * @param context       上下文
     * @param source        源文件
     * @param target        目标文件
     * @throws Exception    Exception
     */
    private void copyFile(Context context, MFile source, MFile target) throws Exception{
        if(FileApi.createOrExistsDir(target.getParentFile())) {
            ///
            if(fileCanWrite(target.exists(), source, target)) {
                // 如果存在且是文件夹
                if(target.exists() && target.isDirectory()) {
                    if(!target.delete()) {
                        return;
                    }
                }
                //////////////////////////////////////////////
                if(isMove) {
                    // 移动文件
                    if(!source.move(context, target)) {
                        publishProgress(ERROR, source.getPath());
                    }
                } else {
                    // 复制文件
                    if(!source.copy(context, target)) {
                        publishProgress(ERROR, source.getPath());
                    }
                }
            }
        }
    }

    /**
     * 复制/移动文件夹
     * @param context   上下文
     * @param file      源文件
     * @param target    目标文件
     */
    private void copyDir(Context context, MFile file, MFile target) {
        // 目录路径
        String destPath = target.getPath() + File.separator;
        ///
        boolean[] haveChild = new boolean[] {true};
        file.listFiles(context, new OnFileDataListener() {
            @Override
            public void onData(MFile f, String readPath) {
                haveChild[0] = false;
                MFile destFile = MFile.create(context, destPath + f.getName());
                if(f.isFile()) {
                    f.setTarget(destFile);
                    data.offerFirst(f);
                } else {
                    //// 文件夹
                    copyDir(context, f, destFile);
                }
            }
            @Override
            public void onNoPermission() {}
            @Override
            public void onNoExist() {}
        });
        // 空文件夹处理
        if(haveChild[0]) {
            boolean exist = target.exists();
            if(exist && target.isFile()) {
                fileCanWrite(true, file, target);
            } else if(!exist) {
                file.setTarget(target);
                data.offerFirst(file);
            }
        }
    }

    /**
     * 创建空文件夹
     * @param file      源文件夹
     * @param target    要创建的文件夹
     */
    private void createEmptyDir(MFile file, MFile target) {
        boolean exist = target.exists();
        // 如果存在且不是文件夹
        if(exist && !target.isDirectory()) {
            if(!target.delete()) {
                return;
            }
        }
        if(!exist) {
            if(target.mkdirs() && isMove) file.delete();
        } else if(isMove) {
            file.delete();
        }
    }

    /**
     * 是否继续写文件
     * @param exist 是否存在
     * @param file  源文件
     * @return      是否继续写
     */
    private boolean fileCanWrite(boolean exist, MFile file, MFile target) {
        // skip
        if(exitTraversal) {
            this.data.offerFirst(file);
            return false;
        }
        // 存在
        if(exist) {
            if(cover || skipAll || coverAll) {
                // 如果需要覆盖
                if(coverAll || cover) {
                    this.cover = false;
                    return true;
                } else {
                    return false;
                }
            } else {
                // set
                file.setTarget(target);
                /// 提示并空循环
                existPause(file);
                return false;
            }
        }
        return true;
    }

    /// 接口回调
    public interface OnFileExistsListener{
        void onFileExists(CopyOrMoveFileTask task, String filePath);
        void onSuccess(String msg);
        void onError(String msg);
        void onCancelled();
    }
}
