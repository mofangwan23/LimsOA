/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-17 下午3:35:10
 */
package cn.flyrise.android.library.view.addressbooklistview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.library.view.addressbooklistview.adapter.AddressBookBaseAdapter;
import cn.flyrise.android.library.view.addressbooklistview.adapter.AddressBookListViewDefaultAdapter;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.library.view.addressbooklistview.util.AddressBookRequstUtil;
import cn.flyrise.android.library.view.addressbooklistview.util.AddressBookRequstUtil.OnHttpResponseListener;
import cn.flyrise.android.library.view.pulltorefreshlistview.FEPullToRefreshListView;
import cn.flyrise.android.protocol.entity.AddressBookResponse;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.core.common.X.AddressBookFilterType;
import cn.flyrise.feep.core.common.X.AddressBookType;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</ br> 修改时间：2012-9-17</br> 修改备注：</br>
 */
public class AddressBookListView extends FEPullToRefreshListView {

	private AddressBookRequstUtil requestUtil;
	private boolean isRequestAllData = true;
	private AddressBookListItem personItem = new AddressBookListItem();                           // 人员岗位切换是的缓存用,默认是人员
	private AddressBookListItem positionItem = new AddressBookListItem();
	private AddressBookListItem currentItem = personItem;
	/**
	 * 用于保存搜索前的数据，以便搜索后返回时，能显示原来的搜索前数据
	 */
	private AddressBookListItem nonSearchItem = new AddressBookListItem();
	/**
	 * 用于保存搜索后数据
	 */
	private AddressBookListItem searchItem = new AddressBookListItem();
	private AddressBookBaseAdapter adapter;
	private OnListItemClickListener listItemClickListener;
	private OnRefreshListener2<ListView> refreshListener;
	private int filterType = AddressBookFilterType.Register;
	private boolean isCurrentDept;
	private int dataSourceType = AddressBookType.Staff;
	private boolean isUpRefresh;
	private OnLoadListener onLoadListener;
	private String searchKey = "";
	private String searchUserId = "";
	public static boolean isSearchRequest = false;
	private boolean isAutoJudgePullRefresh;
	private String currentDeptID = "";
	private OnItemClickListener itemClickListener;
	private String lastKey = "";
	private boolean isSearchShow = false;//是否是搜索状态

	private EditText searchEt;

	public void setSearchShow(boolean isShow) {
		this.isSearchShow = isShow;
	}

	private int dataSourceTypes;

	public AddressBookListView(Context context) {
		this(context, null);
	}

	public AddressBookListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setAddressBookItemType(int dataSourceTypes) {
		this.dataSourceTypes = dataSourceTypes;
	}

	/**
	 * 初始化
	 */
	private void init() {
		requestUtil = new AddressBookRequstUtil(getContext());
		super.setOnItemClickListener(new MyItemClickListener());
	}


	/**
	 * 用默认的适配器
	 */
	public void useDefaultAdapter() {
		adapter = new AddressBookListViewDefaultAdapter(getContext());
		setAdapter(adapter);
	}

	public void setSearchEt(EditText searchEt) {
		this.searchEt = searchEt;
	}

	@Override
	public ListAdapter getAdapter() {
		if (adapter instanceof AddressBookBaseAdapter) {
			return adapter;
		}
		return super.getAdapter();
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (adapter instanceof AddressBookBaseAdapter) {
			this.adapter = (AddressBookBaseAdapter) adapter;
			super.setAdapter(this.adapter);
			setCustomListener();
		}
		else {
			this.adapter = null;
			super.setAdapter(adapter);
		}
	}

	/**
	 * 搜索
	 */
	public void search(int requestType, String searchKey) {
		nonSearchItem = currentItem;
		setCurrentItem(searchItem);
		if (this.searchKey != null && !this.searchKey.equals(searchKey)) {// 确保搜索的不是当前搜索列表
			adapter.refreshAdapter(null);
			notifyListDataChange();
		}
		request(requestType, "", 1, searchKey, "");
	}

	/**
	 * 设置监听器
	 */
	private void setCustomListener() {
		super.setOnRefreshListener(new MyRefreshListener());
		requestUtil.setOnHttpResponseListener(new MyHttpResponseListener());
	}

	/**
	 * 请求通讯录的根目录
	 */
	public void requestRoot(int requestPage) {
		this.request(AddressBookType.Group, "", requestPage);
	}

	/**
	 * 请求通讯录
	 */
	public void requestwithNoDeptID(int requestType, String id, int requestPage) {
		setCurrentDeptID("");
		this.request(requestType, id, requestPage, "", searchUserId);
	}

	/**
	 * 请求通讯录
	 */
	public void request(int parentItemType, String id, int requestPage) {
		if (isSearchShow) {
			this.requestSearch(id, requestPage, searchKey, searchUserId);
		}
		else {
			this.request(parentItemType, id, requestPage, searchKey, searchUserId);
		}
	}

