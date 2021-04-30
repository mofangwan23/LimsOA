package cn.flyrise.android.protocol.entity.workplan;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import java.util.ArrayList;

import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.android.protocol.model.User;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-26 上午10:46:17 <br/>
 *          类说明 :
 */
public class WorkPlanDetailResponse extends ResponseContent {
    private String id;
    private String nextid;
    private String upid;
    private String content;
    private String title;
    private String sendUserID;
    private String sendUser;
    private String sendTime;
    private String startTime;
    private String endTime;
    private String department;
    private ArrayList<User> receiveUsers;
    private ArrayList<User> CCUsers;
    private ArrayList<User> noticeUsers;
    private ArrayList<AttachmentBean> attachments;
    private ArrayList<Reply> replies;

    //klc 2018-05-08 加入，为了计划暂存用的
    private String attachmentGUID;

    /**
     * change by klc on 2018-6-11 11:04
     * msg : Add
     * workPlanId 计划类型：1.日计划 2.周计划 3.月计划 4.其他计划
     */
    protected String type;

    public String favoriteId;
    public String taskId; // 2018/5/29 增加的，用作收藏请求的ID

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getNextid () {
        return nextid;
    }

    public void setNextid (String nextid) {
        this.nextid = nextid;
    }

    public String getUpid () {
        return upid;
    }

    public void setUpid (String upid) {
        this.upid = upid;
    }

    public String getContent () {
        return content;
    }

    public void setContent (String content) {
        this.content = content;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getSendUserID () {
        return sendUserID;
    }

    public void setSendUserID (String sendUserID) {
        this.sendUserID = sendUserID;
    }

    public String getSendUser () {
        return sendUser;
    }

    public void setSendUser (String sendUser) {
        this.sendUser = sendUser;
    }

    public String getSendTime () {
        return sendTime;
    }

    public void setSendTime (String sendTime) {
        this.sendTime = sendTime;
    }

    public String getStartTime () {
        return startTime;
    }

    public void setStartTime (String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime () {
        return endTime;
    }

    public void setEndTime (String endTime) {
        this.endTime = endTime;
    }

    public String getDepartment () {
        return department;
    }

    public void setDepartment (String department) {
        this.department = department;
    }

    public ArrayList<User> getReceiveUsers () {
        return receiveUsers;
    }

    public void setReceiveUsers (ArrayList<User> receiveUsers) {
        this.receiveUsers = receiveUsers;
    }

    public ArrayList<User> getCCUsers () {
        return CCUsers;
    }

    public void setCCUsers (ArrayList<User> cCUsers) {
        CCUsers = cCUsers;
    }

    public ArrayList<User> getNoticeUsers () {
        return noticeUsers;
    }

    public void setNoticeUsers (ArrayList<User> noticeUsers) {
        this.noticeUsers = noticeUsers;
    }

    public ArrayList<AttachmentBean> getAttachments () {
        return attachments;
    }

    public void setAttachments (ArrayList<AttachmentBean> attachments) {
        this.attachments = attachments;
    }

    public ArrayList<Reply> getReplies () {
        return replies;
    }

    public void setReplies (ArrayList<Reply> replies) {
        this.replies = replies;
    }

    public String getAttachmentGUID() {
        return attachmentGUID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
