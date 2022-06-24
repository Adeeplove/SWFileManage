package com.cc.fileManage.ui.views;

import android.content.Context;
import android.content.DialogInterface;
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
    //触摸消失
    private boolean isCancelable;

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

    public T getAdapter() {
        return adapter;
    }

    public void setAdapter(T adapter) {
        this.adapter = adapter;
    }

    public void show() {
        Context context = weakReference.get();
        if(context == null) return;
        //============
        View view = View.inflate(context, R.layout.list_item_dialog, null);
        RecyclerView recyclerView = view.findViewById(R.id.list_item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        ///=============
        dismiss();
        dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(getTitle());
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
