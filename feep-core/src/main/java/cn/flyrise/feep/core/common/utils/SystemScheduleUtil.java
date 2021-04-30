package cn.flyrise.feep.core.common.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

import cn.flyrise.feep.core.common.FELog;
import java.util.Calendar;
import java.util.TimeZone;

import rx.Observable;

/**
 * Created by klc on 2018/1/5.
 */

public class SystemScheduleUtil {

	private static final String CALENDAR_URL = "content://com.android.calendar/calendars";
	private static final String CALENDAR_EVENT_URL = "content://com.android.calendar/events";
	private static final String CALENDAR_REMINDERS_URL = "content://com.android.calendar/reminders";

	/**
	 * 把事件添加到系统日历事件项
	 * @param context context
	 * @param title 事件标题
	 * @param content 事件备注
	 * @param calendar 事件开始时间
	 */
	public static Observable<Integer> addToSystemCalendar(Context context, String title, String content,
			Calendar calendar) {
		return Observable
				.create(subscriber -> {
					try {
						String calendarId;
						Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDAR_URL), null, null, null, null);
						if (userCursor.moveToFirst()) {
							calendarId = userCursor.getString(userCursor.getColumnIndex("_id"));
						}
						else {
							subscriber.onNext(404);
							return;
						}
						ContentValues event = new ContentValues();
						event.put("title", title);
						event.put("calendar_id", calendarId);
						event.put(CalendarContract.Events.DTSTART, calendar.getTime().getTime());
						//华为手机有个破尿性，当开始时间跟结束时间相同的时候，不会提醒。
						if(Build.MANUFACTURER.contains("HUAWEI")){
							event.put(CalendarContract.Events.DTEND, calendar.getTime().getTime()+5*60*1000);
						}else{
							event.put(CalendarContract.Events.DTEND, calendar.getTime().getTime());
						}
						event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
						event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID().toString());
						Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDAR_EVENT_URL), event);
						ContentValues values = new ContentValues();
						values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
						values.put(CalendarContract.Reminders.MINUTES, 0);
						values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
						context.getContentResolver().insert(Uri.parse(CALENDAR_REMINDERS_URL), values);
						subscriber.onNext(200);
					} catch (Exception e) {
						e.printStackTrace();
						subscriber.onNext(404);
					} finally {
						subscriber.onCompleted();
					}
				});
	}
}
