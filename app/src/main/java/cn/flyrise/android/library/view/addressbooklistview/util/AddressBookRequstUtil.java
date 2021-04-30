/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-17 下午3:00:03
 */

package cn.flyrise.android.library.view.addressbooklistview.util;

import android.content.Context;

import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.entity.AddressBookRequest;
import cn.flyrise.android.protocol.entity.AddressBookResponse;
import cn.flyrise.feep.core.common.X.AddressBookFilterType;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</ br> 修改时间：2012-9-17</br> 修改备注：</br>
 */
public class AddressBookRequstUtil {

	public final int perPageNumbers = 10; // 每次加载的数据的数量
	private Context context;
	private OnHttpResponseListener listener;

	private AddressBookListItem listItem;

	public AddressBookRequstUtil(Context context) {
		this.context = context;
	}

	/**
	 * 开始请求服务器
	 */
	public void startRequest(int requestType, String id, int listType, int filterType,
			int requestPage, boolean isRequestAllData, String searchKey, String searchUserId, boolean isCurrentDept, String currentDeptID,
			AddressBookListItem listItem) {
		this.listItem = listItem;
		if (listItem != null) {
			this.startRequest(requestType, listType, filterType, requestPage, id, isRequestAllData, searchKey, searchUserId, isCurrentDept,
					currentDeptID);
		}
	}

	/**
	 * 开始请求服务器
	 */
	public void startRequest(int requestType, int listType, int filterType, int requestPage, String id,
			boolean isRequestAllData, String searchKey, String searchUserId, boolean isCurrentDept, String currentDeptID) {
		final String page = requestPage + "";
		final String perPageNums = String.valueOf(perPageNumbers);
		filterType = filterType == -1 ? AddressBookFilterType.Register : filterType;
		final AddressBookRequest request = new AddressBookRequest();
		request.setCurrentDeptID(currentDeptID);
		request.setDataSourceType(listType);
		request.setFilterType(filterType);
		request.setIsCurrentDept(isCurrentDept);
		request.setPage(page);
		request.setPerPageNums(perPageNums);
		request.setParentItemID(id);
		request.setParentItemType(requestType);
		request.setSearchKey(searchKey);
		request.setSearchUserID(searchUserId);

		startRequest(request);
	}

	/**
	 * 开始请求服务器
	 */
	public void startRequest(AddressBookRequest request) {
		int requestPage;
		try {
			requestPage = Integer.parseInt(request.getPage());
		} catch (final Exception e) {
			requestPage = 1;
		}

		final int sPage = requestPage;
		FEHttpClient.getInstance().post(request, new ResponseCallback<AddressBookResponse>(context) {
			@Override public void onCompleted(AddressBookResponse responseContent) {
				if (listener != null) {
					if (listener != null) {
						listener.onSuccess(responseContent, sPage, listItem);
					}
				}
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				if (listener != null) {
					listener.onFailure(repositoryException.exception(), repositoryException.errorMessage());
				}
			}
		});
	}

	public interface OnHttpResponseListener {

		/**
		 * 加载通讯录成功后调用此方法获取数据
		 */
		void onSuccess(AddressBookResponse listResponse, int requstPage, AddressBookListItem listItem);

		/**
		 * 数据加载失败调用此方法
		 * @param content 错误信息
		 **/
		void onFailure(Throwable error, String content);
	}

	/**
	 * 设置监听加载事件
	 */
	public void setOnHttpResponseListener(OnHttpResponseListener listener) {
		this.listener = listener;
	}
}