	/**
	 * 通讯录
	 */
	public void request(int parentItemType, String id, int requestPage, String searchKey, String searchUserId) {
//        FELog.showCallerStackTrace();
		isSearchRequest = searchKey != null && searchKey.length() != 0;
		this.searchKey = searchKey;
		this.searchUserId = searchUserId;
		/*--parentItemType表示点击的那个选项是什么类型,dataSourceType表示当前列表是显示岗位还是显示人员--*/
		requestUtil.startRequest(parentItemType, id, dataSourceType, filterType, requestPage, isRequestAllData, searchKey, searchUserId,
				isCurrentDept, currentDeptID, currentItem);
	}

	/**
	 * workFlow搜索
	 */
	public void requestSearch(String id, int requestPage, String searchKey, String searchUserId) {
//        FELog.showCallerStackTrace();
		if (!lastKey.equals(searchKey)) {
			isUpRefresh = false;
		}
		lastKey = searchKey;
		isSearchRequest = searchKey != null && searchKey.length() != 0;
		this.searchKey = searchKey;
		this.searchUserId = searchUserId;
		int parentItemType = AddressBookType.Staff;
		if (dataSourceTypes != 1) {
			parentItemType = AddressBookType.Department;
		}
		int filterType = AddressBookFilterType.Organization;
		/*--parentItemType表示点击的那个选项是什么类型,dataSourceType表示当前列表是显示岗位还是显示人员--*/
		requestUtil.startRequest(parentItemType, id, dataSourceTypes, filterType, requestPage, isRequestAllData, searchKey, searchUserId,
				isCurrentDept, currentDeptID, currentItem);
	}

	/**
	 * 清空列表的数据
	 */
	public void clearListData() {
		if (adapter != null) {
			adapter.refreshAdapter(null);
			notifyListDataChange();
		}
	}

	/**
	 * 设置人员或岗位类型
	 */
	public void setPersonType(int personType) {
		this.dataSourceType = personType;
		AddressBookListItem thisItem = null;// 用来去掉重复类似代码的临时指针
		AddressBookListItem otherItem = null;// 用来去掉重复类似代码的临时指针
		if (personType == AddressBookType.Staff) {
			thisItem = personItem;
			otherItem = positionItem;
		}
		else if (personType == AddressBookType.Position) {
			thisItem = positionItem;
			otherItem = personItem;
		}
		if (currentItem == otherItem) {
			setCurrentItem(thisItem);
			if (thisItem.getListDatas() == null || thisItem.getListDatas().size() == 0) {
				setPostToCurrentDepartment(true);
				requestRoot(1);
				if (onLoadListener != null) {
					onLoadListener.Loading(null);
				}
			}
			else {
				adapter.refreshAdapter(thisItem.getListDatas());
				notifyListDataChange();
				if (onLoadListener != null) {
					onLoadListener.Loaded(thisItem);
				}
			}
		}
	}

