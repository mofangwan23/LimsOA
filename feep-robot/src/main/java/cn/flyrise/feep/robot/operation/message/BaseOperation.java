package cn.flyrise.feep.robot.operation.message;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.module.OperationModule;
import cn.flyrise.feep.robot.module.RobotModuleItem;
import cn.flyrise.feep.robot.operation.RobotOperation;
import cn.squirtlez.frouter.FRouter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017-6-29.
 * 所有操作类的父类
 */

public abstract class BaseOperation implements RobotOperation {

	protected Context mContext;
	protected OperationModule mOperationModule;

	public void setContext(Context context) {
		this.mContext = context;
	}

	void setOperationModule(OperationModule module) {
		this.mOperationModule = module;
	}

	RobotModuleItem getModulleDetail(int type, String name) {
		return new RobotModuleItem.Builder()
				.setIndexType(Robot.adapter.ROBOT_CONTENT_LIST)
				.setModuleId(type)
				.setTitle(name)
				.setModuleParentType(mOperationModule.getMessageId())
				.setOperationType(mOperationModule.operationType)
				.create();
	}

	void openMessage() {
		int messageId = mOperationModule.getMessageId();
		Class activityClass = FunctionManager.findClass(messageId);
		Intent intent = new Intent(mContext, activityClass);
		intent.putExtra("moduleId", messageId);
		mContext.startActivity(intent);
	}

	void searchMessage(int type) {
		FRouter.build(mContext, "/message/search")
				.withInt("request_type", type)
				.withString("user_name", mOperationModule.username)
				.go();
	}

	//使用名字搜索
	void queryContact(String nameLike) {
		Observable.just(nameLike)
				.map(queryString -> CoreZygote.getAddressBookServices().queryContactName(queryString, 0))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(contacts -> {
					if (CommonUtil.isEmptyList(contacts)) {
						return;
					}

					if (isInterceptOperation()) {
						AddressBook addressBook = contacts.get(0);
						handleAddressBook(addressBook);
						return;
					}

					if (mOperationModule.grammarResultListener != null) {
						mOperationModule.grammarResultListener.onGrammarResultItems(convertAddressBook(contacts));
					}
				}, exception -> {
				});
	}

	//是否拦截返回的用户详情，给到子类操作
	private boolean isInterceptOperation() {
		int messageId = mOperationModule.getMessageId();
		return messageId == 0           // 代办
				|| messageId == 1       // 已办
				|| messageId == 4       // 已发
				|| messageId == 10      // 新建表单
				|| messageId == 14      // 工作计划
				|| messageId == 44      // 审批
				|| messageId == 45;     // 嘟嘟
	}

	//用户Id
	public void handleAddressBook(AddressBook addressBook) {
	}

	private List<RobotModuleItem> convertAddressBook(List<AddressBook> addressBooks) {
		List<RobotModuleItem> robotModuleItems = new ArrayList<>();
		for (AddressBook addressBook : addressBooks) {
			robotModuleItems.add(new RobotModuleItem.Builder()
					.setIndexType(Robot.adapter.ROBOT_CONTENT_LIST)
					.setOperationType(mOperationModule.operationType)
					.setModuleParentType(mOperationModule.getMessageId())
					.setAddressBook(addressBook)
					.create());
		}
		return robotModuleItems;
	}

	String getSearchText() {
		if (mOperationModule == null) {
			return "";
		}
		if (!TextUtils.isEmpty(mOperationModule.wildcard)) {
			return mOperationModule.wildcard;
		}
		if (!TextUtils.isEmpty(mOperationModule.username)) {
			return mOperationModule.username;
		}
		if (!TextUtils.isEmpty(mOperationModule.dateTime)) {
			return getSearchDateOrTime(mOperationModule.dateTime);
		}
		return "";
	}

	private String getSearchDateOrTime(String text) {
		SimpleDateFormat dateFormat;
		try {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return getMonthAndDay(dateFormat.parse(text), text);
		} catch (ParseException e) {
			try {
				dateFormat = new SimpleDateFormat("yy-MM");
				return getYearAndMonth(dateFormat.parse(text), text);
			} catch (ParseException e1) {
				try {
					dateFormat = new SimpleDateFormat("MM-dd");
					return getMonthAndDay(dateFormat.parse(text), text);
				} catch (ParseException e2) {
					e2.printStackTrace();
				}
			}
		}
		return text;
	}

	private String getMonthAndDay(Date date, String text) {
		Calendar calendar = Calendar.getInstance();
		if (date == null) {
			return text;
		}
		calendar.setTime(date);
		return (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH);

	}

	private String getYearAndMonth(Date date, String text) {
		Calendar calendar = Calendar.getInstance();
		if (date == null) {
			return text;
		}
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1);
	}

	@Override
	public void open() {
	}

	@Override
	public void invita() {
	}

	@Override
	public void search() {
	}

	@Override
	public void create() {
	}
}
