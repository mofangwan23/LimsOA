package cn.flyrise.feep.robot.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.robot.R;

/**
 * 新建：陈冕;
 * 日期： 2017-8-7-11:25.
 * 列表标题(轮询提示语标题)
 */

public class TitleViewHodler extends RobotViewHodler {

	private TextView titleTv;

	public TitleViewHodler(View itemView) {
		super(itemView);
		titleTv = itemView.findViewById(R.id.title_text);
	}

	@Override
	public void onDestroy() {

	}

	public void setTitleViewHodler() {
		if (TextUtils.isEmpty(item.title)) {
			return;
		}
		titleTv.setText(item.title);
	}

}
