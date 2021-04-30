package cn.flyrise.feep.location;

/**
 * 新建：陈冕;
 * 日期： 2018-8-9-10:03.
 */
public interface Sign {

	interface error {//快捷签到失败

		int noCustom = 2011;//不存在自定义地点
		int superRange = 2012;//存在自定义，但超出范围，有周边
		int superRangeNoPlace = 2013;//存在自定义，但超出范围,无周边（大山上）
		int noTime = 2014;//考勤组情况下，未到签到时间
		int workSuperRange = 2015;//考勤组情况下，不在范围内
		int network = 2016;//网络异常
		int signMany = 2017;//多次签到
	}

	interface state {//考勤统计

		int ATTENDANCE = 101;//出勤
		int REST = 102;//休假
		int LATE = 103;//迟到
		int EARLY = 104;//早退
		int ABSENCE_DUTY = 105;//缺卡
		int ABSENTEEISM = 106;//旷工
		int FIELD_PERSONNEL = 107;//外勤
		int NO_SIGN = 108;//未签到
		int ALREADY_SIGN = 109;//已签到
		int SHOULD_BE = 0;//应到人数

		Integer[] suffixDays = {ATTENDANCE, REST, ABSENTEEISM};//后缀是天的
		Integer[] redFonts = {LATE, EARLY, ABSENCE_DUTY, ABSENTEEISM};//红色字体显示
		Integer[] monthStatis = {ATTENDANCE, LATE, EARLY, ABSENCE_DUTY, FIELD_PERSONNEL};//能够查看月汇总详情的
	}

	interface loadMore {//加载更多

		int can_load_more = 301;//可以加载更多
		int no_load_more = 302;//没有更多了
		int success_load_more = 303;//加载成功
	}

}
