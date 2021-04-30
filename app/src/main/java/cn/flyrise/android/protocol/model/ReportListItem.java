package cn.flyrise.android.protocol.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-19 上午11:31:56 <br/>
 *          类说明 :
 */
public class ReportListItem implements Serializable {
    private static final long serialVersionUID = 803745423726467279L;
    private String reportID;
    private String reportName;
    private String searchPageUrl;
    private ArrayList<ReportDetailsItem> reportDetailsItemList;

    public String getReportID () {
        return reportID;
    }

    public void setReportID (String reportID) {
        this.reportID = reportID;
    }

    public String getReportName () {
        return reportName;
    }

    public void setReportName (String reportName) {
        this.reportName = reportName;
    }

    public String getSearchPageUrl () {
        return searchPageUrl;
    }

    public void setSearchPageUrl (String searchPageUrl) {
        this.searchPageUrl = searchPageUrl;
    }

    public ArrayList<ReportDetailsItem> getReportDetailsItemList () {
        return reportDetailsItemList;
    }

    public void setReportDetailsItemList (ArrayList<ReportDetailsItem> reportDetailsItemList) {
        this.reportDetailsItemList = reportDetailsItemList;
    }

    public static long getSerialversionuid () {
        return serialVersionUID;
    }

}
