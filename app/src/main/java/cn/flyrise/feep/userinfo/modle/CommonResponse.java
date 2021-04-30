package cn.flyrise.feep.userinfo.modle;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by Administrator on 2017-5-8.
 */

public class CommonResponse extends ResponseContent {

    public result result;

    public class result {
        public String userImage;
    }
}
