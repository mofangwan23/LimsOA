package cn.flyrise.feep.collaboration.model;

import android.support.annotation.Keep;
import android.text.TextUtils;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.feep.core.common.utils.CommonUtil;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klc on 2017/4/17.
 */
@Keep
public class Collaboration {

    public String id;
    public String title;
    public String content;
    public String important;
    @SerializedName("cflow")
    public Flow flow;
    @SerializedName("track")
    private String track;
    private boolean isModify;
    public String attachmentGUID;
    public String relationflow;
    public String option;
    public String isChangeIdea;
    //原文的关联事项
    private List<AttachmentBean> relationItem;
    //原文的附件信息
    @SerializedName("attachment")
    private List<AttachmentBean> attachmentList;
    //补充正文的关联事项
    private List<SupplyContent> supplyContents;

    public Collaboration() {
    }

    public boolean isUrgent() {
        return important != null && "急件".equals(important);
    }

    public boolean isModify() {
        return isModify;
    }

    public void setTrace(boolean isTrace) {
        if (isTrace) {
            this.track = "1";
        } else {
            this.track = "0";
        }
    }

    public boolean isTrace() {
        return !TextUtils.isEmpty(track) && "1".equals(track);
    }

    public void setModify(boolean modify) {
        isModify = modify;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    //获取关联事项（原文+补充正文的)
    public List<AttachmentBean> getRelationList() {
        List<AttachmentBean> items = new ArrayList<>();
        if (!CommonUtil.isEmptyList(relationItem)) {
            items.addAll(relationItem);
        }
        items.addAll(getSupplyContentRelationList());
        return items;
    }

    public List<AttachmentBean> getSupplyContentRelationList() {
        List<AttachmentBean> matterList = new ArrayList<>();
        if (!CommonUtil.isEmptyList(supplyContents)) {
            for (SupplyContent supplyContent : supplyContents) {
                if (!CommonUtil.isEmptyList(supplyContent.relationItem)) {
                    matterList.addAll(supplyContent.relationItem);
                }
            }
        }
        return matterList;
    }

    //获取附件内容（原文+补充正文的)
    public List<AttachmentBean> getAttachmentList() {
        List<AttachmentBean> items = new ArrayList<>();
        if (!CommonUtil.isEmptyList(attachmentList)) {
            items.addAll(attachmentList);
        }
        if (!CommonUtil.isEmptyList(supplyContents)) {
            for (SupplyContent supplyContent : supplyContents) {
                if (!CommonUtil.isEmptyList(supplyContent.attachmentList)) {
                    items.addAll(supplyContent.attachmentList);
                }
            }
        }
        return items;
    }

    public void setAttachmentList(List<AttachmentBean> attachmentList) {
        this.attachmentList = attachmentList;
    }

    protected class SupplyContent {
        private String sendUser;
        private String sendUserID;
        private String sendUserImg;
        private String sendTime;
        private String content;
        //补充正文的的关联事项
        public List<AttachmentBean> relationItem;
        //补充正文的附件信息
        @SerializedName("attachments")
        public List<AttachmentBean> attachmentList;
    }
}