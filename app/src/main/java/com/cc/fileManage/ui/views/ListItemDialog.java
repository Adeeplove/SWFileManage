package com.cc.fileManage.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cc.fileManage.R;

import java.lang.ref.WeakReference;

public class ListItemDialog<T extends RecyclerView.Adapter<?>> {

    private final WeakReference<Context> weakReference;
    //
    private T adapter;
    ///
    private AlertDialog dialog;
    private String title = "ListItemDialog";
    private LinearLayoutManager layoutManager;
    //触摸消失
    private boolean isCancelable;
    //
    private int colorId = 0;

    public ListItemDialog(Context context, T adapter) {
        this.weakReference = new WeakReference<>(context);
        this.adapter = adapter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCancelable(boolean cancelable) {
        this.isCancelable = cancelable;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public void setLayoutManager(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public T getAdapter() {
        return adapter;
    }

    public void setAdapter(T adapter) {
        this.adapter = adapter;
    }

    @SuppressLint("ResourceAsColor")
    public void show() {
        Context context = weakReference.get();
        if(context == null) return;
        //============
        View view = View.inflate(context, R.layout.list_item_dialog, null);
        if(colorId != 0) {
            view.setBackgroundResource(colorId);
        }
        RecyclerView recyclerView = view.findViewById(R.id.list_item_list);
        recyclerView.setLayoutManager(layoutManager == null ? new LinearLayoutManager(context) : layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        ///=============
        dismiss();
        dialog = new AlertDialog.Builder(context).create();
        if(!TextUtils.isEmpty(title)) {
            dialog.setTitle(getTitle());
        }
        dialog.setView(view);
        dialog.setCancelable(isCancelable);
        if(!isCancelable) {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,"取消", (DialogInterface.OnClickListener) null);
        }
        dialog.show();
    }

    public void dismiss() {
        if(dialog != null)
            dialog.dismiss();
    }
}
