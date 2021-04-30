package cn.flyrise.feep.meeting.old;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.android.protocol.model.ListDataItem;
import cn.flyrise.feep.collaboration.model.FileInfo;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

	/**
	 * 将请求回来的会议列表数据转化为显示的listView数据
	 */
	public static ArrayList<MeetingListItemBean> getTable(ListResponse response) {
		ArrayList<MeetingListItemBean> listItems = null;
		List<List<ListDataItem>> tableRowsList;
		if (response == null || response.getTable() == null) {
			return null;
		}
		tableRowsList = response.getTable().getTableRows();
		if (tableRowsList != null) {
			listItems = new ArrayList<>();
			ArrayList<ListDataItem> tableRows;
			final int length = tableRowsList.size();
			for (int i = 0; i < length; i++) {
				tableRows = (ArrayList<ListDataItem>) tableRowsList.get(i);
				MeetingListItemBean itemBean = new MeetingListItemBean();
				for (ListDataItem dataItem : tableRows) {
					switch (dataItem.getName()) {
						case "id":
							itemBean.setId(dataItem.getValue());
							break;
						case "isNews":
							itemBean.setNews(TextUtils.equals(dataItem.getValue(), "true"));
							break;
						case "title":
							itemBean.setTitle(dataItem.getValue());
							break;
						case "sendUser":
							itemBean.setSendUser(dataItem.getValue());
							break;
						case "status":
							itemBean.setStatus(dataItem.getValue());
							break;
						case "sendTime":
							itemBean.setTime(dataItem.getValue());
							break;
						case "START_DATE":
							itemBean.setStartTime(dataItem.getValue());
							break;
						case "END_DATE":
							itemBean.setEndTime(dataItem.getValue());
							break;
					}
				}
				listItems.add(itemBean);
			}
		}
		return listItems;
	}

	public static ArrayList<String> changeToPath(ArrayList<FileInfo> fileInfoList) {
		final ArrayList<String> pathList = new ArrayList<>();
		for (int i = 0; i < fileInfoList.size(); i++) {
			pathList.add(fileInfoList.get(i).getPath());
		}
		return pathList;
	}
}
