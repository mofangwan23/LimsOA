package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.news.bean.RelatedNews;

/**
 * @author CWW
 * @version 1.0 <br/>
 * 创建时间: 2013-3-12 下午3:51:45 <br/>
 * 类说明 :
 */
public class NewsDetailsResponse extends ResponseContent {

	private String id;
	private String title;
	private String sendUser;
	private String sendUserID;
	private String sendTime;
	private String content;
	private String readNums;
	public String favoriteId;
	private List<AttachmentBean> attachments;
	private List<RelatedNews> relatedNews; // 相关阅读，by 2015-04-28

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSendUser() {
		return sendUser;
	}

	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}

	public String getSendUserID() {
		return sendUserID;
	}

	public void setSendUserID(String sendUserID) {
		this.sendUserID = sendUserID;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getReadNums() {
		return readNums;
	}

	public void setReadNums(String readNums) {
		this.readNums = readNums;
	}

	public List<AttachmentBean> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentBean> attachments) {
		this.attachments = attachments;
	}

	public List<RelatedNews> getRelatedNews() {
		return relatedNews;
	}

	public void setRelatedNews(List<RelatedNews> relatedNews) {
		this.relatedNews = relatedNews;
	}

}
