package cn.flyrise.feep.collaboration.matter.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by klc on 2017/5/16.
 */
@Keep
public class DirectoryNode {

    public DirectoryNode fatherNode;

    @SerializedName("ID")
    public String id;

    @SerializedName("FNAME")
    public String name;


    @SerializedName("attrb")
    public String attr;

    @SerializedName("list")
    public List<DirectoryNode> childNode;

}
