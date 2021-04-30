package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.io.Serializable;

/**
 * @author CWW
 * @version 1.0 <br/>
 * 创建时间: 2013-3-19 上午10:23:03 <br/>
 * 类说明 :
 */
public class ReportDetailsItem implements Serializable {

	private static final long serialVersionUID = -3669783714605380173L;
	private String reportDetailsName;
	private String reportDetailsUrl;
	private String reportDetailsType;

	public String getReportDetailsName() {
		return reportDetailsName;
	}

	public void setReportDetailsName(String reportDetailsName) {
		this.reportDetailsName = reportDetailsName;
	}

	public String getReportDetailsUrl() {
		return reportDetailsUrl;
	}

	public void setReportDetailsUrl(String reportDetailsUrl) {
		this.reportDetailsUrl = reportDetailsUrl;
	}

	public int getReportDetailsType() {
		return CommonUtil.parseInt(reportDetailsType);
	}

	public void setReportDetailsType(int reportDetailsType) {
		this.reportDetailsType = reportDetailsType + "";
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
