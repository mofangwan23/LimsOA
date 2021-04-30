package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-19 上午10:11:51 <br/>
 *          类说明 :
 */
public class ReportListRequest extends RequestContent {
    public static final String NAMESPACE = "ReportListRequest";
    private int page;
    private int perPageNums;
    private String searchKey;

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public int getPage () {
        return page;
    }

    public void setPage (int page) {
        this.page = page;
    }

    public int getPerPageNums () {
        return perPageNums;
    }

    public void setPerPageNums (int perPageNums) {
        this.perPageNums = perPageNums;
    }

    public String getSearchKey () {
        return searchKey;
    }

    public void setSearchKey (String searchKey) {
        this.searchKey = searchKey;
    }

    public static String getNamespace () {
        return NAMESPACE;
    }

}
