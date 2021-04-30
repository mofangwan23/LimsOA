package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.image.loader.FEImageLoader;

/**
 * 新建：陈冕;
 * 日期： 2017-8-7-11:20.
 * 内容左边显示的layout(讯飞返回数据，对话)
 */

public class LeftViewHodler extends RobotViewHodler {

	private ImageView leftIcon;
	private TextView leftTv;

	private Context mContext;

	public LeftViewHodler(View itemView, Context context) {
		super(itemView);
		mContext = context;
		leftIcon = itemView.findViewById(R.id.left_user_icon);
		leftTv = itemView.findViewById(R.id.input_left_text);
	}

	public void setLeftViewHodler() {
		if (!TextUtils.isEmpty(item.title)) {
			leftTv.setText(item.title);
			leftTv.setBackgroundResource(R.drawable.robot_aiui_input_left_bg);
		}
		FEImageLoader.load(mContext, leftIcon, item.icon, R.drawable.robot_understander_icon);
	}

	@Override
	public void onDestroy() {

	}
}
