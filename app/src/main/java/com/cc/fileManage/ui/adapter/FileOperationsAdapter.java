package com.cc.fileManage.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cc.fileManage.R;
import com.cc.fileManage.entity.MethodValue;
import com.cc.fileManage.module.file.FileOperations;

import java.util.ArrayList;
import java.util.List;

public class FileOperationsAdapter extends RecyclerView.Adapter<FileOperationsAdapter.OperationsHolder> {

    private List<MethodValue<Integer, FileOperations.Method>> data;

    public FileOperationsAdapter(List<MethodValue<Integer,FileOperations.Method>> data) {
        this.data = data == null ? new ArrayList<>() : data;
    }

    public List<MethodValue<Integer, FileOperations.Method>> getData() {
        return data;
    }

    public void setData(List<MethodValue<Integer, FileOperations.Method>> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public OperationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View folder = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_operations_item, parent, false);
        return new OperationsHolder(folder);
    }

    @Override
    public void onBindViewHolder(@NonNull OperationsHolder holder, int position) {
        MethodValue<Integer, FileOperations.Method> value = data.get(position);
        //
        holder.icon.setImageResource(value.getValueOne());
        holder.text.setText(value.getValueTwo().getName());
        //
        holder.parent.setEnabled(value.isHandle());
        if(value.isHandle()) {
            holder.parent.setAlpha(1.0f);
        }else {
            holder.parent.setAlpha(0.2f);
        }
        ///
        holder.parent.setOnClickListener(v -> {
            if(onItemListener != null)
                onItemListener.onClick(value);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class OperationsHolder extends RecyclerView.ViewHolder {
        //图标
        ImageView icon;
        //名称
        TextView text;
        //View
        View parent;

        public OperationsHolder(View view) {
            super(view);
            icon = view
                    .findViewById(R.id.operations_item_icon);
            text = view
                    .findViewById(R.id.operations_item_text);
            parent = view;
        }
    }

    //=============接口==============
    private OnItemListener onItemListener;

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public interface OnItemListener{
        void onClick(MethodValue<Integer,FileOperations.Method> value);
    }
}
