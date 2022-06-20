package com.cc.fileManage.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.cc.fileManage.databinding.ActivityEditBinding;

import java.util.Objects;

public class EditActivity extends BaseActivity {

    private ActivityEditBinding binding;

    //file
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //viewBind
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //返回键
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        
        //文件路径
        this.filePath = getIntent().getStringExtra("path");
        if(filePath != null) {
//            StringBuilder content = CFileUtils.readFile(filePath, "UTF-8");
//            binding.editor.setText(content.toString());
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                //退出提示
//                if(binding.codeEditor.isChange())
//                    saveDialog();
//                else
//                    finish();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存文件
     * @param isExit    是否退出
     */
    private void saveFile(final boolean isExit){
    }

    /**
     * 保存文件提示弹框
     */
    private void saveDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("提示");
        dialog.setMessage("是否保存文件?");
        dialog.setPositiveButton("保存并退出", (dialog13, which) -> saveFile(true));
        dialog.setNegativeButton("保存", (dialog12, which) -> saveFile(false));
        dialog.setNeutralButton("退出", (dialog1, which) -> finish());
        dialog.show();
    }
}
