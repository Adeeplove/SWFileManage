package com.cc.fileManage.ui;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cc.fileManage.MainActivity;

public abstract class BaseFragment extends Fragment implements MainActivity.OnEventChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMainActivity().setOnEventChangeListener(this);
    }

    @Override
    public abstract void onBack();

    @Override
    public abstract void onUpdate(String path);

    /**
     *
     * @return MainActivity
     */
    protected MainActivity getMainActivity() {
        return ((MainActivity)getActivity());
    }
}
