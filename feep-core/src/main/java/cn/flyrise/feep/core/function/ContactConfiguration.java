package cn.flyrise.feep.core.function;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 社会主义接班人
 * @since 2018-09-04 16:31
 */
public class ContactConfiguration {

	private ContactConfiguration() { }

	private static final class Singleton {

		private static final ContactConfiguration sInstance = new ContactConfiguration();
	}

	public static ContactConfiguration getInstance() {
		return Singleton.sInstance;
	}

	private Set<String> mCannotSelectButCheckedUsers = new HashSet<>();

	//默认被选中的，不可取消选中
	public void addUserCannotSelectButChecked(String userId) {
		mCannotSelectButCheckedUsers.add(userId);
	}

	public boolean isUserCannotSelectedButCheck(String userId) {
		return mCannotSelectButCheckedUsers.contains(userId);
	}

	public void releaseCache() {
		mCannotSelectButCheckedUsers.clear();
	}

}
