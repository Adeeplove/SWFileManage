package com.cc.fileManage.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.FileUtils;
import com.cc.fileManage.databinding.ActivityEditBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //文件路径
        this.filePath = getIntent().getStringExtra("path");
        if(filePath != null)
            binding.editor.setText(readFileContent(filePath));
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存文件
     * @param isExit    是否退出
     */
    private void saveFile(boolean isExit){
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

    /**
     * 读取文件内容
     * @param filePath  文件路径
     * @return          文件内容
     */
    private String readFileContent(String filePath) {
        StringBuilder stringBuffer = new StringBuilder();
        File f = new File(filePath);
        try (BufferedReader fileReader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }
}
