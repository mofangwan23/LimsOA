package cn.flyrise.feep.core.function;

import android.support.annotation.Keep;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 */
@Keep
public class Module {

	public String id;
	public String name;
	public String nums;
	public String icon;
	public String url;
	public boolean isNative;
	@SerializedName("haveNews")
	public boolean hasNews;
	public String category;         // FEv7.0 新增

	public Module() { }

	public Module(String id, String name) {
		this.id = id;
		this.name = name;
		this.isNative = true;
	}

	public int getModuleId() {
		return CommonUtil.parseInt(id);
	}

}
