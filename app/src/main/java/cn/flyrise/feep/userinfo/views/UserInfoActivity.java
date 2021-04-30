package cn.flyrise.feep.userinfo.views;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.kevin.crop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.event.EventUpdataUserIcon;
import cn.flyrise.feep.userinfo.adapter.UserInfoAdapter;
import cn.flyrise.feep.userinfo.contract.UserInfoContract;
import cn.flyrise.feep.userinfo.modle.UserInfoDetailItem;
import cn.flyrise.feep.userinfo.modle.UserModifyData;
import cn.flyrise.feep.userinfo.presenter.UserInfoPresenter;

public class UserInfoActivity extends BaseActivity implements UserInfoContract.View {

    private FEToolbar mToolbar;

    private UserInfoAdapter mAdapter;

    // 剪切后图像文件
    private Uri mDestinationUri;

    private RecyclerView mRecyclerView;

    private UserInfoPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo_detail);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        mToolbar = toolbar;
    }

    @Override
    public void setAdapter(List<UserInfoDetailItem> lists) {
        if (mAdapter == null) {
            mAdapter = new UserInfoAdapter(this, lists);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(bean -> clickItem(bean));
        } else {
            mAdapter.addList(lists);
        }
    }

    private void clickItem(UserInfoDetailItem bean) {
        if (bean == null) {
            return;
        }
        if (K.userInfo.DETAIL_ICON == bean.itemType) {
            startCropActivity(getUriPath(bean.content));
            return;
        }

        Intent intent = new Intent(this, UserInfoModifyActivity.class);
        intent.putExtra("USER_BEAN", GsonUtil.getInstance().toJson(bean));
        startActivityForResult(intent, MODIFY_TEXT);
    }

    private Uri getUriPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        if (path.equals(mDestinationUri.getPath())) {
            return mDestinationUri;
        }
        String address = CoreZygote.getLoginUserServices().getServerAddress();
        String url = address + path;
        Uri uriPath = Uri.parse(url);
        if (uriPath == null) {
            return null;
        }
        return uriPath;
    }

    @Override
    public void bindView() {
        super.bindView();
        mRecyclerView = (RecyclerView) this.findViewById(R.id.user_detail_lv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void bindData() {
        super.bindData();
        mPresenter = new UserInfoPresenter(this);
        String title = getResources().getString(R.string.user_info_title);
        if (mToolbar != null && !TextUtils.isEmpty(title)) {
            mToolbar.setTitle(title);
        }
        mPresenter.initData();
        mDestinationUri = Uri.fromFile(new File(this.getCacheDir(), "cropImage.jpeg"));
        mRecyclerView.setClickable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case MODIFY_TEXT://已修改文本
                mPresenter.modifyView(data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    private void startCropActivity(Uri uri) {
        UCrop.of(uri, mDestinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(312, 312)
                .withTargetActivity(CropActivity.class)
                .start(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBusUpdataUserIcon(EventUpdataUserIcon updata) {
        if (TextUtils.isEmpty(updata.version)) {
            return;
        }
        if (mPresenter == null) {
            return;
        }
        mPresenter.completeAttachment(updata.version);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        UserModifyData modifyData = UserModifyData.getInstance();
        if (modifyData == null) {
            return;
        }
        modifyData.setModifyBean(null);
    }
}