/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-18 下午4:50:00
 */
package cn.flyrise.feep.core.network.request;

import java.util.List;
import java.util.Map;

/**
 * 类功能描述：</br>
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class FileRequestContent {

	private List<String> files;
	private String attachmentGUID;
	private String updateType;   // TODO 只用于邮件,知识中心
	private List<String> deleteFileIds;
	private String audioTime;//如果是录音附件，需要将录音时间传上去

	private String copyFileIds; //告诉服务器哪些附件需要copy一份 ，有一些转发的操作，需要拷贝一份源文件

	private Map<String, String> valueMap;

	public Map<String, String> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<String, String> valueMap) {
		this.valueMap = valueMap;
	}

	public String getAudioTime() {
		return audioTime;
	}

	public void setAudioTime(String audioTime) {
		this.audioTime = audioTime;
	}

	public boolean isEmpty() {
		boolean empty = true;
		if (files != null && files.size() > 0) {
			empty = false;
		}

		if (deleteFileIds != null && deleteFileIds.size() > 0) {
			empty = false;
		}
		return empty;
	}

	public String getAttachmentGUID() {
		return attachmentGUID;
	}

	public void setAttachmentGUID(String attachmentGUID) {
		this.attachmentGUID = attachmentGUID;
	}

	public List<String> getDeleteFileIds() {
		return deleteFileIds;
	}

	public void setDeleteFileIds(List<String> attachmentItemIds) {
		this.deleteFileIds = attachmentItemIds;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public String getUpdateType() {
		return updateType;
	}

	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}

	public void setCopyFileIds(String copyFileIds) {
		this.copyFileIds = copyFileIds;
	}

	public String getCopyFileIds() {
		return copyFileIds;
	}
}
