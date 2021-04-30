package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2017-02-19 20:40
 */
public class SalaryRequest extends RequestContent {

    public static final String COVER_DETAIL = "typeList";   // 某月份具体的薪资列表：应发、实发、绩效、五险...

    public String pwd;          // 校验的密码
    public String cover;        // api 就是智障
    public String date;         // 查询的工资日期： 2016-01

    @Override public String getNameSpace() {
        return "SalaryRequest";
    }

    /**
     * 构建查询工资列表的请求
     */
    public static SalaryRequest buildQueryMonthListsRequest() {
        SalaryRequest request = new SalaryRequest();
        request.cover = "interval";
        return request;
    }

    /**
     * 构建查询具体月份薪资的请求
     * @param month 具体月份
     */
    public static SalaryRequest buildSalaryDetailRequest(String month) {
        SalaryRequest request = new SalaryRequest();
        request.cover = "typeList";
        request.date = month;
        return request;
    }

}
