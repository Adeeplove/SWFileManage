package com.cc.fileManage.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
        return ((MainActivity)requireActivity());
    }

    private AlertDialog dialog;
    protected void showMessage( String title, String msg) {
        dismiss();
        dialog = new AlertDialog.Builder(requireContext()).create();
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", (DialogInterface.OnClickListener) null);
        dialog.show();
    }

    protected void dismiss() {
        if(dialog != null)
            dialog.dismiss();
    }
}
