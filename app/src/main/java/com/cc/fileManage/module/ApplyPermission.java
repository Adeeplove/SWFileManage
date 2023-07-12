package com.cc.fileManage.module;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.cc.fileManage.App;
import com.cc.fileManage.entity.file.FileApi;

import java.io.File;

public class ApplyPermission {

    public static final int ANDROID_DATA = 1001;

    /**
     * @param tree 文件夹
     * @return     字符串
     */
    public static String getTipMessage(Context context, String tree, String version){
        String colorString = App.isUiMode(context) ? "#FFFFFF" : "#000000";
        return "<font color=\""+colorString+"\">从安卓"+version+"开始 软件无法直接访问" +
                "<strong>"
                +tree+"" +
                "</strong>目录 "
                +"可以尝试通过授权 来让软件获得访问文件的权限" +
                "点击" +
                "<strong>"
                +"[确定]" +
                "</strong>" +
                "后 在" +
                "<strong>"+
                "弹出的页面下方" +
                "</strong>"+
                "直接点击" +
                "<strong>" +
                "[使用这个文件夹]" +
                "</strong>" +
                "允许后 即可完成授权</font>";
    }

    /**
     * 申请数据目录的权限
     * @param tree 文件夹
     */
    public static void applyDataPermission(Activity activity, String tree, OnButtonClickListener listener)
    {
        //版本低于android 11 不需要申请这个
        if(FileApi.isAndroid11()) {
            //内容
            TextView message = new TextView(activity);
            message.setPadding(60,50,60,50);
            message.setTextSize(17);
            message.setText(Html.fromHtml(getTipMessage(activity,
                    tree, FileApi.isAndroid13() ? "13" : "11"), Html.FROM_HTML_MODE_LEGACY));
            message.setMovementMethod(LinkMovementMethod.getInstance());
            //
            AlertDialog dialog = new AlertDialog.Builder(activity).create();
            dialog.setTitle("注意事项");
            dialog.setView(message);
            dialog.setCancelable(false);
            ///
            dialog.setButton(AlertDialog.BUTTON_POSITIVE,"确定", (p1, p2) -> {
                String fullPath = FileApi.getExternalStorageDirectory() + File.separator + tree;
                Uri uri = FileApi.getDocumentUri(fullPath);
                if(uri != null){
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
                    activity.startActivityForResult(intent, ANDROID_DATA);
                }
            });
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL,"取消", (p1, p2) ->{
                if(listener != null)
                    listener.cancel();
            });
            dialog.show();
        }
    }

    public interface OnButtonClickListener{
        void cancel();
    }
}
