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

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.R;
import com.cc.fileManage._static.CSetting;
import com.cc.fileManage._static.FileMethodType;
import com.cc.fileManage.databinding.FragmentFileBrowserBinding;
import com.cc.fileManage.db.DBService;
import com.cc.fileManage.entity.BookMark;
import com.cc.fileManage.entity.file.DFileMethod;
import com.cc.fileManage.entity.file.JFile;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.module.FileOperations;
import com.cc.fileManage.task.fileBrowser.FileBrowserLoadTask;
import com.cc.fileManage.task.module.SearchFilesTask;
import com.cc.fileManage.ui.BaseFragment;
import com.cc.fileManage.ui.adapter.BookMarkAdapter;
import com.cc.fileManage.ui.adapter.FileBrowserAdapter;
import com.cc.fileManage.ui.adapter.SearchListAdapter;
import com.cc.fileManage.ui.callback.FileItemTouchHelperCallback;
import com.cc.fileManage.ui.views.CreateFileView;
import com.cc.fileManage.ui.views.ListItemDialog;
import com.cc.fileManage.ui.views.RenameFileView;
import com.cc.fileManage.utils.CharUtil;
import com.cc.fileManage.utils.RPermissionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileBrowserFragment extends BaseFragment implements FileBrowserAdapter.OnItemClickListener
{
    //viewBind
    private FragmentFileBrowserBinding binding;

    private boolean requestAccess = false;                          //?????????????????????
    private LayoutAnimationController layoutAnimationController;    //item????????????

    private FileBrowserLoadTask loadFiles;          //????????????????????????
    private int lastOffset, lastPosition;           //????????????
    private String readFilePath;                    //???????????????

    private boolean isCheckItem;                    //?????????????????????
    private int checkFileNum;                       //?????????????????????

    private int openFileIndex = -1;                 //?????????????????????

    private boolean pasteState;                     //????????????
    private boolean copyOrMove;                     //??????/??????
    private List<ManageFile> waitCopyFile;          //????????????/???????????????

    private FileBrowserAdapter adapter;             //?????????????????????

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFileBrowserBinding.inflate(inflater, container, false);
        //???????????????
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
        else if(openFileIndex != -1){
            ManageFile manageFile = adapter.getData().size() > openFileIndex ? adapter.getData().get(openFileIndex) : null;
            //?????????
            if(manageFile == null){
                updateFileData(getReadFilePath(), null, false, false);
            }else{
                adapter.notifyItemChanged(openFileIndex);
            }
            this.openFileIndex = -1;
        }
    }

    @Override
    public void onBack() {
        if(isParentCanRead()) {
            backParent();   //???????????????
        }else {
            getMainActivity().exit();   //????????????
        }
    }

    @Override
    public void onUpdate(String path) {
        File file = new File(path);
        if(!file.exists()){
            ToastUtils.showShort("???????????????!");
            return;
        }
        if(file.isFile())
            updateFileData(file.getParent()+File.separator);
        else
            updateFileData(file.getPath());
    }

    @Override
    public boolean onCreateMenu(Menu menu) {
        menu.add(Menu.NONE,1000, 0,"????????????");
        menu.add(Menu.NONE,2000, 0,"????????????");
        menu.add(Menu.NONE,3000, 0,"????????????");
        menu.add(Menu.NONE,4000, 0,"????????????").
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
                List<JFile> files = new ArrayList<>();
                files.add(new JFile(getReadFilePath()));
                ///===============
                SearchFilesTask<JFile> searchFilesCallback = new SearchFilesTask<>(getActivity(),
                        files, CSetting.showHiddenFile);
                searchFilesCallback.setOnSearchDataListener(this::showDataView);
                searchFilesCallback.showSearchView("*.*");
                break;
            case 2000:
                showBookMark();     //????????????
                break;
            case 3000:              //????????????
                RenameFileView addBookMark = new RenameFileView(requireContext());
                addBookMark.setOnRenameFileListener((newName, dialog) -> {
                    if(dialog != null) dialog.dismiss();
                    BookMark bookMark = new BookMark(BookMark.Type.Path);
                    bookMark.setName(TextUtils.isEmpty(newName) ? getReadFilePath() : newName);
                    bookMark.setPath(getReadFilePath());
                    if(DBService.getInstance(requireContext()).addBookMark(bookMark)){
                        ToastUtils.showShort("????????????");
                    }else {
                        ToastUtils.showShort("????????????");
                    }
                });
                addBookMark.setShowPaste(true);
                addBookMark.rename("????????????", getReadFilePath(), "??????");
                break;
            case 4000:
                CSetting.showHiddenFile = !item.isChecked();
                CSetting.writeSettingNow(requireContext());
                ToastUtils.showShort(CSetting.showHiddenFile ? "??????.?????????????????????" : "?????????.?????????????????????");
                updateFileData();
                break;

        }
        return true;
    }

    /**
     * ???????????????
     */
    private void initView(){
        //????????????
        initAnim();
        //????????????
        setReadFilePath(PathUtils.getExternalStoragePath() + File.separator);

        //????????????
        binding.fileButton.setVisibility(pasteState ? View.VISIBLE : View.GONE);
        //??????????????????
        binding.fileRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fileRecyclerView.setHasFixedSize(true);

        //??????RecyclerView????????????
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
        //????????????
        adapter.setOnItemClickListener(this);
        //???????????????
        binding.fileRecyclerView.setAdapter(adapter);

        //??????
        ItemTouchHelper.Callback callback = new FileItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.fileRecyclerView);

        // ??????????????????????????????????????????????????????????????????
        binding.fileSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        binding.fileSwipeRefreshLayout.setDistanceToTriggerSync(350);// ????????????????????????????????????????????????????????????
        binding.fileSwipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        binding.fileSwipeRefreshLayout.setOnRefreshListener(this::updateFileData);

        //?????????????????????????????????
        setButtonMethod(FileMethodType.Refresh, FileMethodType.Add, FileMethodType.Back);

        //????????????
        updateFileData();
    }

    /**
     * ?????????????????????
     */
    private void setButtonMethod(Object left, Object center, Object right){
        //??????
        binding.fileRefresh.setTag(left);
        //??????
        binding.fileAdd.setTag(center);
        //??????
        binding.fileCancel.setTag(right);
    }

    /**
     * ?????????????????????
     */
    private void initClick(){
        binding.fileAdd.setOnClickListener(v -> {
            //??????
            if(binding.fileAdd.getTag().equals(FileMethodType.Cancel)){
                //??????
                setFilesCheckState(false);
                changeButtonState();
            }else{
                if(isCheckItem()) return;
                CreateFileView cfv = new CreateFileView(getActivity(), getReadFilePath());
                cfv.setOnCreateFileListener(name ->
                        updateFileData(getReadFilePath(), name, false, false));
                cfv.createFile();
            }
        });
        binding.fileRefresh.setOnClickListener(v -> {
            //??????
            if(binding.fileRefresh.getTag().equals(FileMethodType.Select)){
                //??????
                setFilesCheckState(true);
            }else{
                //??????
                if(isCheckItem()) return;
                updateFileData();
                //??????????????????
                setPasteState(false, false,"", null);
            }
        });
        binding.fileCancel.setOnClickListener(v -> {
            if(binding.fileCancel.getTag().equals(FileMethodType.Reverse)){
                //??????
                reverseElection();
                //??????????????????
                changeButtonState();
            }else {
                //???????????????
                backParent();
            }
        });
        //????????????
        binding.fileButton.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
            alertDialog.setTitle(copyOrMove ? "??????" : "??????");
            alertDialog.setMessage("?????????????????????????");
            alertDialog.setPositiveButton("??????", (dialog, which) -> {
                setPasteState(false, false, "", null);
                if(!copyOrMove) {
                    //??????
                }
            });
            alertDialog.setNegativeButton("??????", null);
            alertDialog.show();
        });
    }

    /**
     * ??????????????????
     */
    private void initAnim() {
        //????????????XML?????????????????????????????????Animation?????????
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.recy_load_item);
        //????????????LayoutAnimationController?????????
        layoutAnimationController = new LayoutAnimationController(animation);
        //??????????????????????????????
        layoutAnimationController.setOrder(LayoutAnimationController.ORDER_NORMAL);
        //?????????????????????????????????
        layoutAnimationController.setDelay(0.2f);
    }

    //????????????
    public void updateFileData(){
        updateFileData(getReadFilePath());
    }

    //??????
    public void updateFileData(String path){
        updateFileData(path, null, true, false);
    }

    /**
     *  ?????????????????????
     * @param path          //???????????????
     * @param showItem      //?????????item
     * @param flash         //????????????
     * @param scrollToTop   //?????????????????????
     */
    public void updateFileData(final String path, String showItem, final boolean flash, final boolean scrollToTop)
    {
        ///=======================
        if(loadFiles != null) loadFiles.cancel(true);
        //
        loadFiles = new FileBrowserLoadTask(requireContext());
        loadFiles.setCanReadSystemPath(flash);
        loadFiles.setIsShowHideFile(CSetting.showHiddenFile);

        loadFiles.setPath(path);
        loadFiles.setShowItem(showItem);

        loadFiles.setOnLoadFilesListener(new FileBrowserLoadTask.OnFileChangeListener(){
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFilesData(List<ManageFile> data, String showItem, int fileIndex) {
                //?????????????????????????????????
                setReadFilePath(path);
                //?????????????????????
                getMainActivity().setSubtitleText(path);
                //????????????
                if(flash){
                    //??????RecyclerView??????
                    binding.fileRecyclerView.setLayoutAnimation(layoutAnimationController);
                }
                //??????????????????
                adapter.setData(data);
                adapter.notifyDataSetChanged();
                //
                changeButtonState();
                if(showItem != null){
                    //?????????????????????
                    scrollTo(fileIndex);
                }else if(scrollToTop){
                    //???????????????
                    scrollToTopPosition();
                }else{
                    //??????????????????????????????
                    scrollToPosition();
                }
                //??????????????????
                setCheckItem(false);
                setCheckFileNum(0);
                //
                if(binding.fileSwipeRefreshLayout.isRefreshing())
                    binding.fileSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void askPathPermission(String dir) {
                //????????????
                requestAccess = true;
                RPermissionUtil rPermissionUtil = new RPermissionUtil();
                rPermissionUtil.askDataPathPermission(getActivity(), dir);
            }
            @Override
            public void onFailure(Exception e) {}
        });
        loadFiles.execute();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemClick(ManageFile file, int index) {
        //?????????tag
        if(file.isTag()){
            //???????????????
            backParent();
        }else{
            //???????????????
            if(isCheckItem){
                if(file.isCheck()){
                    file.setCheck(false);
                    if(--checkFileNum <= 0)
                        setCheckItem(false);
                }else{
                    file.setCheck(true);
                    checkFileNum++;
                }
                adapter.notifyItemChanged(index);
                changeButtonState();
                return;
            }

            //????????????
            if(file.isFile()){
                //?????????????????? ????????????
                if(pasteState) return;
                //
                FileOperations fileMethod = new FileOperations(this, file, index);
                if(!fileMethod.openFile()) {
                    fileMethod.showOperationsView();
                }else {
                    this.openFileIndex = index;
                }
            }else{
                //??????
                updateFileData(file.getFilePath() + File.separator, null, true, true);
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
     * ????????????
     * @param data ??????
     */
    @SuppressLint("SetTextI18n")
    private void showDataView(List<File> data)
    {
        //??????
        View view = requireActivity().getLayoutInflater().inflate(R.layout.file_browser,null);
        //??????
        TextView title = view.findViewById(R.id.file_browser_title);
        title.setText("????????????(" +data.size() +")");

        //RecyclerView
        RecyclerView re = view.findViewById(R.id.file_browser_recy);
        re.setLayoutManager(new LinearLayoutManager(getContext()));
        re.setHasFixedSize(true);

        //???????????????
        AlertDialog builder = new AlertDialog.Builder(requireContext()).create();
        builder.setCancelable(false);
        builder.setView(view);
        builder.setButton(AlertDialog.BUTTON_POSITIVE,"??????", (DialogInterface.OnClickListener) null);
        builder.setButton(AlertDialog.BUTTON_NEUTRAL,"??????????????????", (p1, p2) -> {
            //?
        });
        builder.show();

        //?????????
        SearchListAdapter searchAdapter = new SearchListAdapter();
        searchAdapter.setOnSearchFilesDataListener(file -> {
            builder.dismiss();
            updateFileData(file.getParent() + File.separator, file.getName(),true, false);
        });
        searchAdapter.setData(data);
        searchAdapter.setRootPath(getReadFilePath());
        //???????????????
        re.setAdapter(searchAdapter);
    }

    @Override
    public void onCheckItem(ManageFile manageFile, int position) {
        if(pasteState) {
            setPasteState(false, false, "", null);
        }
        //??????
        manageFile.setCheck(true);
        //??????item
        adapter.notifyItemChanged(position);
        //
        //???????????????
        setCheckItem(true);
        //?????????????????????
        checkFileNum++;
        //??????????????????
        changeButtonState();
    }

    /**
     * ?????????????????????
     * @return ????????????
     */
    public boolean isParentCanRead(){
        return DFileMethod.isParentCanRead(readFilePath);
    }

    /**
     * ?????????????????????
     */
    public void backParent(){
        //???????????????????????????????????? ??????
        if(isCheckItem() || getReadFilePath().equals(File.separator)) return;
        //??????????????? ????????? ???????????????
        if(CharUtil.charNum(getReadFilePath(), File.separator) == 2){
            updateFileData(File.separator);
            return;
        }
        //???????????????
        if(isParentCanRead()){
            updateFileData(new File(readFilePath).getParent() + File.separator);
        }
    }

    /**
     * ??????????????????????????????
     * @return ????????????
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
     * ?????????????????????????????????
     * @param state ??????????????????
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
        }else{
            setCheckItem(false);
            setCheckFileNum(0);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * ??????
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
        //?????????????????????
        if(checkSize <= 0){
            setCheckItem(false);
            setCheckFileNum(0);
        }else{
            setCheckItem(true);
            setCheckFileNum(checkSize);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * ??????????????????
     */
    private boolean isStartAnimation = true;
    public void changeButtonState(){
        //???????????????
        AlphaAnimation anima = new AlphaAnimation(0.2f, 1.0f);
        anima.setDuration(500);// ????????????????????????

        //????????????
        binding.fileButton.setVisibility(pasteState ? View.VISIBLE : View.GONE);
        //?????????????????????
        if(getCheckFileNum() > 0){
            if(isStartAnimation){
                binding.fileRefresh.setImageResource(R.drawable.ic_text_save);
                binding.fileCancel.setImageResource(R.drawable.ic_reverse);
                //??????
                binding.fileRefresh.startAnimation(anima);
                binding.fileCancel.startAnimation(anima);
                //
                //????????????
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anim);
                animation.setFillAfter(true);//?????????true?????????????????????????????????
                binding.fileAdd.startAnimation(animation);
            }
            //????????????????????????
            setButtonMethod(FileMethodType.Select, FileMethodType.Cancel, FileMethodType.Reverse);
            isStartAnimation = false;
        }
        else{
            if(!isStartAnimation){
                binding.fileRefresh.setImageResource(R.drawable.ic_refresh);
                binding.fileCancel.setImageResource(R.drawable.ic_up);
                //??????
                binding.fileRefresh.startAnimation(anima);
                binding.fileCancel.startAnimation(anima);

                //????????????
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anim_left);
                animation.setFillAfter(true);//?????????true?????????????????????????????????
                binding.fileAdd.startAnimation(animation);
            }
            //??????
            setButtonMethod(FileMethodType.Refresh, FileMethodType.Add, FileMethodType.Back);
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
            //??????????????????????????????????????????
            if(waitCopyFile.size() < 1) waitCopyFile.add(file);
        }else {
            waitCopyFile.clear();
            waitCopyFile = null;
        }
    }

    /**
     * ??????RecyclerView????????????
     */
    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.fileRecyclerView.getLayoutManager();
        if(layoutManager != null) {
            //????????????????????????view
            View topView = layoutManager.getChildAt(0);
            if(topView != null) {
                //????????????view?????????????????????
                lastOffset = topView.getTop();
                //?????????View???????????????
                lastPosition = layoutManager.getPosition(topView);
            }
        }
    }

    private void getPositionAndOffset(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.fileRecyclerView.getLayoutManager();
        //????????????????????????view
        if(layoutManager != null) {
            //
            View topView = layoutManager.getChildAt(0);
            if(topView != null) {
                //????????????view?????????????????????
                lastOffset = 0;
                //???????????????top view??????
                if(position == layoutManager.getPosition(topView)){
                    //??????????????????view
                    lastPosition = position - 1;
                }else{
                    lastPosition = position;
                }
                //??????0 ??????
                if(lastPosition < 0){
                    lastPosition = 0;
                }
            }
        }
    }

    /**
     * ???RecyclerView?????????????????????
     */
    private void scrollToPosition() {
        if(binding.fileRecyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) binding.fileRecyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }

    /**
     * ???RecyclerView???????????????
     */
    private void scrollToTopPosition() {
        if(binding.fileRecyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) binding.fileRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        }
    }

    /**
     * ???RecyclerView?????????????????????
     */
    private void scrollTo(int position) {
        if(binding.fileRecyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) binding.fileRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            getPositionAndOffset(position);
        }
    }

    /**
     * ????????????
     */
    private ListItemDialog<BookMarkAdapter> itemDialog;
    private void showBookMark() {
        try {
            List<BookMark> data = DBService.getInstance(requireContext())
                    .queryAllBookMark(BookMark.Type.Path);
            if(data.size() < 1) {
                BookMark bookMark = new BookMark();
                bookMark.setName("????????????");
                bookMark.setPath("????????????");
                bookMark.setDescribe("????????????");
                data.add(bookMark);
            }
            BookMarkAdapter bookMarkAdapter = new BookMarkAdapter(data);
            bookMarkAdapter.setOnItemListener(new BookMarkAdapter.OnItemListener() {
                @Override
                public void onEdit(BookMark bookMark, int index) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("????????????")) {
                        RenameFileView addBookMark = new RenameFileView(requireContext());
                        addBookMark.setOnRenameFileListener((newName, dialog) -> {
                            if(dialog != null) dialog.dismiss();
                            if(!TextUtils.isEmpty(newName)) {
                                bookMark.setName(newName);
                                if(DBService.getInstance(requireContext()).updateBookMark(bookMark)){
                                    ToastUtils.showShort("????????????");
                                    bookMarkAdapter.notifyItemChanged(index);
                                }else {
                                    ToastUtils.showShort("????????????");
                                }
                            }
                        });
                        addBookMark.setShowPaste(true);
                        addBookMark.rename("???????????????", bookMark.getName(), "?????????");
                    }
                }
                @Override
                public void onClick(BookMark bookMark) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("????????????")) {
                        File file = new File(bookMark.getPath());
                        if(!file.exists()){
                            ToastUtils.showShort("???????????????!");
                            return;
                        }
                        if(file.isFile())
                            updateFileData(file.getParent()+File.separator);
                        else
                            updateFileData(file.getPath());
                        if(itemDialog != null) itemDialog.dismiss();
                    }
                }
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public boolean onLongClick(BookMark bookMark, int index) {
                    if(null == bookMark.getDescribe() || !bookMark.getDescribe().equals("????????????")) {
                        if(DBService.getInstance(requireContext()).deleteBookMark(bookMark)){
                            bookMarkAdapter.getData().remove(index);
                            bookMarkAdapter.notifyDataSetChanged();
                            ToastUtils.showShort("????????????");
                        }else {
                            ToastUtils.showShort("????????????");
                        }
                    }
                    return true;
                }
            });
            //
            itemDialog = new ListItemDialog<>(requireContext(), bookMarkAdapter);
            itemDialog.setTitle("????????????");
            itemDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}