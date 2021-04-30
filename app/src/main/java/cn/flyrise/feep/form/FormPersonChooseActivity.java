/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-18 上午11:06:06
 */

package cn.flyrise.feep.form;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView.OnListItemClickListener;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView.OnLoadListener;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.library.view.pulltorefreshlistview.FEPullToRefreshListView;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.PersonSearchActivity;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.event.EventFormSearchPersonChooseData;
import cn.flyrise.feep.form.adapter.FormPersonListAdapter;
import cn.flyrise.feep.form.been.FormPersonCollection;
import cn.flyrise.feep.form.been.PowersType;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-18</br> 修改备注：</br>
 */
public class FormPersonChooseActivity extends NotTranslucentBarActivity {

    private RelativeLayout searchLayout;
    private AddressBookListView showLV;
    private TextView backTV;

    private ListView checkedLV;
    private int filterType;
    private int personType = AddressBookType.Staff;
    private final ArrayList<AddressBookListItem> checkedDatas = new ArrayList<>();

    private boolean isMultiple;
    private FEToolbar mToolBar;
    private FormPersonListAdapter showLVAdapter;

    private CheckPersonObject checkObject;
    private FormPersonListAdapter checkLVAdapter;
    private List<AddressBookItem> nodeItems;

    @Override
    protected void onPause() {
        super.onPause();
        FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.FormPersonChoose);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.FormPersonChoose);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_choose_person);
        showLoadingBar();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBusSearchData(EventFormSearchPersonChooseData data) {
        final AddressBookListItem item = new AddressBookListItem();
        item.setAddressBookItem(data.addingNode);
        checkObject.check(item, FormPersonListAdapter.ShowListAdapterType);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        this.mToolBar = toolbar;
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
                finish();
            }
        });
    }

    @Override
    public void bindView() {
        showLV = (AddressBookListView) findViewById(R.id.form_choose_person_show_lv);
        showLV.setDiverHide();
        backTV = (TextView) findViewById(R.id.form_choose_person_back);
        checkedLV = (ListView) findViewById(R.id.form_choose_person_checked_lv);
        searchLayout = (RelativeLayout) findViewById(R.id.form_choose_person_searchBar);
    }

    @Override
    public void bindData() {
        getIntentData();
        packageCheckedData();
        checkObject = new CheckPersonObject();
        backTV.setText(getResources().getString(R.string.flow_btnback));

        checkLVAdapter = new FormPersonListAdapter(this, FormPersonListAdapter.CheckedListAdapterType, checkObject);
        checkLVAdapter.refreshAdapter(checkedDatas);
        checkedLV.setAdapter(checkLVAdapter);
        setTitle();
        setShowListview();
    }

    /**
     * 设置标题
     */
    private void setTitle() {
        if (personType == AddressBookType.Staff) {
            this.mToolBar.setTitle(R.string.form_choose_node_person);
        } else if (personType == AddressBookType.Position) {
            this.mToolBar.setTitle(R.string.form_choose_node_position);
        } else {
            this.mToolBar.setTitle(R.string.form_choose_node_department);
        }
    }

    /**
     * 获取intent中的数据
     */
    private void getIntentData() {
        final Intent intent = getIntent();
        final JSControlInfo controlInfo = intent.getParcelableExtra("NewFormChooseNodeData");
        if (controlInfo == null) {
            return;
        }
        nodeItems = controlInfo.getNodeItems();
        final PowersType powersType = controlInfo.getPowersType();
        if (powersType == null) {
            return;
        }
        filterType = powersType.getFilterType();
        personType = powersType.getDataSourceType();
        isMultiple = powersType.isMultiple();
    }

    /**
     * 把从intent闯过来的默认选中人员的数据封装成ArrayList<AddressBookListItem>类型
     */
    private void packageCheckedData() {
        if (nodeItems != null) {
            for (final AddressBookItem addressBookItem : nodeItems) {
                final AddressBookListItem item = new AddressBookListItem();
                item.setAddressBookItem(addressBookItem);
                checkedDatas.add(item);
            }
        }
    }

    private void setShowListview() {
        showLVAdapter = new FormPersonListAdapter(this, FormPersonListAdapter.ShowListAdapterType, isMultiple, personType, checkObject);
        showLV.setAdapter(showLVAdapter);
        if (personType == AddressBookType.Staff || personType == AddressBookType.Position) {
            showLV.setPostToCurrentDepartment(true);
        }
        showLV.setFilterType(filterType);
        showLV.setPersonType(personType);
        showLV.setAutoJudgePullRefreshAble(false);
        showLV.requestRoot(1);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        searchLayout.setOnClickListener(v -> {
            PersonSearchActivity.setPersonSearchActivity(getSearchType(), "FormPersonChooseActivity");
            Intent intent = new Intent(FormPersonChooseActivity.this, PersonSearchActivity.class);
            intent.putExtra(PersonSearchActivity.REQUESTNAME, getResources().getString(R.string.flow_titleadd));
            startActivity(intent);
        });
        backTV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showLV.isCanGoBack()) {
                    showLV.goBack();
                }
            }
        });
        showLV.setOnLoadListener(new OnLoadListener() {
            @Override
            public void Loading(AddressBookListItem bookListItem) {
                showLoadingBar();
                backTV.setText(getResources().getString(R.string.flow_loading));
            }

            @Override
            public void Loaded(AddressBookListItem bookListItem) {
                hideLoadingBar();
                if (bookListItem != null) {
                    final String name = bookListItem.getItemName();
                    if (name == null) {
                        backTV.setText(getResources().getString(R.string.flow_btnback));
                    } else if ("-1".equals(name)) {
                        backTV.setText(getResources().getString(R.string.flow_root));
                    } else {
                        backTV.setText(name);
                    }
                }
            }
        });

        showLV.setOnListItemClickListener(new OnListItemClickListener() {

            @Override
            public boolean onItemClick(FEPullToRefreshListView parent, View view, int position, long id) {
                final AddressBookListItem item = showLVAdapter.getItem(position);
                final AddressBookItem addressBookItem = item.getAddressBookItem();
                final int itemType = addressBookItem.getType();
                int nodeNums = 0;
                try {
                    nodeNums = Integer.parseInt(addressBookItem.getNodeNums());
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                if (nodeNums == 0 && personType == AddressBookType.SourceDepartment && itemType == AddressBookType.Department) {
                    checkObject.check(item, FormPersonListAdapter.ShowListAdapterType);
                    return true;
                }
                final int type = addressBookItem.getType();
                if (type == AddressBookType.Staff || type == AddressBookType.Position) {
                    checkObject.check(item, FormPersonListAdapter.ShowListAdapterType);
                }
                return false;
            }
        });
    }

    private int getSearchType() {
        return personType;
    }

    /**
     * 显示提示加载中的view
     */
    private void showLoadingBar() {
        LoadingHint.show(this);
        backTV.setClickable(false);
        showLV.setMode(Mode.DISABLED);
    }

    /**
     * 隐藏提示加载中的view
     */
    private void hideLoadingBar() {
        if (LoadingHint.isLoading()) {
            LoadingHint.hide();
        }
        backTV.setClickable(true);
        showLV.setRefreshAble();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onBack() {
        final Intent intent = new Intent();
        final FormPersonCollection collection = new FormPersonCollection();
        final ArrayList<AddressBookItem> checkedItems = new ArrayList<>();
        for (final AddressBookListItem listItem : checkedDatas) {
            checkedItems.add(listItem.getAddressBookItem());
        }
        collection.setPersonArray(checkedItems);
        intent.putExtra("CheckedPersons", collection);
        setResult(0, intent);
    }

    public class CheckPersonObject {
        /**
         * 选中
         */
        public void check(AddressBookListItem listItem, int listviewType) {
            final int index = containsObject(checkedDatas, listItem);
            if (index != -1) {
                checkedDatas.remove(index);
            } else {
                if (!isMultiple && listviewType == FormPersonListAdapter.ShowListAdapterType) {
                    checkedDatas.clear();
                }
                checkedDatas.add(listItem);
            }
            if (showLVAdapter != null) {
                showLVAdapter.checkPerson(listItem);
            }
            if (checkLVAdapter != null) {
                checkLVAdapter.refreshAdapter(checkedDatas);
            }
        }

        /**
         * 判断数据集合中是否包含某个对象
         */
        public int containsObject(ArrayList<AddressBookListItem> listItems, AddressBookListItem item) {
            if (listItems == null || listItems.size() == 0 || item == null) {
                return -1;
            }
            for (int i = 0; i < listItems.size(); i++) {
                final AddressBookListItem listItem = listItems.get(i);
                if (isEqualsObjects(listItem, item)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 特殊条件判断两对象是否相等
         */
        public boolean isEqualsObjects(AddressBookListItem firstListItem, AddressBookListItem secondListItem) {
            if (firstListItem != null && secondListItem != null) {
                final AddressBookItem firstItem = firstListItem.getAddressBookItem();
                final AddressBookItem secondItem = secondListItem.getAddressBookItem();
                if (firstItem != null && secondItem != null) {
                    if (firstItem.getName().equals(secondItem.getName()) && firstItem.getId().equals(secondItem.getId())) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * 获取存在Activity中的选中的数据
         */
        public ArrayList<AddressBookListItem> getCheckedDatas() {
            return checkedDatas;
        }
    }
}
