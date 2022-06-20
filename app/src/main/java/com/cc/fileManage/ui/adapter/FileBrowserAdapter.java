package com.cc.fileManage.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cc.fileManage.R;
import com.cc.fileManage.entity.ImageID;
import com.cc.fileManage.file.ManageFile;
import com.cc.fileManage.ui.callback.FileItemTouchHelperCallback;
import com.cc.fileManage.ui.views.EllipsizingTextView;

import java.util.ArrayList;
import java.util.List;

public class FileBrowserAdapter extends RecyclerView.Adapter<FileBrowserAdapter.BrowserViewHolder>
        implements FileItemTouchHelperCallback.ItemTouchHelperListener
{
    private final Context context;
    //数据
    private List<ManageFile> data;

    //监听
    private OnItemClickListener onItemClickListener;

    public FileBrowserAdapter(Context context){
        this.context = context;
        this.data = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(List<ManageFile> data) {
        this.data = data;
    }

    public List<ManageFile> getData() {
        return data;
    }

    @NonNull
    @Override
    public BrowserViewHolder onCreateViewHolder(ViewGroup parent, int p2)
    {
        View folder = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_browser_item, parent, false);
        return new BrowserViewHolder(folder);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BrowserViewHolder holder, @SuppressLint("RecyclerView") int index)
    {
        try{
            final ManageFile file = data.get(index);

            //是否被选中
            if(file.isCheck()){
                holder.parent.setBackgroundColor(context.getResources().getColor(R.color.teal_700));
            }else{
                holder.parent.setBackgroundColor(Color.TRANSPARENT);
            }

            //设置点击监听
            holder.parent.setOnClickListener(p1 -> {
                if(onItemClickListener != null)
                    onItemClickListener.onItemClick(file, index);
            });
            //设置点击监听
            holder.parent.setOnLongClickListener(p1 -> {
                if(onItemClickListener != null)
                    onItemClickListener.onItemLongClick(holder.parent, file);
                return true;
            });

            //最大行数
            holder.name.setMaxLines(2);
            //设置名称
            holder.name.setText(file.getFileName());

            //是否高亮
            if(file.isHighlight()){
                //
                holder.name.setTextColor(context.getResources().getColor(R.color.teal_200));
            }else{
                holder.name.setTextColor(context.getResources().getColor(R.color.black));
            }

            //最大行数
            holder.time.setMaxLines(1);
            holder.time.setEllipsizeNoun(TextUtils.TruncateAt.START);

            //是否是tag
            if(file.isTag()){
                holder.time.setText("...");
            }else{
                if(file.isDirectory()){
                    holder.time.setText(file.getTimeToString());
                }else{
                    holder.time.setText(file.getTimeToString()+" "+file.getSizeToString());
                }
            }
            //设置图片
            setIcon(file,holder.icon);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    @Override
    public void onItemChecked(RecyclerView.ViewHolder viewHolder, int position) {
        if(data != null){
            ManageFile manageFile = data.get(position);
            //是tag或者是已经选中的
            if(manageFile.isTag() || manageFile.isCheck()){
                //更新item
                notifyItemChanged(position);
                return;
            }
            //选中
            manageFile.setCheck(true);
        }
        //更新item
        notifyItemChanged(position);
        //
        onItemClickListener.onCheckItem();
    }

    //holder
    public static class BrowserViewHolder extends RecyclerView.ViewHolder
    {
        //名称
        EllipsizingTextView name;
        //描述
        EllipsizingTextView time;

        //图标
        ImageView icon;

        //View
        RelativeLayout parent;

        public BrowserViewHolder(View view) {
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

    /**
     * 设置图标
     * @param cfile 文件
     * @param iv    图片控件
     */
    private void setIcon(ManageFile cfile, ImageView iv){
        //转成小写
        if (cfile.isTag() || cfile.isDirectory())
        {
            iv.setImageResource(ImageID.image_folder);
        }
        else
        {
            String name = cfile.getFileName().toLowerCase();
            if (name.endsWith(".ppt"))
            {
                iv.setImageResource(ImageID.image_ppt);
            }
            else if (name.endsWith(".apk"))
            {
                iv.setImageResource(ImageID.image_apk);
            }
            else if (name.endsWith(".mp3")
                    || name.endsWith(".ogg")
                    || name.endsWith(".wav"))
            {
                iv.setImageResource(ImageID.image_mp3);
            }
            else if (name.endsWith(".jpg")
                    || name.endsWith(".jpeg")
                    || name.endsWith(".png"))
            {
                iv.setImageResource(ImageID.image_image);
            }
            else if (name.endsWith(".txt")
                    ||name.endsWith(".log"))
            {
                iv.setImageResource(ImageID.image_txt);
            }
            else if (name.endsWith(".html"))
            {
                iv.setImageResource(ImageID.image_html);
            }
            else if (name.endsWith(".zip")
                    || name.endsWith(".7z")
                    || name.endsWith(".rar")
                    || name.endsWith(".gzip")
                    || name.endsWith(".jar"))
            {
                iv.setImageResource(ImageID.image_zip);
            }
            else if (name.endsWith(".pdf"))
            {
                iv.setImageResource(ImageID.image_pdf);
            }
            else if (name.endsWith(".link"))
            {
                iv.setImageResource(ImageID.image_link);
            }
            else if (name.endsWith(".xml"))
            {
                iv.setImageResource(ImageID.image_html);
            }
            else if (name.endsWith(".mp4")
                    || name.endsWith(".rmvb")
                    || name.endsWith(".avi")
                    || name.endsWith(".mtv")
                    || name.endsWith(".wmv"))
            {
                iv.setImageResource(ImageID.image_mv);
            }
            else if (name.endsWith(".doc"))
            {
                iv.setImageResource(ImageID.image_world);
            }
            else if(name.endsWith(".xls")
                    ||name.endsWith(".xmls")){

                iv.setImageResource(ImageID.image_xls);
            }
            else if (name.endsWith(".chm"))
            {
                iv.setImageResource(ImageID.image_chm);
            }
            else
            {
                iv.setImageResource(ImageID.image_null);
            }
        }
    }

    public interface OnItemClickListener{
        void onItemClick(ManageFile file, int index);
        void onItemLongClick(View item, ManageFile file);
        void onCheckItem();
    }
}
