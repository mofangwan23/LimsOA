package cn.flyrise.feep.news;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.entity.ListRequest;
import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.android.protocol.model.ListDataItem;
import cn.flyrise.android.protocol.model.ListTable;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * @author ZYP
 * @since 2016-11-09 16:01
 * 1. 数据来源有了。
 * 2. 刷新数据
 */
public class NewsBulletinPresenter implements NewsBulletinContract.IPresenter {

	private static final int PAGE_SIZE = 20;
	private int mRequestType;
	private NewsBulletinContract.IView mNewsBulletinView;

	private int mTotalCount;
	private int mCurrentPage = 1;

	public NewsBulletinPresenter(NewsBulletinContract.IView newsBulletinView, int requestType) {
		this.mNewsBulletinView = newsBulletinView;
		this.mRequestType = requestType;
	}

	@Override public void start() {
		mNewsBulletinView.showLoading();
		loadData(mCurrentPage = 1);
	}

	@Override public void request(int pageNumber) {
		loadData(mCurrentPage = pageNumber);
	}

	@Override public void refresh() {
		loadData(mCurrentPage = 1);
	}

	@Override public int getCurrentPage() {
		return this.mCurrentPage;
	}

	@Override public void setCurrentPage(int currentPage) {
		this.mCurrentPage = currentPage;
	}

	@Override public boolean hasMoreData() {
		return mCurrentPage * PAGE_SIZE < mTotalCount;
	}

	@Override public void onDestroy() {
		FEHttpClient.cancel(this);
	}

	private void loadData(int page) {
		ListRequest request = new ListRequest();
		request.setPage(page + "");
		request.setPerPageNums(PAGE_SIZE + "");
		request.setRequestType(mRequestType);
		request.setSearchKey("");

		FEHttpClient.getInstance().post(request, new ResponseCallback<ListResponse>(this) {
			@Override public void onCompleted(ListResponse listResponse) {
				try {
					mTotalCount = Integer.parseInt(listResponse.getTotalNums());
				} catch (Exception exception) {
					exception.printStackTrace();
				}

				List<FEListItem> feListItems = convertToFEListItem(listResponse.getTable());
				boolean hasMoreData = mTotalCount > mCurrentPage * PAGE_SIZE;
				mNewsBulletinView.fetchDataSuccess(feListItems, hasMoreData);
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				mCurrentPage = mCurrentPage > 1 ? mCurrentPage-- : 1;
				mNewsBulletinView.fetchDataError(repositoryException);
			}
		});
	}

	private List<FEListItem> convertToFEListItem(ListTable listTable) {
		if (listTable == null) {
			return new ArrayList<>();
		}

		List<List<ListDataItem>> tableRows = listTable.getTableRows();
		if (CommonUtil.isEmptyList(tableRows)) {
			return new ArrayList<>();
		}

		List<FEListItem> feListItems = new ArrayList<>();
		for (List<ListDataItem> listItem : tableRows) {
			FEListItem item = new FEListItem();
			for (ListDataItem listDataItem : listItem) {
				if ("id".equals(listDataItem.getName())) {
					item.setId(listDataItem.getValue());
				}
				else if ("title".equals(listDataItem.getName())) {
					item.setTitle(listDataItem.getValue());
				}
				else if ("sendTime".equals(listDataItem.getName())) {
					item.setSendTime(listDataItem.getValue());
				}
				else if ("sendUser".equals(listDataItem.getName())) {
					item.setSendUser(listDataItem.getValue());
				}
				else if ("msgId".equals(listDataItem.getName())) {
					item.setMsgId(listDataItem.getValue());
				}
				else if ("msgType".equals(listDataItem.getName())) {
					item.setMsgType(listDataItem.getValue());
				}
				else if ("requestType".equals(listDataItem.getName())) {
					item.setRequestType(listDataItem.getValue());
				}
				else if ("date".equals(listDataItem.getName())) { // 以下为位置上报历史记录新增的2013-10-22
					item.setDate(listDataItem.getValue());
				}
				else if ("whatDay".equals(listDataItem.getName())) {
					item.setWhatDay(listDataItem.getValue());
				}
				else if ("time".equals(listDataItem.getName())) {
					item.setTime(listDataItem.getValue());
				}
				else if ("address".equals(listDataItem.getName())) {
					item.setAddress(listDataItem.getValue());
				}
				else if ("name".equals(listDataItem.getName())) {
					item.setName(listDataItem.getValue());
				}
				else if ("guid".equals(listDataItem.getName())) {// 现场签到的图片2015-02-10,by luozhanjian
					item.setImageHerf(listDataItem.getValue());
				}
				else if ("pdesc".equals(listDataItem.getName())) {// 图片描述
					item.setPdesc(listDataItem.getValue());
				}
				else if ("sguid".equals(listDataItem.getName())) {// 图片的缩略图
					item.setSguid(listDataItem.getValue());
				}
				else if ("content".equals(listDataItem.getName())) {
					item.setContent(listDataItem.getValue());
				}
				else if ("badge".equals(listDataItem.getName())) {
					item.setBadge(listDataItem.getValue());
				}
				else if ("category".equals(listDataItem.getName())) {
					item.setCategory(listDataItem.getValue());
				}
				else if ("isNews".equals(listDataItem.getName())) {
					item.setNews(TextUtils.equals(listDataItem.getValue(), "true"));
				}
			}
			feListItems.add(item);
		}
		return feListItems;
	}
}
