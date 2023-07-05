package com.cc.fileManage.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.R;
import com.cc.fileManage.entity.ImageID;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.ui.views.EllipsizeTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ListViewHolder>
{
    private List<ManageFile> data;

    private String rootPath;

    private OnSearchFilesDataListener onSearchFilesDataListener;

    public SearchListAdapter(){
        this.data = new ArrayList<>();
    }

    public void setOnSearchFilesDataListener(OnSearchFilesDataListener onSearchFilesDataListener)
    {
        this.onSearchFilesDataListener = onSearchFilesDataListener;
    }

    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath.endsWith(File.separator) ? rootPath : rootPath + File.separator;
    }

    public void setData(List<ManageFile> data)
    {
        this.data = data;
    }

    public List<ManageFile> getData()
    {
        return data;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int p2)
    {
        View folder = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_browser_item, parent, false);
        return new ListViewHolder(folder);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int index)
    {
        ManageFile cfile = data.get(index);
        try{
            //设置数据
            holder.name.setText(cfile.getName());
            holder.name.setTag(cfile.getName());
            holder.name.setEllipsizeNoun(TextUtils.TruncateAt.START);
            holder.name.setMaxLines(1);

            String userPath = cfile.getParent() + File.separator;

            if(userPath.equals(rootPath)){
                userPath = "./";
            }
            else{
                userPath = "./" + userPath.substring(rootPath.length());
            }
            holder.time.setText(userPath);
            //
            holder.time.setTag(cfile.getPath());
            holder.time.setEllipsizeNoun(TextUtils.TruncateAt.START);
            holder.time.setMaxLines(1);
        } catch(Exception e){
            ToastUtils.showShort("加载错误!");
        }
        //设置图片
        setIcon(cfile.getName(), holder.icon);
        //设置点击监听
        holder.parent.setOnClickListener(p1 -> onSearchFilesDataListener.updateView(cfile));
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder
    {
        //名称
        EllipsizeTextView name;
        //描述
        EllipsizeTextView time;

        //图标
        ImageView icon;

        //View
        RelativeLayout parent;

        public ListViewHolder(View view) {
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
     * @param filename 文件名
     * @param iv       图片控件
     */
    private void setIcon(String filename, ImageView iv){
        //转成小写
        String path = filename.toLowerCase();

        if (path.endsWith("/") || path.equals(".../返回上一级"))
        {
            iv.setImageResource(ImageID.image_folder);
        }
        else
        {
            if (path.endsWith(".ppt"))
            {
                iv.setImageResource(ImageID.image_ppt);
            }
            else if (path.equals(".apk"))
            {
                iv.setImageResource(ImageID.image_apk);
            }
            else if (path.endsWith(".mp3")
                    || path.endsWith(".ogg")
                    || path.endsWith(".wav"))
            {
                iv.setImageResource(ImageID.image_mp3);
            }
            else if (path.endsWith(".jpg")
                    || path.endsWith(".jpeg")
                    || path.endsWith(".png"))
            {

                iv.setImageResource(ImageID.image_image);
            }
            else if (path.endsWith(".txt")
                    ||path.endsWith(".log"))
            {
                iv.setImageResource(ImageID.image_txt);
            }
            else if (path.endsWith(".html"))
            {
                iv.setImageResource(ImageID.image_html);
            }
            else if (path.endsWith(".zip")
                    || path.endsWith(".7z")
                    || path.endsWith(".rar")
                    || path.endsWith(".gzip")
                    || path.endsWith(".jar"))
            {
                iv.setImageResource(ImageID.image_zip);
            }
            else if (path.endsWith(".pdf"))
            {
                iv.setImageResource(ImageID.image_pdf);
            }
            else if (path.endsWith(".link"))
            {
                iv.setImageResource(ImageID.image_link);
            }
            else if (path.endsWith(".xml"))
            {
                iv.setImageResource(ImageID.image_html);
            }
            else if (path.endsWith(".mp4")
                    || path.endsWith(".rmvb")
                    || path.endsWith(".avi")
                    || path.endsWith(".mtv")
                    || path.endsWith(".wmv"))
            {
                iv.setImageResource(ImageID.image_mv);
            }
            else if (path.endsWith(".doc"))
            {
                iv.setImageResource(ImageID.image_world);
            }
            else if(path.endsWith(".xls")
                    ||path.endsWith(".xmls")){

                iv.setImageResource(ImageID.image_xls);
            }
            else if (path.endsWith(".chm"))
            {
                iv.setImageResource(ImageID.image_chm);
            }
            else
            {
                iv.setImageResource(ImageID.image_null);
            }
        }
    }

    public interface OnSearchFilesDataListener{
        void updateView(ManageFile file);
    }
}