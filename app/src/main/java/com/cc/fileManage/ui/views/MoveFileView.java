package com.cc.fileManage.ui.views;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.R;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.task.fileBrowser.CopyOrMoveFileTask;

import java.util.List;

public class MoveFileView {

    private AlertDialog dialog;
    private TextView textViewTitle;

    /**
     * 显示文件重复提示弹窗
     * @param context   上下文
     * @param message   信息
     * @param fileTask  线程
     */
    private void showDialog(Context context, String message, CopyOrMoveFileTask fileTask){
        if(dialog == null) {
            View view = View.inflate(context, R.layout.file_move,null);
            //设置输入框
            TextView textView = view.findViewById(R.id.move_title);
            textView.setText("文件已存在");
            ///
            textViewTitle = view.findViewById(R.id.move_msg);
            textViewTitle.setText(message);
            /// 单选
            RadioGroup radioGroup = view.findViewById(R.id.move_group);
            //
            CheckBox checkBox = view.findViewById(R.id.move_check);
            ///
            //设置view
            dialog = new AlertDialog.Builder(context).create();
            dialog.setView(view);
            dialog.setCancelable(false);
            //确定按钮
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", (dialog12, which) -> {});
            //取消按钮
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"取消", (dialog1, which) -> {});
            dialog.show();
            //
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                dialog.hide();
                RadioButton button = (RadioButton) radioGroup.getChildAt(0);
                if(checkBox.isChecked()) {
                    if(!button.isChecked()) {
                        // 跳过
                        fileTask.skipAllRun();
                    } else {
                        fileTask.coverAllRun();
                    }
                } else if(!button.isChecked()) {
                    fileTask.skipRun();
                } else {
                    fileTask.coverRun();
                }
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                dialog.hide();
                ///
                fileTask.cancel(true);
                fileTask.skipAllRun();
            });
        } else {
            textViewTitle.setText(message);
            dialog.show();
        }
    }

    /**
     * 关闭弹窗
     */
    private void dismiss() {
        if(dialog != null)
            dialog.dismiss();
    }

    /**
     * 复制/移动文件
     * @param context           上下文
     * @param waitCopyFile      待移动的文件
     * @param readFilePath      目标路径
     * @param copyOrMove        复制还是移动
     * @param listener          回调
     */
    public void copyOrMove(Context context,
                           List<ManageFile> waitCopyFile, String readFilePath, boolean copyOrMove, OnListener listener) {
        ///
        CopyOrMoveFileTask task = new CopyOrMoveFileTask(context, waitCopyFile,
                readFilePath, copyOrMove, new CopyOrMoveFileTask.OnFileExistsListener() {
            @Override
            public void onFileExists(CopyOrMoveFileTask fileTask, String filePath) {
                // 提示文件已存在
                showDialog(context, filePath, fileTask);
            }

            @Override
            public void onSuccess(String msg) {
                dismiss();
                if(msg != null) {
                    ToastUtils.showShort(msg);
                }
                listener.onUpdate();
            }

            @Override
            public void onError(String msg) {
                if(msg != null)
                    ToastUtils.showShort("移动失败: " + msg);
            }

            @Override
            public void onCancelled() {
                dismiss();
                listener.onUpdate();
            }
        });
        task.execute();
    }

    public interface OnListener{
        void onUpdate();
    }
}
