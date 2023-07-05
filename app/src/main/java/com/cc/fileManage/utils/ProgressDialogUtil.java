package com.cc.fileManage.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.content.DialogInterface;
import com.cc.fileManage.R;

/**
 * 耗时对话框工具类
 */
public class ProgressDialogUtil {

    /**
     * 弹出耗时对话框
     * @param context   上下文
     */
    public static AlertDialog showProgressDialog(Context context)
    {
        AlertDialog mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();

        View loadView = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog_view, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();

        return mAlertDialog;
    }

    public static void showDialog(Context context, String title, String message){
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.getWindow().setDimAmount(0f);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", (DialogInterface.OnClickListener) null);
        dialog.show();
    }

    public static AlertDialog.Builder createDialog(Context context, String title, String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        return dialog;
    }
}