package com.cc.fileManage.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.cc.fileManage.R;
import com.cc.fileManage.entity.TEXFile;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.List;

public class ConvertTexDialog extends AlertDialog implements View.OnClickListener{

    //图片格式
    private NiceSpinner niceFormatSpinner;
    //图片类型
    private NiceSpinner niceTypeSpinner;
    //生成Mipmap映射
    private CheckBox generate;
    //预乘透明度
    private CheckBox multiplyAlpha;
    //生成备份
    private CheckBox backup;

    private final boolean isFolder;

    //图片格式类型数据
    private List<String> formatSpinnerData;
    private List<String> typeSpinnerData;

    public ConvertTexDialog(@NonNull Context context, boolean isFolder) {
        super(context);
        this.isFolder = isFolder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.convert_tex_dialog);
        //
        initData();
        //
        initView();
    }

    /**
     * 初始化页面
     */
    private void initView(){
        //
        setCancelable(false);
        //两个下拉选择框
        this.niceFormatSpinner = findViewById(R.id.tex_format_spinner);
        this.niceFormatSpinner.attachDataSource(formatSpinnerData);
        this.niceFormatSpinner.setSelectedIndex(formatSpinnerData.size() - 1);

        //type
        this.niceTypeSpinner = findViewById(R.id.tex_type_spinner);
        this.niceTypeSpinner.attachDataSource(typeSpinnerData);
        this.niceTypeSpinner.setSelectedIndex(0);
        //
        this.generate = findViewById(R.id.tex_generate);
        this.multiplyAlpha = findViewById(R.id.tex_multiplyAlpha);
        //
        this.backup = findViewById(R.id.tex_backup);
        TextView tips = findViewById(R.id.tex_tips);
        if(isFolder){
            backup.setVisibility(View.VISIBLE);
            tips.setVisibility(View.VISIBLE);
        }
        //按钮
        //取消 开始按钮
        Button cancel = findViewById(R.id.tex_cancel);
        Button start = findViewById(R.id.tex_start);
        //
        cancel.setOnClickListener(this);
        start.setOnClickListener(this);
    }

    private void initData(){
        this.formatSpinnerData = new ArrayList<>();
        this.formatSpinnerData.add(TEXFile.PixelFormat.DXT1.getName());
        this.formatSpinnerData.add(TEXFile.PixelFormat.DXT3.getName());
        this.formatSpinnerData.add(TEXFile.PixelFormat.DXT5.getName());
        this.formatSpinnerData.add(TEXFile.PixelFormat.ARGB.getName());

        //==================格式
        this.typeSpinnerData = new ArrayList<>();
        this.typeSpinnerData.add(TEXFile.TextureType.OneD.getDes());
        this.typeSpinnerData.add(TEXFile.TextureType.TwoD.getDes());
        this.typeSpinnerData.add(TEXFile.TextureType.ThreeD.getDes());
        this.typeSpinnerData.add(TEXFile.TextureType.CubeMap.getDes());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tex_cancel:   //取消
                dismiss();
                break;
            case R.id.tex_start:    //开始
                if(onStartClickListener != null) {
                    dismiss();
                    //
                    onStartClickListener.onStart(
                            getFormatByIndex(niceFormatSpinner.getSelectedIndex()),
                            getTypeByIndex(niceTypeSpinner.getSelectedIndex()),
                            generate.isChecked(),
                            multiplyAlpha.isChecked(),
                            backup.isChecked());
                }
                break;
        }
    }

    private TEXFile.PixelFormat getFormatByIndex(int index){
        switch(index){
            case 0:
                return TEXFile.PixelFormat.DXT1;
            case 1:
                return TEXFile.PixelFormat.DXT3;
            case 2:
                return TEXFile.PixelFormat.DXT5;
            case 3:
            default:
                return TEXFile.PixelFormat.ARGB;
        }
    }

    private TEXFile.TextureType getTypeByIndex(int index){
        switch(index){
            case 1:
                return TEXFile.TextureType.TwoD;
            case 2:
                return TEXFile.TextureType.ThreeD;
            case 3:
                return TEXFile.TextureType.CubeMap;
            case 0:
            default:
                return TEXFile.TextureType.OneD;
        }
    }

    private OnStartClickListener onStartClickListener;

    //===========
    public interface OnStartClickListener{
        void onStart(TEXFile.PixelFormat format, TEXFile.TextureType type,
                     boolean isGenerate, boolean isMultiplyAlpha, boolean isBackup);
    }

    public void setOnStartClickListener(OnStartClickListener onStartClickListener) {
        this.onStartClickListener = onStartClickListener;
    }
}
