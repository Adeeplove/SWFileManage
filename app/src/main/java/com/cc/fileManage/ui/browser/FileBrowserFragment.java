package com.cc.fileManage.ui.browser;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.App;
import com.cc.fileManage.R;
import com.cc.fileManage._static.CSetting;
import com.cc.fileManage.databinding.FragmentFileBrowserBinding;
import com.cc.fileManage.db.DBService;
import com.cc.fileManage.entity.BookMark;
import com.cc.fileManage.entity.file.FileApi;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.module.ApplyPermission;
import com.cc.fileManage.module.file.FileOperations;
import com.cc.fileManage.task.fileBrowser.FileBrowserLoadTask;
import com.cc.fileManage.task.module.SearchFilesTask;
import com.cc.fileManage.ui.BaseFragment;
import com.cc.fileManage.ui.adapter.BookMarkAdapter;
import com.cc.fileManage.ui.adapter.FileBrowserAdapter;
import com.cc.fileManage.ui.adapter.SearchListAdapter;
import com.cc.fileManage.ui.callback.FileItemTouchHelperCallback;
import com.cc.fileManage.ui.views.CreateFileView;
import com.cc.fileManage.ui.views.ListItemDialog;
import com.cc.fileManage.ui.views.MoveFileView;
import com.cc.fileManage.ui.views.RenameFileView;
import com.cc.fileManage.utils.CharUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileBrowserFragment extends BaseFragment implements FileBrowserAdapter.OnItemClickListener
{
    //viewBind
    private FragmentFileBrowserBinding binding;

    private boolean requestAccess = false;                          //是否申请了权限
    private LayoutAnimationController layoutAnimationController;    //item加载动画

    private FileBrowserLoadTask loadFiles;          //文件数据载入线程
    private int lastOffset, lastPosition;           //滑动位置
    private String readFilePath;                    //访问的路径

    private boolean isCheckItem;                    //是否选中了文件
    private int checkFileNum;                       //选中的文件数量

    private int openFileIndex = -1;                 //打开的文件下标

    private boolean pasteState;                     //等待粘贴
    private boolean copyOrMove;                     //复制/粘贴
    private List<ManageFile> waitCopyFile;          //等待复制/粘贴的文件

    private FileBrowserAdapter adapter;             //列表数据适配器

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileBrowserBinding.inflate(inflater, container, false);
        //初始化页面
        initView();
        //
        initClick();
        //
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ///-===================================
        if(requestAccess){
            requestAccess = false;
            updateFileData(getReadFilePath(), null, false, true);
        }
        else if(openFileIndex != -1){   // 更新某个条目
            ManageFile manageFile = adapter.getData().size() >
                    openFileIndex ? adapter.getData().get(openFileIndex) : null;
            //被删除
            if(manageFile == null){
                updateFileData(getReadFilePath(), null, false, false);
            } else{
                adapter.notifyItemChanged(openFileIndex);
            }
            this.openFileIndex = -1;
        }
        else {
            getMainActivity().setTitleText(getReadFilePath());
        }
        // 设置title
        getMainActivity().setSubtitleText(String.format("文件夹: %s  文件: %s", getDirSize(), getFileSize()));
    }

    @Override
    public void onBack() {
        if(parentCanRead()) {
            backParent();   //返回上一级
        } else {
            getMainActivity().exit();   //双击退出
        }
    }

    @Override
    public void onUpdate(String path) {
        startTo(path);
    }

    @Override
    public boolean onCreateMenu(Menu menu) {
        menu.add(Menu.NONE,1000, 0,"文件查找");
        menu.add(Menu.NONE,2000, 0,"书签列表");
        menu.add(Menu.NONE,3000, 0,"添加书签");
        menu.add(Menu.NONE,4000, 0,"隐藏文件").
                setCheckable(true).setChecked(CSetting.showHiddenFile);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getMainActivity().openDrawer();
                break;
            case 1000:
                //show
                List<ManageFile> files = new ArrayList<>();
                files.add(ManageFile.create(requireContext(), getReadFilePath()));
                ///===============
                SearchFilesTask<ManageFile> searchFilesCallback = new SearchFilesTask<>(getActivity(),
                        files, CSetting.showHiddenFile);
                searchFilesCallback.setOnSearchDataListener(this::showDataView);
                searchFilesCallback.showSearchView("*.*");
                break;
            case 2000:
                showBookMark();     //书签列表
                break;
            case 3000:              //添加书签
                RenameFileView addBookMark = new RenameFileView(requireContext());
                addBookMark.setOnRenameFileListener((newName, dialog) -> {
                    if(dialog != null) dialog.dismiss();
                    BookMark bookMark = new BookMark(BookMark.Type.Path);
                    bookMark.setName(TextUtils.isEmpty(newName) ? getReadFilePath() : newName);
                    bookMark.setPath(getReadFilePath());
                    if(DBService.getInstance(requireContext()).addBookMark(bookMark)){
                        ToastUtils.showShort("添加成功");
                    } else {
                        ToastUtils.showShort("添加失败");
                    }
                });
                addBookMark.setShowPaste(true);
                addBookMark.rename("添加书签", getReadFilePath(), "添加");
                break;
            case 4000:
                CSetting.showHiddenFile = !item.isChecked();
                CSetting.writeSettingNow(requireContext());
                ToastUtils.showShort(CSetting.showHiddenFile ? "显示.开头的隐藏文件" : "不显示.开头的隐藏文件");
                updateFileData();
                break;

        }
        return true;
    }

    public int getDirSize() {
        return loadFiles != null ? loadFiles.getDirSize() : 0;
    }

    public int getFileSize() {
        return loadFiles != null ? loadFiles.getFileSize() : 0;
    }

    /**
     * 初始化页面
     */
    private void initView(){
        //加载动画
        initAnim();
        //读取路径
        setReadFilePath(PathUtils.getExternalStoragePath());

        //粘贴按钮
        binding.fileButton.setVisibility(pasteState ? View.VISIBLE : View.GONE);
        //设置列表属性
        binding.fileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fileRecyclerView.setHasFixedSize(true);

        //监听RecyclerView滚动状态
        binding.fileRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(recyclerView.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });

        adapter = new FileBrowserAdapter(getContext());
        //设置监听
        adapter.setOnItemClickListener(this);
        //设置适配器
        binding.fileRecyclerView.setAdapter(adapter);

        //侧滑
        ItemTouchHelper.Callback callback = new FileItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.fileRecyclerView);

        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        binding.fileSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        binding.fileSwipeRefreshLayout.setDistanceToTriggerSync(350);// 设置手指在屏幕下拉多少距离会触发下拉刷新
        binding.fileSwipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        binding.fileSwipeRefreshLayout.setOnRefreshListener(this::updateFileData);

        //初始化按钮点击触发事件
        setButtonMethod(MethodType.Refresh, MethodType.Add, MethodType.Back);

        //更新数据
        updateFileData();
    }

    /**
     * 设置按钮的方法
     */
    private void setButtonMethod(MethodType left, MethodType center, MethodType right){
        //刷新
        binding.fileRefresh.setTag(left);
        //添加
        binding.fileAdd.setTag(center);
        //返回
        binding.fileCancel.setTag(right);
    }

    /**
     * 初始化点击事件
     */
    private void initClick(){
        binding.fileAdd.setOnClickListener(v -> {
            //取消
            if(binding.fileAdd.getTag().equals(MethodType.Cancel)){
                //取消
                setFilesCheckState(false);
                changeButtonState();
            } else{
                if(isCheckItem()) return;
                CreateFileView.createFile(requireContext(), getReadFilePath(),
                        name -> updateFileData(getReadFilePath(), name, false, false));
            }
        });
        binding.fileRefresh.setOnClickListener(v -> {
            //全选
            if(binding.fileRefresh.getTag().equals(MethodType.Select)){
                //全选
                setFilesCheckState(true);
                // 设置title
                getMainActivity().setSubtitleText(String.
                        format("文件夹: %s  文件: %s  已选: %s", getDirSize(), getFileSize(), (getDirSize() + getFileSize())));
            }else{
                //刷新
                if(isCheckItem()) return;
                updateFileData();
                //取消粘贴状态
                setPasteState(false, false,"", null);
            }
        });
        binding.fileCancel.setOnClickListener(v -> {
            if(binding.fileCancel.getTag().equals(MethodType.Reverse)){
                //反选
                reverseElection();
                //改变按钮状态
                changeButtonState();
            } else {
                //返回上一级
                backParent();
            }
        });
        //复制粘贴
        binding.fileButton.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
            alertDialog.setTitle(copyOrMove ? "移动" : "复制");
            alertDialog.setMessage(copyOrMove ? "是否确定移动文件?" : "是否确定粘贴文件?");
            alertDialog.setPositiveButton("确定", (dialog, which) -> {
                ////
                if(waitCopyFile != null && waitCopyFile.size() > 0) {
                    ManageFile file = ManageFile.create(requireContext(), getReadFilePath());
                    ///
                    if(file.exists() && !file.canWrite()) {
                        // 取消状态
                        setPasteState(false, false, "", null);
                        ToastUtils.showShort("目录无写出权限!");
                        return;
                    }
                    //////////
                    if(file.getESPath().equals(waitCopyFile.get(0).getParentFile().getESPath())) {
                        // 取消状态
                        setPasteState(false, false, "", null);
                        ToastUtils.showShort("文件目录相同!");
                        return;
                    }
                    ////
                    MoveFileView fileView = new MoveFileView();
                    fileView.copyOrMove(requireContext(), waitCopyFile, file.getPath(), copyOrMove, () -> {
                        // 取消状态
                        setPasteState(false, false, "", null);
                        // 更新
                        updateFileData();
                    });
                }
            });
            alertDialog.setNegativeButton("取消", null);
            alertDialog.show();
        });
    }

    /**
     * 列表加载动画
     */
    private void initAnim() {
        //通过加载XML动画设置文件来创建一个Animation对象；
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.recy_load_item);
        //得到一个LayoutAnimationController对象；
        layoutAnimationController = new LayoutAnimationController(animation);
        //设置控件显示的顺序；
        layoutAnimationController.setOrder(LayoutAnimationController.ORDER_NORMAL);
        //设置控件显示间隔时间；
        layoutAnimationController.setDelay(0.2f);
    }

    // 获取列表数据
    public List<ManageFile> getAdapterData() {
        return adapter.getData();
    }

    //更新数据
    public void updateFileData(){
        updateFileData(getReadFilePath());
    }

    //默认
    public void updateFileData(String path){
        updateFileData(path, null, true, false);
    }

    /**
     *  获取文件子文件
     * @param path          //读取的路径
     * @param showItem      //高亮的item
     * @param flash         //是否刷新
     * @param scrollToTop   //是否滑动至顶部
     */
    public void updateFileData(String path, String showItem, boolean flash, boolean scrollToTop)
    {
        /// 验证路径结尾是否有路径分割符
        final String updatePath = path.endsWith(File.separator) ? path : path + File.separator;
        /// 开始加载
        if(loadFiles != null) loadFiles.cancel(true);
        loadFiles = new FileBrowserLoadTask(requireContext());
        loadFiles.setCanReadSystemPath(false);
        loadFiles.setShowHideFile(CSetting.showHiddenFile);
        loadFiles.setPath(updatePath);
        loadFiles.setShowItem(showItem);

        loadFiles.setOnLoadFilesListener(new FileBrowserLoadTask.OnFileChangeListener(){
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFilesData(List<ManageFile> data, String showItem) {
                //重新设置当前访问的路径
                setReadFilePath(updatePath);
                //更新访问的路径
                getMainActivity().setTitleText(updatePath);
                //设置数据
                if(flash){
                    //设置RecyclerView动画
                    binding.fileRecyclerView.setLayoutAnimation(layoutAnimationController);
                }
                //设置数据更新
                adapter.setData(data);
                adapter.notifyDataSetChanged();
                // 取消选择状态
                setCheckItem(false);
                setCheckFileNum(0);
                // 设置按钮状态
                changeButtonState();
                //
                if(showItem != null){
                    //移动到指定位置
                    scrollTo(adapter.getHighlightIndex());
                } else if(scrollToTop){
                    //移动到顶端
                    scrollToTopPosition();
                } else{
                    //移动到上次记录的位置
                    scrollToPosition();
                }
                //
                if(binding.fileSwipeRefreshLayout.isRefreshing())
                    binding.fileSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void askPathPermission(String dir) {
                //获取权限
                requestAccess = true;
                ApplyPermission.applyDataPermission(requireActivity(), dir, null);
            }
            @Override
            public void onFailure(Exception e) {}
            @Override
            public void onPathNoExist(String path) {
                ToastUtils.showShort("加载路径失败!");
            }
        });
        loadFiles.execute();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemClick(ManageFile file, int index) {
        //如果是tag
        if(file.isTag()){
            //返回上一级
            backParent();
        } else{
            //选择了文件
            if(isCheckItem){
                if(file.isCheck()){
                    file.setCheck(false);
                    if(--checkFileNum <= 0)
                        setCheckItem(false);
                } else{
                    file.setCheck(true);
                    checkFileNum++;
                }
                adapter.notifyItemChanged(index);
                changeButtonState();
                return;
            }

            //文件处理
            if(file.isFile()){
                //复制粘贴 直接返回
                if(pasteState) return;
                //
                FileOperations fileMethod = new FileOperations(this, file, index);
                if(!fileMethod.openFile()) {
                    fileMethod.showOperationsView();
                } else {
                    this.openFileIndex = index;
                }
            } else{
                //首位
                updateFileData(file.getPath(), null, true, true);
            }
        }
    }

    @Override
    public void onItemLongClick(ManageFile file, int index) {
        if(file.isTag() || pasteState) return;
        ////
        FileOperations fileMethod = new FileOperations(this, file, index);
        fileMethod.showLongOperationsView();
    }

    /**
     * 搜索结果
     * @param data 数据
     */
    @SuppressLint("SetTextI18n")
    private void showDataView(List<ManageFile> data)
    {
        //页面
        View view = requireActivity().getLayoutInflater().inflate(R.layout.file_browser,null);
        //标题
        TextView title = view.findViewById(R.id.file_browser_title);
        title.setText("搜索结果(" +data.size() +")");

        //RecyclerView
        RecyclerView re = view.findViewById(R.id.file_browser_recy);
        re.setLayoutManager(new LinearLayoutManager(getContext()));
        re.setHasFixedSize(true);

        //用弹框展示
        AlertDialog builder = new AlertDialog.Builder(requireContext()).create();
        builder.setCancelable(false);
        builder.setView(view);
        builder.setButton(AlertDialog.BUTTON_POSITIVE,"关闭", (DialogInterface.OnClickListener) null);
        builder.setButton(AlertDialog.BUTTON_NEUTRAL,"从结果中替换", (p1, p2) -> {
            //?
        });
        builder.show();

        //适配器
        SearchListAdapter searchAdapter = new SearchListAdapter();
        searchAdapter.setOnSearchFilesDataListener(file -> {
            builder.dismiss();
            String parent = file.getParent();
            if(parent != null) {
                updateFileData(parent, file.getName(),true, false);
            }
        });
        searchAdapter.setData(data);
        searchAdapter.setRootPath(getReadFilePath());
        //设置适配器
        re.setAdapter(searchAdapter);
    }

    @Override
    public void onCheckItem(ManageFile manageFile, int position) {
        if(pasteState) {
            setPasteState(false, false, "", null);
        }
        //选中
        manageFile.setCheck(true);
        //更新item
        adapter.notifyItemChanged(position);
        //
        //选中了文件
        setCheckItem(true);
        //选中的文件数量
        checkFileNum++;
        //改变按钮状态
        changeButtonState();
    }

    /**
     * 父目录是否可读
     * @return 是否可读
     */
    public boolean parentCanRead() {
        ManageFile file = ManageFile.create(requireContext(), getReadFilePath());
        /// 父目录是否可读
        return FileApi.isDataDir(file.getParent()) || file.parentCanRead();
    }

    /**
     * 返回上一级目录
     */
    public void backParent(){
        //如果选中或者是最上级目录 返回
        if(isCheckItem() || getReadFilePath().equals(File.separator)) return;
        //第二级目录 点返回 就进根目录
        if(CharUtil.charNum(getReadFilePath(), File.separator) == 2) {
            updateFileData(File.separator);
            return;
        }
        //父目录可读
        if(parentCanRead()){
            updateFileData(new File(readFilePath).getParent());
        }
    }

    /**
     * 获取选中的的文件列表
     * @return 文件集合
     */
    public List<ManageFile> getCheckFiles(){
        List<ManageFile> check = new ArrayList<>();
        for(ManageFile cm : adapter.getData()){
            if(!cm.isTag() && cm.isCheck())
                check.add(cm);
        }
        return check;
    }

    /**
     * 设置所有文件的选择状态
     * @param state 文件选择状态
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setFilesCheckState(boolean state){
        for (ManageFile manageFile : adapter.getData()) {
            if(manageFile.isTag()){
                continue;
            }
            manageFile.setCheck(state);
        }
        //
        if(state){
            setCheckItem(true);
            setCheckFileNum(adapter.getData().size() - 1);
        } else{
            setCheckItem(false);
            setCheckFileNum(0);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 反选
     */
    @SuppressLint("NotifyDataSetChanged")
    private void reverseElection(){
        int checkSize = 0;
        for (ManageFile manageFile : adapter.getData()) {
            if(manageFile.isTag()){
                continue;
            }
            //
            if(manageFile.isCheck()) {
                manageFile.setCheck(false);
            }
            else {
                manageFile.setCheck(true);
                checkSize++;
            }
        }
        //选中的文件数量
        if(checkSize <= 0){
            setCheckItem(false);
            setCheckFileNum(0);
        } else{
            setCheckItem(true);
            setCheckFileNum(checkSize);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 改变按钮状态
     */
    private boolean isStartAnimation = true;
    public void changeButtonState(){
        //透明度动画
        AlphaAnimation anima = new AlphaAnimation(0.2f, 1.0f);
        anima.setDuration(500);// 设置动画显示时间

        //粘贴按钮
        binding.fileButton.setVisibility(pasteState ? View.VISIBLE : View.GONE);
        //选中的文件数量
        int checkSize = getCheckFileNum();
        //是否是暗黑模式
        boolean isUiMode = App.isUiMode(requireContext());
        if(checkSize > 0){
            // 设置title
            getMainActivity().setSubtitleText(String
                    .format("文件夹: %s  文件: %s  已选: %s", getDirSize(), getFileSize(), checkSize));
            if(isStartAnimation){
                binding.fileRefresh.setImageResource
                        (isUiMode ? R.drawable.ic_text_save_night : R.drawable.ic_text_save);
                binding.fileCancel.setImageResource
                        (isUiMode ? R.drawable.ic_reverse_night : R.drawable.ic_reverse);
                //动画
                binding.fileRefresh.startAnimation(anima);
                binding.fileCancel.startAnimation(anima);
                //
                //旋转动画
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anim);
                animation.setFillAfter(true);//设置为true，动画转化结束后被应用
                binding.fileAdd.startAnimation(animation);
            }
            //设置按钮点击方法
            setButtonMethod(MethodType.Select, MethodType.Cancel, MethodType.Reverse);
            isStartAnimation = false;
        } else{
            // 设置title
            getMainActivity().setSubtitleText(String.format("文件夹: %s  文件: %s", getDirSize(), getFileSize()));
            if(!isStartAnimation){
                binding.fileRefresh.setImageResource
                        (isUiMode ? R.drawable.ic_refresh_night : R.drawable.ic_refresh);
                binding.fileCancel.setImageResource
                        (isUiMode ? R.drawable.ic_up_night : R.drawable.ic_up);
                //动画
                binding.fileRefresh.startAnimation(anima);
                binding.fileCancel.startAnimation(anima);

                //旋转动画
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anim_left);
                animation.setFillAfter(true);//设置为true，动画转化结束后被应用
                binding.fileAdd.startAnimation(animation);
            }
            //方法
            setButtonMethod(MethodType.Refresh, MethodType.Add, MethodType.Back);
            isStartAnimation = true;
        }
    }

    public boolean isCheckItem() {
        return isCheckItem;
    }

    public void setCheckItem(boolean checkItem) {
        isCheckItem = checkItem;
    }

    public int getCheckFileNum() {
        return checkFileNum;
    }

    public void setCheckFileNum(int checkFileNum) {
        this.checkFileNum = checkFileNum;
    }

    public String getReadFilePath() {
        return readFilePath;
    }

    public void setReadFilePath(String readFilePath) {
        this.readFilePath = readFilePath;
    }

    public void setOpenFileIndex(int openFileIndex) {
        this.openFileIndex = openFileIndex;
    }

    public void setPasteState(boolean pasteState, boolean copyOrMove, String text, ManageFile file) {
        this.pasteState = pasteState;
        this.copyOrMove = copyOrMove;
        //////
        binding.fileButton.setText(text);
        binding.fileButton.setVisibility(pasteState ? View.VISIBLE : View.GONE);
        ////
        if(pasteState) {
            waitCopyFile = getCheckFiles();
            //没有选中的。就删除长按的这个
            if(waitCopyFile.size() < 1) waitCopyFile.add(file);
        } else {
            if(waitCopyFile != null) {
                waitCopyFile.clear();
                waitCopyFile = null;
            }
        }
    }

    /**
     * 记录RecyclerView当前位置
     */
    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.fileRecyclerView.getLayoutManager();
        if(layoutManager != null) {
            //获取可视的第一个view
            View topView = layoutManager.getChildAt(0);
            if(topView != null) {
                //获取与该view的顶部的偏移量
                lastOffset = topView.getTop();
                //得到该View的数组位置
                lastPosition = layoutManager.getPosition(topView);
            }
        }
    }

    /**
     * 记录RecyclerView当前位置
     */
    private void getPositionAndOffset(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.fileRecyclerView.getLayoutManager();
        //获取可视的第一个view
        if(layoutManager != null) {
            //
            View topView = layoutManager.getChildAt(0);
            if(topView != null) {
                //获取与该view的顶部的偏移量
                lastOffset = 0;
                //数组位置跟top view一致
                if(position == layoutManager.getPosition(topView)){
                    //偏移到上一个view
                    lastPosition = position - 1;
                }else{
                    lastPosition = position;
                }
                //小于0 复位
                if(lastPosition < 0){
                    lastPosition = 0;
                }
            }
        }
    }

    /**
     * 让RecyclerView滚动到指定位置
     */
    private void scrollToPosition() {
        if(binding.fileRecyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) binding.fileRecyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }

    /**
     * 让RecyclerView滚动到首位
     */
    private void scrollToTopPosition() {
        if(binding.fileRecyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) binding.fileRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        }
    }

    /**
     * 让RecyclerView滚动到指定位置
     */
    private void scrollTo(int position) {
        if(binding.fileRecyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) binding.fileRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            getPositionAndOffset(position);
        }
    }

    /**
     * 显示书签
     */
    private ListItemDialog<BookMarkAdapter> itemDialog;
    private void showBookMark() {
        try {
            List<BookMark> data = DBService.getInstance(requireContext())
                    .queryAllBookMark(BookMark.Type.Path);
            if(data.size() < 1) {
                BookMark bookMark = new BookMark();
                bookMark.setName("暂无书签");
                bookMark.setPath("暂无书签");
                bookMark.setDescribe("暂无书签");
                data.add(bookMark);
            }
            BookMarkAdapter bookMarkAdapter = new BookMarkAdapter(data);
            bookMarkAdapter.setOnItemListener(new BookMarkAdapter.OnItemListener() {
                @Override
                public void onEdit(BookMark bookMark, int index) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("暂无书签")) {
                        RenameFileView addBookMark = new RenameFileView(requireContext());
                        addBookMark.setOnRenameFileListener((newName, dialog) -> {
                            if(dialog != null) dialog.dismiss();
                            if(!TextUtils.isEmpty(newName)) {
                                bookMark.setName(newName);
                                if(DBService.getInstance(requireContext()).updateBookMark(bookMark)){
                                    ToastUtils.showShort("修改成功");
                                    bookMarkAdapter.notifyItemChanged(index);
                                }else {
                                    ToastUtils.showShort("修改失败");
                                }
                            }
                        });
                        addBookMark.setShowPaste(true);
                        addBookMark.rename("重命名书签", bookMark.getName(), "重命名");
                    }
                }
                @Override
                public void onClick(BookMark bookMark) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("暂无书签")) {
                        startTo(bookMark.getPath());
                        if(itemDialog != null) itemDialog.dismiss();
                    }
                }
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public boolean onLongClick(BookMark bookMark, int index) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("暂无书签")) {
                        if(DBService.getInstance(requireContext()).deleteBookMark(bookMark)){
                            bookMarkAdapter.getData().remove(index);
                            bookMarkAdapter.notifyDataSetChanged();
                            ToastUtils.showShort("删除成功");
                        }else {
                            ToastUtils.showShort("删除失败");
                        }
                    }
                    return true;
                }
            });
            //
            itemDialog = new ListItemDialog<>(requireContext(), bookMarkAdapter);
            itemDialog.setTitle("书签列表");
            itemDialog.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 跳转路径
     * @param path  路径
     */
    private void startTo(String path) {
        ManageFile file = ManageFile.create(requireContext(), path);
        if(FileApi.isDataDirChild(path) || file.exists()) {
            ////
            if(file.exists()) {
                if(file.isFile())
                    updateFileData(file.getParent());
                else
                    updateFileData(file.getPath());
            } else {
                updateFileData(file.getPath());
            }
        } else {
            ToastUtils.showShort("路径不存在!");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * button tag
     */
    public enum MethodType {
        Refresh,        //刷新
        Add,            //新增
        Back,           //返回
        Cancel,         //取消
        Select,         //全选
        Reverse         //反选
    }
}