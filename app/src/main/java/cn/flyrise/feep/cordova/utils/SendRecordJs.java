package cn.flyrise.feep.cordova.utils;

import android.support.annotation.Keep;
import java.util.List;
import java.util.Map;

@Keep
public class SendRecordJs {

	private String uiControlType;
	private String uiControlId;
	private List<Map<String, String>> referenceItems;
	private static SendRecordJs sendJs;

	public String getUiControlType() {
		return uiControlType;
	}

	public void setUiControlType(String uiControlType) {
		this.uiControlType = uiControlType;
	}

	public String getUiControlId() {
		return uiControlId;
	}

	public void setUiControlId(String uiControlId) {
		this.uiControlId = uiControlId;
	}

	public void setReferenceItems(List<Map<String, String>> sn) {
		this.referenceItems = sn;
	}

	public static SendRecordJs setData(String uiControlType, String uiControlId, List<Map<String, String>> send2service) {
		if (sendJs == null) sendJs = new SendRecordJs();
		sendJs.setUiControlType(uiControlType);
		sendJs.setUiControlId(uiControlId);
		sendJs.referenceItems = send2service;
		return sendJs;
	}
}
