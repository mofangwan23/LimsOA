package cn.flyrise.android.protocol.entity.knowledge;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by KLC on 2016/12/12.
 */

public class FolderTypeResponse  extends ResponseContent {

    public Result result;
    public class Result{
        /**
         * isPic : false
         * isDoc : false
         */

        public boolean isPic;
        public boolean isDoc;
    }
}
