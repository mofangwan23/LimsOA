package cn.flyrise.android.protocol.entity.knowledge;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by k on 2016/9/7.
 */
public class FilePermissionResponse extends ResponseContent {

    private FilePermission result;

    public FilePermission getResult() {
        return result;
    }

    public class FilePermission {
        public boolean del;
        public boolean publish;
    }

}
