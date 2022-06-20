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
     * @param context
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

    public static void showDialog(Context context, String title, String meaasge){
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.getWindow().setDimAmount(0f);
        dialog.setTitle(title);
        dialog.setMessage(meaasge);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1, int p2) {
            }
        });
        dialog.show();
    }

    public static AlertDialog.Builder showDialogAndExit(Context context, String title, String meaasge){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(meaasge);

        return dialog;
    }
}