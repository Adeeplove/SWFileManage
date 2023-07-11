package com.cc.fileManage.ui.activity;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.cc.fileManage.databinding.ActivityEditBinding;
import com.cc.fileManage.task.StandardMsgTask;

import java.io.File;

public class EditActivity extends BaseActivity {

    private ActivityEditBinding binding;

    //file
    private Uri uri;

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
        this.uri = getIntent().getParcelableExtra("uri");
        if(uri != null)
            readFileContent();
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
     */
    private void readFileContent() {
        StandardMsgTask task = new StandardMsgTask(this, new StandardMsgTask.OnCompleteListener() {
            @Override
            public String doMethod() {
                try {
                    String suffix = uri.getLastPathSegment().toLowerCase();
                    if(uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                        File file = new File(uri.getPath());
                        ///
                        if(file.exists() && file.isFile() && file.canRead()) {
//                            FileInputStream inputStream = new FileInputStream(file);
                        }
                    } else {
//                        @SuppressLint("Recycle")
//                        InputStream inputStream = getContentResolver().openInputStream(uri);
                    }
                } catch (Exception e) {
                   e.printStackTrace();
                }
                return "";
            }
            @Override
            public void success(String msg, float time) {
                binding.editor.setText(msg);
            }
        });
        task.execute();
    }
}
