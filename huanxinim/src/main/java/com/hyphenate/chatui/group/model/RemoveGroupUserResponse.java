package com.hyphenate.chatui.group.model;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by klc on 2017/3/23.
 */

public class RemoveGroupUserResponse extends ResponseContent {

    public Result result;

    public class Result {
        public String exception;
        public String error_description;
    }


}
