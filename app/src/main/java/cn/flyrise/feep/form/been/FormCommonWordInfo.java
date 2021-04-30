package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import org.json.JSONException;
import org.json.JSONObject;


import cn.flyrise.feep.core.common.utils.GsonUtil;

/**
 * @author CWW
 * @version 1.0 <br/>
 * 创建时间: 2013-5-7 下午2:46:28 <br/>
 * 类说明 :表单常用语数据（填入到webview显示的表单上）
 */
@Keep
public class FormCommonWordInfo {

	private String uiControlId;
	private String uiControlType;
	private String useCommonValue;

	public String getUiControlId() {
		return uiControlId;
	}

	public void setUiControlId(String uiControlId) {
		this.uiControlId = uiControlId;
	}

	public int getUiControlType() {
		return CommonUtil.parseInt(uiControlType);
	}

	public void setUiControlType(int uiControlType) {
		this.uiControlType = uiControlType + "";
	}

	public String getUseCommonValue() {
		return useCommonValue;
	}

	public void setUseCommonValue(String useCommonValue) {
		this.useCommonValue = useCommonValue;
	}

	/**
	 * 把本对象转换成json字符串
	 */
	public JSONObject getProperties() {
		final StringBuilder strBuf = new StringBuilder("{\"OcToJs_JSON\":");
		strBuf.append(GsonUtil.getInstance().toJson(this)).append("}");
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(strBuf.toString());
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public String getJson() {
		return "jsBridge.trigger('SetWebHTMLEditorContent',{\"OcToJs_JSON\":" + GsonUtil.getInstance().toJson(this) + "})";
	}
}
