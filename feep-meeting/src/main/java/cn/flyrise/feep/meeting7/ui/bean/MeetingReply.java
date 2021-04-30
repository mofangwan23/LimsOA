package cn.flyrise.feep.meeting7.ui.bean;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-06-22 09:46
 */
public class MeetingReply {

	/**
	 * "id":"11470",
	 * "content":"sfsafasfsafasf",
	 * "sendTime":"2018-06-15 15:28",
	 * "sendUser":"高强",//意见提出者
	 * "feClient":"iphone",
	 * "sendUserID":"5569",//意见提出者Id
	 * "sendUserImg":"/images/user/336CFDF0-28C6-F15D-575E-9CB2FBD9BA61.png",//意见提出者图片
	 * "attachments"
	 **/

	public String id;           // 意见Id
	public String content;      // 意见内容
	public String sendTime;     // 发表日期
	public String sendUser;
	public String sendUserID;
	public String sendUserImg;
	public String feClient;     // 客户端类型
	public List<AttachmentBean> attachments;
	public List<MeetingReply> subReplies;

}