	/**
	 * 设置filterType
	 */
	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}

	/**
	 * 设置定位到本部门
	 */
	public void setPostToCurrentDepartment(boolean isCurrentDepartment) {
		this.isCurrentDept = isCurrentDepartment;
	}

	public void setCurrentDeptID(String currentDeptID) {
		this.currentDeptID = currentDeptID;
	}

	/**
	 * @param isRequestAllData true: 是(包含所有人员) false: 否(不包含花名册人员) 默认是true
	 */
	public void setRequestAllData(boolean isRequestAllData) {
		this.isRequestAllData = isRequestAllData;
	}

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.itemClickListener = listener;
	}

	/**
	 * 设置Item点击事件（非系统的Item点击事件），并且有boolean类型返回值
	 */
	public void setOnListItemClickListener(OnListItemClickListener listener) {
		this.listItemClickListener = listener;
	}

	public void setonRefreshListener(OnRefreshListener2<ListView> refreshListener) {
		this.refreshListener = refreshListener;
	}

	/**
	 * 判断是否可以返回上一层
	 */
	public boolean isCanGoBack() {
		return currentItem.getParentItem() != null || isHasParent();
	}

	/**
	 * 判断当前级别是否还有上一层级别
	 */
	private boolean isHasParent() {
		if (currentItem == null) {
			return false;
		}
		final String id = currentItem.getItemID();
		return (id != null && !"-1".equals(id));
	}

	/**
	 * 返回上一层
	 */
	public void goBack() {
		if (isCanGoBack()) {
			setPostToCurrentDepartment(false);
			if (currentItem.getParentItem() == null && isHasParent()) {
				goBackRequest();
				if (onLoadListener != null) {
					onLoadListener.Loading(currentItem);
				}
			}
			else {
				setCurrentItem(currentItem.getParentItem());
				if (onLoadListener != null) {
					onLoadListener.Loaded(currentItem);
				}
			}
			refreshAdapter(currentItem);
			autoJudgePullRefresh(currentItem);
			notifyListDataChange();
		}
	}

	private void setCurrentItem(AddressBookListItem item) {
		currentItem = item;
		if (isSearchRequest) {
			return;
		}
		if (dataSourceType == AddressBookType.Staff) {
			personItem = currentItem;
		}
		else {
			positionItem = currentItem;
		}
	}

	private void goBackRequest() {
		isUpRefresh = false;
		setCurrentDeptID(currentItem.getItemID());
		final AddressBookListItem parentItem = new AddressBookListItem();
		currentItem.setParentItem(parentItem);
		setCurrentItem(parentItem);
		request(getRequstType(), "", 1, "", "");// 请求上一级
	}

	/**
	 * 获取请求类型
	 */
	private int getRequstType() {
		if (currentItem == null) {
			return AddressBookType.Group;
		}
		final AddressBookItem item = currentItem.getAddressBookItem();
		return item == null ? AddressBookType.Group : item.getType();
	}

	/**
	 * item点击监听器
	 */
	private class MyItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(FEPullToRefreshListView parent, View view, int position, long id) {
			boolean isBreak = false;
			if (listItemClickListener != null) {
				isBreak = listItemClickListener.onItemClick(parent, view, position, id);
			}
			if (isBreak) {
				return;
			}
			if (itemClickListener != null) {
				itemClickListener.onItemClick(parent, view, position, id);
			}
			if (adapter != null && adapter instanceof AddressBookBaseAdapter) {
				final AddressBookListItem bookListItem = adapter.getItem(position);
				final AddressBookItem bookItem = bookListItem.getAddressBookItem();
				final int itemType = bookItem.getType();
				if (!isPersonOrPosition(itemType)) {
					setPostToCurrentDepartment(false);
					refreshAdapter(null);
					notifyListDataChange();
					final AddressBookListItem parentItem = currentItem;
					final ArrayList<AddressBookListItem> listItems = bookListItem.getListDatas();
					setCurrentItem(bookListItem);
					if (listItems != null) {
						refreshAdapter(currentItem);
						notifyListDataChange();
						if (onLoadListener != null) {
							onLoadListener.Loaded(currentItem);
						}
					}
					else {
						isUpRefresh = false;
						currentItem.setParentItem(parentItem);
						requestwithNoDeptID(bookItem.getType(), bookItem.getId(), 1);
						if (onLoadListener != null) {
							onLoadListener.Loading(currentItem);
						}
					}
					autoJudgePullRefresh(currentItem);
				}
			}
		}
	}

	/**
	 * 判断是否非人员和岗位
	 */
	private boolean isPersonOrPosition(int itemType) {
		return itemType == AddressBookType.Staff || itemType == AddressBookType.Position;
	}

	/**
	 * 上下拉加载监听器
	 */
	private class MyRefreshListener implements OnRefreshListener2<ListView> {

		@Override
		public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
			isUpRefresh = false;
			pullToRequest(1);
			if (refreshListener != null) {
				refreshListener.onPullDownToRefresh(refreshView);
			}
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
			isUpRefresh = true;
			final int page = currentItem.getDataPage() + 1;
			pullToRequest(page);
			if (refreshListener != null) {
				refreshListener.onPullUpToRefresh(refreshView);
			}
		}
	}

	public void onPullDownToRefresh() {
		isUpRefresh = false;
		pullToRequest(1);
	}

	/**
	 * 上下拉请求数据
	 */
	private void pullToRequest(int page) {
		final AddressBookItem item = currentItem.getAddressBookItem();
		if (item == null) {
			request(getRequstType(), currentItem.getItemID(), page);
		}
		else {
			request(getRequstType(), item.getId(), page);
		}
	}

	/**
	 * 获取服务器返回结果监听器
	 */
	private class MyHttpResponseListener implements OnHttpResponseListener {

		@Override
		public void onSuccess(AddressBookResponse listResponse, int requstPage, AddressBookListItem listItem) {
			if (isSearchShow && TextUtils.isEmpty(searchEt.getText().toString().trim())) {
				return;
			}
			final ArrayList<AddressBookListItem> bookListItems = createListData(listResponse);
			if (isUpRefresh) {
				final ArrayList<AddressBookListItem> savedListItems = listItem.getListDatas();
				if (savedListItems != null) {
					savedListItems.addAll(bookListItems);
				}
			}
			else {
				listItem.setListDatas(bookListItems);
			}
			final String currentDeptID = listResponse.getCurrentDeptID();
			if (currentDeptID != null) {
				listItem.setItemID(currentDeptID);
				listItem.setItemName(listResponse.getCurrentDeptName());
			}
			else {
				final AddressBookItem item = listItem.getAddressBookItem();
				if (item != null) {
					listItem.setItemName(item.getName());
				}
			}
			listItem.setDataPage(requstPage);
			int totalNums = 0;
			try {
				totalNums = Integer.valueOf(listResponse.getTotalNums());
			} catch (final Exception e) {
				e.printStackTrace();
			}
			listItem.setTotalNums(totalNums);
			if (currentItem.equals(listItem)) {
				refreshAdapter(listItem);
				notifyListDataChange();
			}
			onRefreshComplete();
			autoJudgePullRefresh(listItem);
			if (onLoadListener != null) {
				onLoadListener.Loaded(listItem);
			}
		}

		@Override
		public void onFailure(Throwable error, String content) {
			if (onLoadListener != null) {
				onLoadListener.Loaded(currentItem);
			}
			onRefreshComplete();
			autoJudgePullRefresh(currentItem);
			refreshAdapter(null);
			notifyListDataChange();
		}
	}

	/**
	 * 刷新适配器
	 */
	private void refreshAdapter(AddressBookListItem listItem) {
		if (adapter != null && adapter instanceof AddressBookBaseAdapter) {
			adapter.refreshAdapter((listItem == null) ? null : listItem.getListDatas());
		}
	}

	/**
	 * 设置列表可否上下拉加载
	 */
	private void setRefreshAble(AddressBookListItem bookListItem) {
		if (bookListItem != null) {
			final ArrayList<AddressBookListItem> listDatas = bookListItem.getListDatas();
			if (listDatas != null) {
				if (listDatas.size() == 0) {
					if (!isSearchRequest) {
						setMode(Mode.PULL_FROM_START);
					}
				}
				else if (listDatas.size() < bookListItem.getTotalNums()) {
					setMode(Mode.PULL_FROM_END);
				}
				else if (listDatas.size() == bookListItem.getTotalNums()) {
					setMode(PullToRefreshBase.Mode.DISABLED);
				}
				else {
					setMode(Mode.DISABLED);
				}
			}
			else {
				if (!isSearchRequest) {
					setMode(Mode.PULL_FROM_START);
				}
			}
		}
	}

	/**
	 * 设置列表可否上下拉加载
	 */
	public void setRefreshAble() {
		this.setRefreshAble(getCurrentItem());
	}

	/**
	 * 设置是否自动判断是否可以上下拉刷新
	 */
	public void setAutoJudgePullRefreshAble(boolean isAutoJudge) {
		isAutoJudgePullRefresh = isAutoJudge;
	}

	/**
	 * 判断上下拉刷新时，调用此方法
	 */
	private void autoJudgePullRefresh(AddressBookListItem listItem) {
		if (isAutoJudgePullRefresh || isSearchRequest) {
			setRefreshAble(listItem);
		}
	}

	public AddressBookListItem getCurrentItem() {
		return currentItem;
	}

	/**
	 * 把AddressBookResponse类型封装成ArrayList<AddressBookListItem>
	 */
	private ArrayList<AddressBookListItem> createListData(AddressBookResponse listResponse) {
		final ArrayList<AddressBookListItem> listDatas = new ArrayList<>();
		List<AddressBookItem> listItems = null;
		try {
			listItems = listResponse.getItems();
			if (listItems != null) {
				for (final AddressBookItem item : listItems) {
					final AddressBookListItem listData = new AddressBookListItem();
					listData.setAddressBookItem(item);
					listDatas.add(listData);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return listDatas;
	}

	/**
	 * 设置加载监听器
	 */
	public void setOnLoadListener(OnLoadListener loadListener) {
		this.onLoadListener = loadListener;
	}

	public interface OnLoadListener {

		/**
		 * 正在加载</br> 调用时机：当点击列表时，需要加载时调用
		 */
		void Loading(AddressBookListItem bookListItem);

		/**
		 * 加载完成</br> 调用时机：当点击列表时，不需要加载时或者加载完成时调用
		 */
		void Loaded(AddressBookListItem bookListItem);
	}

	/**
	 * 列表item点击事件
	 */
	public interface OnListItemClickListener {

		/**
		 * @return false可以继续系统的点击事件，true不可以
		 */
		boolean onItemClick(FEPullToRefreshListView parent, View view, int position, long id);
	}

	public interface OnListDataChangeListener {

		void onListDataChange();
	}

	private OnListDataChangeListener mListDataChangeListener;

	public void setOnListDataChangeListener(OnListDataChangeListener listDataChangeListener) {
		this.mListDataChangeListener = listDataChangeListener;
	}

	private void notifyListDataChange() {
		if (mListDataChangeListener != null) {
			mListDataChangeListener.onListDataChange();
		}
	}
}
