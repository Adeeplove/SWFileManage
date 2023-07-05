package com.cc.fileManage.ui.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ClipboardUtils;
import com.cc.fileManage.R;

import java.lang.ref.WeakReference;

/**
 * 重命名文件视图类
 * @time 2021/08/21
 */
public class RenameFileView {
    private final WeakReference<Context> weakReference;

    private boolean isShowPaste;
    ///
    private int inputType = EditorInfo.TYPE_CLASS_TEXT;
    //
    private OnRenameFileListener onRenameFileListener;

    public RenameFileView(Context context) {
        this.weakReference = new WeakReference<>(context);
    }

    public void setOnRenameFileListener(OnRenameFileListener onRenameFileListener) {
        this.onRenameFileListener = onRenameFileListener;
    }

    public void setShowPaste(boolean showPaste) {
        this.isShowPaste = showPaste;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    /**
     * 重命名
     * @param title title
     */
    public void rename(String title, String message, String successStr){
        Context context = weakReference.get();
        if(context == null) return;
        View view = View.inflate(context, R.layout.file_rename,null);
        //设置输入框
        final EditText name = view.findViewById(R.id.rename_edit);
        name.setInputType(inputType);
        TextView textViewTitle = view.findViewById(R.id.rename_title);
        textViewTitle.setText(title);
        //文本
        name.setText(message);
        name.setHint(message);
        //光标
        name.setSelection(message.length());
        //设置view
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setView(view);

        //取消按钮
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"取消", (dialog1, which) -> dialog1.dismiss());
        //确定按钮
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, successStr, (dialog12, which) -> {
        });

        if(isShowPaste){
            //粘贴按钮
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL,"粘贴", (dialog13, which) -> {});
        }

        dialog.show();
        //
        dialog.getButton(dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            //
            String text = name.getText() == null ? "" : name.getText().toString();
            //空字符
            if(TextUtils.isEmpty(text)){
                dialog.dismiss();
                Toast.makeText(context, "内容不能为空!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(onRenameFileListener != null)
                onRenameFileListener.onRenameFileUpdate(text, dialog);
        });
        ///
        if(isShowPaste){
            //
            dialog.getButton(dialog.BUTTON_NEUTRAL)
                    .setOnClickListener(v -> name.setText(ClipboardUtils.getText() != null ?
                            ClipboardUtils.getText().toString() : ""));
        }
    }

    public interface OnRenameFileListener{
        void onRenameFileUpdate(String newName, AlertDialog dialog);
    }
}
