package cn.flyrise.android.protocol.entity;

import java.util.ArrayList;

import cn.flyrise.android.protocol.model.ReportListItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-19 上午11:41:04 <br/>
 *          类说明 :
 */
public class ReportListResponse extends ResponseContent {
    private String totalNums;
    private ArrayList<ReportListItem> reportList;

    public int getTotalNums () {
        try {
            return Integer.parseInt (totalNums);
        } catch (final Exception e) {
            e.printStackTrace ();
        }
        return 0;
    }

    public void setTotalNums (String totalNums) {
        this.totalNums = totalNums;
    }

    public ArrayList<ReportListItem> getReportList () {
        return reportList;
    }

    public void setReportList (ArrayList<ReportListItem> reportList) {
        this.reportList = reportList;
    }

}
