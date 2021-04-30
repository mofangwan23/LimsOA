/**
 * 公告
 */
package cn.flyrise.feep.news;

import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.AppMenu;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import java.util.List;

import cn.flyrise.feep.commonality.bean.MenuInfo;

public class AnnouncementListActivity extends NewsBulletinActivity {

	@Override public Module menuInfo() {
		Module module = FunctionManager.findModule(Func.Announcement);
		if (module != null) {
			return module;
		}
		throw new NullPointerException("Could not found the announcement menu information.");
	}

}