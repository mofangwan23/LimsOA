package cn.flyrise.feep.location.bean;

import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-17:47.
 */

public class SignInLeaderMonthStatisList {

	public int count;//总人数（包含新入职和离职人员）

	public int exceptCount;//异常人数

	public List<SignInLeaderMonthItem> record;
}
