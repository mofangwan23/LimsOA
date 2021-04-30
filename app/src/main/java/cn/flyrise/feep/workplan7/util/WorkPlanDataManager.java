package cn.flyrise.feep.workplan7.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.android.protocol.model.ListDataItem;
import cn.flyrise.android.protocol.model.ListTable;
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-26 下午4:36:47 <br/>
 *          类说明 :
 */
public class WorkPlanDataManager {
    public static ArrayList<WorkPlanListItemBean> changeDataToListItemBean(ListResponse listResponse) {
        ArrayList<WorkPlanListItemBean> list = new ArrayList<>();
        if (listResponse != null && listResponse.getTable() != null) {
            ListTable listtable = listResponse.getTable();
            List<List<ListDataItem>> tableRows = listtable.getTableRows();
            int lenght = tableRows.size();
            for (int i = 0; i < lenght; i++) {
                List<ListDataItem> dataItems = tableRows.get(i);
                WorkPlanListItemBean itemBean = new WorkPlanListItemBean();
                for (ListDataItem dataItem : dataItems) {
                    String value = dataItem.getValue();
                    switch (dataItem.getName()) {
                        case "id":
                            itemBean.setId(value);
                            break;
                        case "isNews":
                            itemBean.setNews(TextUtils.equals(value, "true"));
                            break;
                        case "title":
                            itemBean.setTitle(value);
                            break;
                        case "sendUser":
                            itemBean.setSendUser(value);
                            break;
                        case "sendTime":
                            itemBean.setSendTime(value);
                            break;
                        case "sectionName":
                            itemBean.setSectionName(value);
                            break;
                        case "status":
                            itemBean.setStatus(value);
                            break;
                        case "UserId":
                            itemBean.setSendUserId(value);
                            break;
                    }
                }
                list.add(itemBean);
            }
        }
        return list;
    }
}
