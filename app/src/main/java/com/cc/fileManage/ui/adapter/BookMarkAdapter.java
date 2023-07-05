package com.cc.fileManage.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cc.fileManage.R;
import com.cc.fileManage.entity.BookMark;
import com.cc.fileManage.ui.views.EllipsizeTextView;

import java.util.ArrayList;
import java.util.List;

public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.BookMarkHolder> {

    private List<BookMark> data;

    public BookMarkAdapter(List<BookMark> data) {
        this.data = data == null ? new ArrayList<>() : data;
    }

    public List<BookMark> getData() {
        return data;
    }

    public void setData(List<BookMark> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public BookMarkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View folder = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_browser_item, parent, false);
        return new BookMarkHolder(folder);
    }

    @Override
    public void onBindViewHolder(@NonNull BookMarkHolder holder, int position) {
        BookMark bookMark = data.get(position);
//        holder.icon.setImageResource(R.drawable.ic_link);
        holder.name.setText(bookMark.getName());
        holder.time.setText(bookMark.getPath());
        ///
        holder.icon.setOnClickListener(v -> {
            if(onItemListener != null)
                onItemListener.onEdit(bookMark, position);
        });
        //
        holder.parent.setOnClickListener(v -> {
            if(onItemListener != null)
                onItemListener.onClick(bookMark);
        });
        holder.parent.setOnLongClickListener(v -> {
            if(onItemListener != null)
                return onItemListener.onLongClick(bookMark, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class BookMarkHolder extends RecyclerView.ViewHolder
    {
        //名称
        EllipsizeTextView name;
        //描述
        EllipsizeTextView time;

        //图标
        ImageView icon;

        //View
        RelativeLayout parent;

        public BookMarkHolder(View view) {
            super(view);

            icon = view
                    .findViewById(R.id.file_browser_item_file_icon);
            name = view
                    .findViewById(R.id.file_browser_item_file_name);
            time = view
                    .findViewById(R.id.file_browser_item_file_time);

            parent = view
                    .findViewById(R.id.file_browser_item_parent);
        }
    }

    //=============接口==============
    private OnItemListener onItemListener;

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public interface OnItemListener{
        void onEdit(BookMark bookMark, int index);
        void onClick(BookMark bookMark);
        boolean onLongClick(BookMark bookMark, int index);
    }
}
