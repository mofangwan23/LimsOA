package cn.flyrise.android.protocol.entity.knowledge;


import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by k on 2016/9/7.
 */
public class FolderTreeResponse extends ResponseContent {

    public List<FolderTree> result;

    public List<FolderTree> getResult() {
        return result;
    }

    public class FolderTree {

        @SerializedName("FNAME")
        public String name;

        @SerializedName("ID")
        public String id;

        @SerializedName("firstfolder")
        public boolean canManage;

        public List<FolderTree> list;

    }

}
