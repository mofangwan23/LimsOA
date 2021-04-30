/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-16 上午10:34:02
 */
package cn.flyrise.feep.commonality.util;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.ListRequest;
import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.android.protocol.model.ListDataItem;
import cn.flyrise.android.protocol.model.ListTable;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</                                                               br> 修改时间：2013-3-16</br> 修改备注：</br>
 */
public class ListDataProvider {

	/**
	 * 每页加载的数据的数量
	 */
	private int perPageNums = 10;
	private OnListDataResponseListener listener;
	private Context context;

	public ListDataProvider(Context context) {
		this.context = context;
	}

	public void setPerPageNums(int perPageNums) {
		this.perPageNums = perPageNums;
	}

	/**
	 * 请求数据
	 */
	public void request(int requestType, int page, String searchKey) {
		final ListRequest request = new ListRequest();
		request.setPage(page + "");
		request.setPerPageNums(perPageNums + "");
		request.setRequestType(requestType);
		request.setSearchKey(searchKey);
		final boolean isSearch = !TextUtils.isEmpty(searchKey);
		FEHttpClient.getInstance().post(request, createResponseHandler(isSearch));
	}

	/**
	 * 请求数据(位置上报历史记录)
	 * @param requestType 请求类型
	 * @param id 月份(格式：2013-10)
	 * @param page 第几页
	 */
	public void request(int requestType, String id, int page, String userId) {
		request(requestType, id, page, userId, perPageNums);
	}

	public void request(int requestType, String id, int page, String userId, int perPageNums, int sumId) {
		final ListRequest request = new ListRequest();
		request.setPage(page + "");
		request.setPerPageNums(perPageNums + "");
		request.setRequestType(requestType);
		request.setId(id);
		request.setUserId(userId);
		request.setSumId(sumId);
		final boolean isSearch = false;
		FEHttpClient.getInstance().post(request, createResponseHandler(isSearch));
	}

	public void request(int requestType, String id, int page, String userId, int perPageNums) {
		request(requestType, id, page, userId, perPageNums, 0);
	}

	/**
	 * 创建服务器响应handler
	 */
	private ResponseCallback<ListResponse> createResponseHandler(final boolean isSearch) {
		return new ResponseCallback<ListResponse>(context) {
			@Override
			public void onCompleted(ListResponse listResponse) {
				try {
					final ListResponse rspContent = listResponse;
					int totalNums = 0;
					try {
						totalNums = Integer.parseInt(rspContent.getTotalNums());
					} catch (final Exception e) {
						e.printStackTrace();
					}
					int requestType = CommonUtil.parseInt(rspContent.getRequestType());
					final ListTable table = rspContent.getTable();
					final List<List<ListDataItem>> tableRows = table.getTableRows();
					final List<FEListItem> listItems = changeResposeData(tableRows);
					if (listener != null) {
						listener.onSuccess(listItems, totalNums, requestType, isSearch);
					}
				} catch (final Exception e) {
					if (listener != null) {
						listener.onFailure(e, "异常出错", isSearch);
					}
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				if (listener != null) {
					listener.onFailure(repositoryException.exception(), repositoryException.errorMessage(), isSearch);
				}
			}
		};
	}

	/**
	 * 根据不同的列表转换数据
	 */
	private List<FEListItem> changeResposeData(List<List<ListDataItem>> listItems) {
		final List<FEListItem> feListItems = new ArrayList<>();
		for (final List<ListDataItem> listItem : listItems) {
			final FEListItem item = new FEListItem();
			for (final ListDataItem listDataItem : listItem) {
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
					item.setGuid(listDataItem.getValue());
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
				else if ("sendUserImg".equals(listDataItem.getName())) {
					item.setSendUserImg(listDataItem.getValue());
				}
			}
			feListItems.add(item);
		}
		return feListItems;
	}

	public interface OnListDataResponseListener {

		/**
		 * 数据加载成功调用此方法
		 * @param requestType 当前列表类型
		 * 服务器返回的数据
		 */
		void onSuccess(List<FEListItem> listItems, int totalNums, int requestType, boolean isSearch);

		/**
		 * 数据加载失败调用此方法
		 */
		void onFailure(Throwable error, String content, boolean isSearch);
	}

	/**
	 * 设置监听加载事件
	 */
	public void setOnListResposeListener(OnListDataResponseListener listener) {
		this.listener = listener;
	}
}
