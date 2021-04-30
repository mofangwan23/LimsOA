package cn.flyrise.feep.knowledge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog;
import cn.flyrise.feep.knowledge.adpater.MoveListAdapter;
import cn.flyrise.feep.knowledge.contract.MoveContract;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.model.Folder;
import cn.flyrise.feep.knowledge.model.FolderManager;
import cn.flyrise.feep.knowledge.presenter.MovePresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by KLC on 2016/12/6.
 * 
 */
public class MoveFileAndFolderActivity extends BaseActivity implements MoveContract.View, RenameCreateContract.View {


    public static void startMoveActivity(Context context, String parentFolderID, String moveFolders, String moveFiles, ArrayList<String> fileType, FolderManager manager) {
        Intent intent = new Intent(context, MoveFileAndFolderActivity.class);
        intent.putExtra(KnowKeyValue.EXTRA_MOVEFOLDERID, moveFolders);
        intent.putExtra(KnowKeyValue.EXTRA_MOVEFILEID, moveFiles);
        intent.putExtra(KnowKeyValue.EXTRA_FOLDERMANAGER, manager);
        intent.putExtra(KnowKeyValue.EXTRA_MOVEPARENTID, parentFolderID);
        intent.putStringArrayListExtra(KnowKeyValue.EXTRA_FILETYPE, fileType);
        ((Activity) context).startActivityForResult(intent, KnowKeyValue.STARTMOVECODE);
    }

    private FEToolbar mToolbar;
    private TextView mTvPathView;
    private FloatingActionsMenu mDetailMenu;
    private FloatingActionButton mNewFolderIcon;
    private PullAndLoadMoreRecyclerView mListView;

    private MoveListAdapter mAdapter;
    private MoveContract.Presenter mPresenter;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.knowledge_move_file);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        this.mToolbar = toolbar;
        mToolbar.setLineVisibility(View.GONE);
        mToolbar.setTitle(R.string.know_select_folder);
        mToolbar.setRightText(R.string.permission_text_confirm);
        mToolbar.setRightIconVisibility(View.GONE);
    }

    @Override
    public void bindView() {
        super.bindView();
        mListView = (PullAndLoadMoreRecyclerView) findViewById(R.id.listview);
        mTvPathView = (TextView) findViewById(R.id.path_textview);
        mDetailMenu = (FloatingActionsMenu) findViewById(R.id.moreaction_menu);
        mNewFolderIcon = (FloatingActionButton) findViewById(R.id.newfloder_icon);
    }

    @Override
    public void bindData() {
        super.bindData();
        mHandler = new Handler();
        String mMoveFolderIDs = getIntent().getStringExtra(KnowKeyValue.EXTRA_MOVEFOLDERID);
        String mMoveFileIDs = getIntent().getStringExtra(KnowKeyValue.EXTRA_MOVEFILEID);
        String mParentFolderID = getIntent().getStringExtra(KnowKeyValue.EXTRA_MOVEPARENTID);
        ArrayList<String> fileType = getIntent().getStringArrayListExtra(KnowKeyValue.EXTRA_FILETYPE);
        FolderManager manager = getIntent().getParcelableExtra(KnowKeyValue.EXTRA_FOLDERMANAGER);
        mPresenter = new MovePresenterImpl(this, this, manager, mParentFolderID, mMoveFolderIDs, mMoveFileIDs, fileType);
        mHandler.postDelayed(() -> mPresenter.start(), 500);
        mAdapter = new MoveListAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        mListView.setRefreshListener(() -> mPresenter.refreshListData());
        mAdapter.setOnItemClickListener((view, object) -> {
            Folder openFolder = (Folder) object;
            mPresenter.openFolder(openFolder.id, openFolder.name);
        });
        mToolbar.setRightTextClickListener(v -> mPresenter.moveFileAndFolder());
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mNewFolderIcon.setOnClickListener(v -> mPresenter.createFolder());

    }

    @Override
    public void showRefreshLoading(boolean show) {
        if (show) {
            mListView.setRefreshing(true);
        }
        else {
            mHandler.postDelayed(() -> mListView.setRefreshing(false), 500);
        }
    }

    @Override
    public void refreshListData(List<Folder> dataList) {
        mAdapter.refreshData(dataList);
        setEmptyView();
    }

    @Override
    public void setEmptyView() {
//        if (mAdapter.getItemCount() == 0) {
//            mIvEmptyView.setVisibility(View.VISIBLE);
//        }
//        else {
//            mIvEmptyView.setVisibility(View.GONE);
//        }
    }

    @Override
    public void showInputDialog(int titleResourceID, int hintResourceID, String checkBoxText, FEMaterialEditTextDialog.OnClickListener onClickListener) {
        new FEMaterialEditTextDialog.Builder(this)
                .setTitle(getString(titleResourceID))
                .setHint(getString(hintResourceID))
                .setPositiveButton(null, onClickListener)
                .setNegativeButton(null, null)
                .build()
                .show();
    }

    @Override
    public void refreshList() {
    }

    @Override
    public void refreshListByNet() {
        mPresenter.refreshListData();
    }

    @Override
    public void dealComplete() {
        setResult(RESULT_OK);
        finish();
    }

    @Override public void showErrorMessage(String errorMessage) {
        FEToast.showMessage(errorMessage);
    }

    @Override
    public void setPathText(String pathText) {
        mTvPathView.setText(pathText);
    }

    @Override
    public void setButtonEnable(boolean canMove, boolean canCreate) {
        TextView rightTextView = mToolbar.getRightTextView();
        if (canMove) {
            rightTextView.setEnabled(true);
            rightTextView.setTextColor(Color.BLACK);
        }
        else {
            rightTextView.setEnabled(false);
            rightTextView.setTextColor(Color.GRAY);
        }
        mDetailMenu.collapse();
        if (canCreate) {
            mDetailMenu.setEnabled(true);
            mDetailMenu.setAlpha(1);
        }
        else {
            mDetailMenu.setEnabled(false);
            mDetailMenu.setAlpha(0.3f);
        }
    }


    @Override
    public void showDealLoading(boolean show) {
        if (show) {
            LoadingHint.show(this);
        }
        else {
            LoadingHint.hide();
        }
    }

    @Override
    public void showMessage(int resourceID) {
        FEToast.showMessage(getString(resourceID));
    }

    @Override
    public void onBackPressed() {
        mPresenter.backToParent();
    }
}
