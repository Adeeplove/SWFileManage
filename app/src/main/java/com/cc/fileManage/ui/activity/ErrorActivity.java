package com.cc.fileManage.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cc.fileManage.MainActivity;
import com.cc.fileManage._static.AppManager;
import com.cc.fileManage.databinding.ActivityErrorBinding;

public class ErrorActivity extends BaseActivity {

    private ActivityErrorBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //viewBind
        binding = ActivityErrorBinding.inflate(getLayoutInflater());
        //
        setContentView(binding.getRoot());
        //
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData(){
        String message = getIntent().getStringExtra("error");
        if(message != null && !TextUtils.isEmpty(message)){
            //set
            binding.errorMessage.setText(message);
        }

        //退出
        binding.errorExit.setOnClickListener(v -> AppManager.getAppManager().finishAllActivity());
        //重启
        binding.errorReboot.setOnClickListener(v -> {
            Intent intent = new Intent(ErrorActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
