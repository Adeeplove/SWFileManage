package com.cc.fileManage.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WelcomeFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(getContext());
    }

    @Override
    public boolean onCreateMenu(Menu menu) {
        menu.add(Menu.NONE,1000, 0,"Menu");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onBack() {}

    @Override
    public void onUpdate(String path) {
    }
}
