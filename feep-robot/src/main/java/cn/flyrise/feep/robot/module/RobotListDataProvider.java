package cn.flyrise.feep.robot.module;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.robot.bean.RobotFEListItem;
import cn.flyrise.feep.robot.bean.RobotListDataItem;
import cn.flyrise.feep.robot.bean.RobotListRequest;
import cn.flyrise.feep.robot.bean.RobotListResponse;
import cn.flyrise.feep.robot.bean.RobotListTable;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-11:07.
 */
public class RobotListDataProvider {

	private final int perPageNums = 10;
	private OnListDataResponseListener listener;
	private Context context;

	public RobotListDataProvider(Context context) {
		this.context = context;
	}

	/**
	 * 请求数据
	 */
	public void request(int requestType, int page, String searchKey) {
		final RobotListRequest requests = new RobotListRequest();
		requests.page = page + "";
		requests.perPageNums = perPageNums + "";
		requests.requestType = requestType + "";
		requests.searchKey = searchKey;
		final boolean isSearch = !TextUtils.isEmpty(searchKey);
		FEHttpClient.getInstance().post(requests, createResponseHandler(isSearch));
	}

	private ResponseCallback<RobotListResponse> createResponseHandler(final boolean isSearch) {
		return new ResponseCallback<RobotListResponse>(context) {
			@Override
			public void onCompleted(RobotListResponse listResponse) {
				try {
					final RobotListResponse rspContent = listResponse;
					int totalNums = 0;
					try {
						totalNums = Integer.parseInt(rspContent.totalNums);
					} catch (final Exception e) {
						e.printStackTrace();
					}
					int requestType = CommonUtil.parseInt(rspContent.requestType);
					final RobotListTable table = rspContent.table;
					final List<List<RobotListDataItem>> tableRows = table.tableRows;
					final List<RobotFEListItem> listItems = changeResposeData(tableRows);
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
	private List<RobotFEListItem> changeResposeData(List<List<RobotListDataItem>> listItems) {
		final List<RobotFEListItem> feListItems = new ArrayList<>();
		for (final List<RobotListDataItem> listItem : listItems) {
			final RobotFEListItem item = new RobotFEListItem();
			for (final RobotListDataItem listDataItem : listItem) {
				if ("id".equals(listDataItem.name)) {
					item.setId(listDataItem.value);
				}
				else if ("title".equals(listDataItem.name)) {
					item.setTitle(listDataItem.value);
				}
				else if ("sendTime".equals(listDataItem.name)) {
					item.setSendTime(listDataItem.value);
				}
				else if ("sendUser".equals(listDataItem.name)) {
					item.setSendUser(listDataItem.value);
				}
				else if ("msgId".equals(listDataItem.name)) {
					item.setMsgId(listDataItem.value);
				}
				else if ("msgType".equals(listDataItem.name)) {
					item.setMsgType(listDataItem.value);
				}
				else if ("requestType".equals(listDataItem.name)) {
					item.setRequestType(listDataItem.value);
				}
				else if ("date".equals(listDataItem.name)) { // 以下为位置上报历史记录新增的2013-10-22
					item.setDate(listDataItem.value);
				}
				else if ("whatDay".equals(listDataItem.name)) {
					item.setWhatDay(listDataItem.value);
				}
				else if ("time".equals(listDataItem.name)) {
					item.setTime(listDataItem.value);
				}
				else if ("address".equals(listDataItem.name)) {
					item.setAddress(listDataItem.value);
				}
				else if ("name".equals(listDataItem.name)) {
					item.setName(listDataItem.value);
				}
				else if ("guid".equals(listDataItem.name)) {// 现场签到的图片2015-02-10,by luozhanjian
					item.setImageHerf(listDataItem.value);
					item.setGuid(listDataItem.value);
				}
				else if ("pdesc".equals(listDataItem.name)) {// 图片描述
					item.setPdesc(listDataItem.value);
				}
				else if ("sguid".equals(listDataItem.name)) {// 图片的缩略图
					item.setSguid(listDataItem.value);
				}
				else if ("content".equals(listDataItem.name)) {
					item.setContent(listDataItem.value);
				}
				else if ("badge".equals(listDataItem.name)) {
					item.setBadge(listDataItem.value);
				}
				else if ("category".equals(listDataItem.name)) {
					item.setCategory(listDataItem.value);
				}
				else if ("sendUserImg".equals(listDataItem.name)) {
					item.setSendUserImg(listDataItem.value);
				}
			}
			feListItems.add(item);
		}
		return feListItems;
	}

	public interface OnListDataResponseListener {

		void onSuccess(List<RobotFEListItem> listItems, int totalNums, int requestType, boolean isSearch);

		void onFailure(Throwable error, String content, boolean isSearch);
	}

	public void setOnListResposeListener(OnListDataResponseListener listener) {
		this.listener = listener;
	}

}
