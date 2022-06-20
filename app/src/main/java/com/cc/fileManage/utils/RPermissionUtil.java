package com.cc.fileManage.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

public class RPermissionUtil {

    public static final int ANDROID_DATA = 1001;

    private OnButtonClickListener onButtonClickListener;

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    /**
     *
     * @param dir 文件夹
     * @return     字符串
     */
    public String getTipMessage(String dir){
        return "从安卓11开始 第三方软件都不能访问" +
                "<font color=\"blue\"><strong>" +
                "Android/"+dir+"" +
                "</strong></font>目录 "
                +"可以尝试通过授权 来让软件获得访问文件的权限" +
                "点击" +
                "<font color=\"blue\"><strong>"
                +"[确定]" +
                "</strong></font>" +
                "后 在" +
                "<font color=\"red\"><strong>"+
                "弹出的页面下方" +
                "</strong></font>"+
                "直接点击" +
                "<font color=\"blue\"><strong>" +
                "[使用这个文件夹]" +
                "</strong></font>" +
                "允许后 即可完成授权";
    }

    /**
     *
     * @param dir 文件夹
     */
    public void askDataPathPermission(Activity activity, String dir){
        //版本低于android 11 不需要申请这个
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            askDataPathPermission(activity, dir, ANDROID_DATA);
        }
    }

    /**
     * 申请数据目录的权限
     * @param dir 文件夹
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void askDataPathPermission(Activity activity, String dir, int code)
    {
        //内容
        TextView message = new TextView(activity);
        message.setPadding(60,50,60,50);
        message.setTextColor(Color.BLACK);
        message.setTextSize(20);
        message.setText(Html.fromHtml(getTipMessage(dir)));
        message.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.setTitle("注意事项");
        dialog.setView(message);
        dialog.setCancelable(false);

        dialog.setButton(AlertDialog.BUTTON_POSITIVE,"确定", (p1, p2) -> {
            Uri uri = Uri.parse("conten://com.android.externalstorage.documents/tree/primary%3AAndroid%2F" + dir);
            DocumentFile file = DocumentFile.fromTreeUri(activity, uri);
            if(file != null){
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, file.getUri());
                activity.startActivityForResult(intent, code);
            }
        });
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL,"取消", (p1, p2) ->{
            if(onButtonClickListener != null)
                onButtonClickListener.onCancelClick();
        });
        dialog.show();
    }

    public interface OnButtonClickListener{
        void onCancelClick();
    }
}
