/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����11:48:20
 */
package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class AssociationListResponse extends ResponseContent {

    private Result result;

    public Result getResult() {
        return result;
    }

    public class Result {

        /**
         * CurrentPage : 1
         * PageSize : 25
         * TotalRow : 15
         * TotalPage : 1
         * FieldSet : [{"ID":"6158","TOPIC":"测试参会","DEAL_FLAG":"已读","ATTENDED_FLAG":"未处理","PUBLISH_DATE":"2016-04-25 13:40","START_DATE":"2016-04-30 14:00","END_DATE":"2016-04-30 17:00"},{"ID":"6138","TOPIC":"测试","DEAL_FLAG":"已读","ATTENDED_FLAG":"未处理","PUBLISH_DATE":"2016-04-25 10:57","START_DATE":"2016-04-28 09:00","END_DATE":"2016-04-28 11:59"}]
         */

        private int CurrentPage;
        private int PageSize;
        private int totalRow;
        private int TotalPage;
        @SerializedName("FieldSet")
        private List<Matter> associationList;


        public int getCurrentPage() {
            return CurrentPage;
        }

        public void setCurrentPage(int currentPage) {
            CurrentPage = currentPage;
        }

        public int getPageSize() {
            return PageSize;
        }

        public void setPageSize(int pageSize) {
            PageSize = pageSize;
        }

        public int getTotalRow() {
            return totalRow;
        }

        public void setTotalRow(int totalRow) {
            this.totalRow = totalRow;
        }

        public int getTotalPage() {
            return TotalPage;
        }

        public void setTotalPage(int totalPage) {
            this.TotalPage = totalPage;
        }

        public List<Matter> getAssociationList() {
            return associationList;
        }

        public void setAssociationList(List<Matter> associationList) {
            this.associationList = associationList;
        }
    }

}
