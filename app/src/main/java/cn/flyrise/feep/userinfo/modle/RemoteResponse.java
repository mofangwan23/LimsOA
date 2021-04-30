package cn.flyrise.feep.userinfo.modle;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by Administrator on 2017-5-8.
 */

public class RemoteResponse extends ResponseContent {

    public Result result;

    public class Result{
        public String userImage;
    }
}
