/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;


import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.feep.collaboration.matter.model.DirectoryNode;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class MatterFolderTreeResponse extends ResponseContent {

    public Result result;


    public class Result {

        @SerializedName("GroupFolderTree")
        public List<DirectoryNode> groupFolderTree;
        @SerializedName("UnitFolderTree")
        public List<DirectoryNode> unitFolderTree;
        @SerializedName("PersonalFolderTree")
        public List<DirectoryNode> personalFolderTree;

    }

}
