package cn.flyrise.feep.core.function;

import android.support.annotation.Keep;
import android.text.TextUtils;

/**
 * @author ZYP
 */
@Keep
public class Category {

	public String key;
	public String value;
	public String editable; // 1 可编辑

	public Category() { }

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Category)) return false;

		Category category = (Category) o;

		if (!key.equals(category.key)) return false;
		return value.equals(category.value);
	}

	@Override public int hashCode() {
		int result = key.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	public boolean isEditable() {
		return TextUtils.equals(editable, "1");
	}

	public static Category quickShortCut() {
		Category shortCut = new Category();
		shortCut.key = "10086";
		shortCut.value = "快捷入口";
		shortCut.editable = "1";
		return shortCut;
	}
}
