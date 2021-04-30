package cn.flyrise.feep.meeting7.ui.bean;

/**
 * @author 社会主义接班人
 * @since 2018-07-31 10:35
 * 巨他妈傻逼的一个工具类，右面再优化
 */
public class MeetingUpdateTookKit {

	private MeetingUpdateTookKit() { }

	public static MeetingUpdateTookKit sInstance;

	private MeetingDetail mDetail;

	public static void saveData(MeetingDetail detail) {
		sInstance = new MeetingUpdateTookKit();
		sInstance.mDetail = detail;
	}

	public static MeetingDetail getData() {
		if (sInstance != null) {
			return sInstance.mDetail;
		}
		return null;
	}

}
