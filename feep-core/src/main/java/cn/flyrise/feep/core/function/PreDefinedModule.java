package cn.flyrise.feep.core.function;

import android.app.Activity;

/**
 * 预定义的模块
 * @author ZYP
 */
public final class PreDefinedModule {

	public int moduleId;
	public int icon;
	public Class<? extends Activity> moduleClass;

	public PreDefinedModule(int moduleId, int icon, Class<? extends Activity> moduleClass) {
		this.icon = icon;
		this.moduleId = moduleId;
		this.moduleClass = moduleClass;
	}

	public PreDefinedModule() { }

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PreDefinedModule that = (PreDefinedModule) o;

		return moduleId == that.moduleId;
	}

	@Override public int hashCode() {
		return moduleId;
	}
}
