package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.robot.R;

/**
 * 新建：陈冕;
 * 日期： 2017-8-7-11:23.
 */

public class RightViewHodler extends RobotViewHodler {

	//内容右边显示的layout(一般为用户输入语)
	private ImageView rightIcon;
	private TextView rightTv;

	private Context mContext;

	public RightViewHodler(View itemView, Context context) {
		super(itemView);
		this.mContext = context;
		rightIcon = itemView.findViewById(R.id.right_user_icon);
		rightTv = itemView.findViewById(R.id.input_right_text);
	}

	public void setRightViewHodler() {
		if (!TextUtils.isEmpty(item.title)) {
			rightTv.setText(item.title);
			rightTv.setBackgroundResource(R.drawable.robot_user_input_right_bg);
		}
		ILoginUserServices services = CoreZygote.getLoginUserServices();
		if (services != null) {
			FEImageLoader.load(mContext, rightIcon, services.getServerAddress() + services.getUserImageHref(), services.getUserId(),
					services.getUserName());
		}
	}

	@Override
	public void onDestroy() {

	}
}
