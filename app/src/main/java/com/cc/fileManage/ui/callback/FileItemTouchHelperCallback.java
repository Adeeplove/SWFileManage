package com.cc.fileManage.ui.callback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Canvas;

import com.cc.fileManage.ui.adapter.FileBrowserAdapter;

public class FileItemTouchHelperCallback extends ItemTouchHelper.Callback
{
    private final FileBrowserAdapter adapter;

    public FileItemTouchHelperCallback(FileBrowserAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * 设置滑动类型标记
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
    {
        int dragFlags = 0;  // 禁止上下拖动
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;  // 只允许从右向左滑动

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    /**
     * 滑动Item结束执行方法
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemChecked(viewHolder, viewHolder.getAdapterPosition());
    }

    /**
     * 针对swipe状态，swipe滑动的位置超过了百分之多少就消失
     */
    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.3f;
    }
    /**
     * 设置 Item 不支持长按拖动
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    /**
     * 设置 Item 支持滑动
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    /**
     * Item 被选中时候，改变 Item 的背景
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * 移动过程中重新绘制 Item，随着滑动的距离，设置 Item 的透明度
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
    {
        float x = Math.abs(dX);
        float width = viewHolder.itemView.getWidth();

        //滑动超过30%距离
        if((x / width) * 100 >= 30){
            return;
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    /**
     * 用户操作完毕或者动画完毕后调用，恢复 item 的背景和透明度
     */
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
    }

    public interface ItemTouchHelperListener{
        void onItemChecked(RecyclerView.ViewHolder viewHolder, int position);
    }
}