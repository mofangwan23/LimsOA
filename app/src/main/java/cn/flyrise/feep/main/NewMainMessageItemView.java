package cn.flyrise.feep.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.main.message.MessageVO;
import com.drop.DropCover;
import com.drop.WaterDrop;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ZYP
 * @since 2017-04-01 10:11
 */
public class NewMainMessageItemView extends RelativeLayout {

	private MessageVO mMessageVO;
	private TextView mTvMessage;
	private TextView mTvTime;
	private WaterDrop mWaterDrop;

	public NewMainMessageItemView(Context context) {
		this(context, null);
	}

	public NewMainMessageItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NewMainMessageItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_new_main_message_head_item, this);
		ImageView ivIcon = findViewById(R.id.ivIcon);
		TextView tvTitle = findViewById(R.id.tvTitle);
		mTvMessage = findViewById(R.id.tvMessage);
		mTvTime = findViewById(R.id.tvTime);
		mWaterDrop = findViewById(R.id.badge_view);

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NewMainMessageItemView, defStyleAttr, 0);
		String title = typedArray.getString(R.styleable.NewMainMessageItemView_tvTitle);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		Drawable drawable = typedArray.getDrawable(R.styleable.NewMainMessageItemView_ivIcon);
		if (drawable != null) {
			ivIcon.setImageDrawable(drawable);
		}
		typedArray.recycle();
	}

	public void setBadgeView(int count) {
		if (mWaterDrop == null) {
			return;
		}

		if (count == 0) {
			mWaterDrop.setVisibility(View.INVISIBLE);
			return;
		}

		String number = (count > 0 && count <= 99) ? String.valueOf(count) : "99+";
		mWaterDrop.setVisibility(View.VISIBLE);
		mWaterDrop.setText(number);
	}

	public void setMessage(String message) {
		if (mTvMessage == null) {
			return;
		}

		mTvMessage.setText(message);
	}

	public void setTime(String time) {
		if (mTvTime == null) {
			return;
		}

		if (TextUtils.isEmpty(time)) {
			mTvTime.setVisibility(View.GONE);
		}
		else {
			mTvTime.setVisibility(View.VISIBLE);
			mTvTime.setText(formatTime(time));
		}
	}

	public MessageVO getMessageVO() {
		return mMessageVO;
	}

	public void setMessageVO(MessageVO messageVO) {
		this.mMessageVO = messageVO;
		this.setMessage(messageVO.getTitle());
		this.setTime(messageVO.getSendTime());
		this.setBadgeView(CommonUtil.parseInt(messageVO.getBadge()));
	}


	public static String formatTime(String time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(DateUtil.getServiceTime()));
		if (time.contains(":") && time.contains("-") && time.indexOf(" ") > 0) {
			time = time.split(" ")[0];
		}

		if (time.contains(":")) {
			String[] hourAndMin = time.split(":");
			calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hourAndMin[0]));
			calendar.set(Calendar.MINUTE, Integer.valueOf(hourAndMin[1]));
		}
		else if (time.equals("昨天")) {
			calendar.add(Calendar.DATE, -1);
		}
		else if (time.contains("-")) {
			String[] date = time.split("-");
			calendar.set(Calendar.YEAR, Integer.valueOf("20" + date[0]));
			calendar.set(Calendar.MONTH, Integer.valueOf(date[1]) - 1);
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date[2]));
		}
		else {//下面的星期日啊这种数据要用服务器的时间来做比较的。
			String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
			int msgDayOfWeek = 0;
			for (int i = 0; i < 7; i++) {
				if (weekDays[i].equals(time)) {
					msgDayOfWeek = i;
				}
			}
			int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			int interval;
			if (nowDayOfWeek >= msgDayOfWeek) {
				interval = nowDayOfWeek - msgDayOfWeek;
			}
			else {
				interval = nowDayOfWeek + (7 - msgDayOfWeek);
			}
			calendar.add(Calendar.DATE, interval * -1);
		}
		return DateUtil.formatTimeForList(calendar.getTimeInMillis());
	}

	public void setOnDragCompeteListener(DropCover.OnDragCompeteListener listener) {
		if (mWaterDrop != null && listener != null) {
			mWaterDrop.setOnDragCompeteListener(listener);
		}
	}
}
