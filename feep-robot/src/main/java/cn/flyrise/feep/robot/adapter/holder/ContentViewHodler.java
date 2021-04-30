package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.adapter.RobotUnderstanderAdapter;
import cn.flyrise.feep.robot.contract.RobotEntityContractKt;
import cn.flyrise.feep.robot.entity.RobotResultItem;
import cn.flyrise.feep.robot.module.RobotModuleItem;
import cn.flyrise.feep.robot.util.RobotFilterChar;

/**
 * 新建：陈冕;
 * 日期： 2017-8-7-10:58.
 * 内容居中显示的layout(温馨提示)
 */

public class ContentViewHodler extends RobotViewHodler {

	private LinearLayout mLayout; //最外层的布局
	private ImageView mImageHeadIcon;
	private TextView mTvHeadTitle;

	private LinearLayout mLayoutHead; //提示框的头
	private LinearLayout mLayoutSwitch;//提示框选择控件
	private TextView mCancelTV; //取消
	private TextView mSaveTV; //保存

	private TextView mTvConTitle;
	private TextView mTvConSubTitle; //作者名称及朝代
	private TextView mTvConContent;

	private Context mContext;
	private RobotUnderstanderAdapter.OnRobotClickeItemListener mListener;

	private long currentTime;

	public ContentViewHodler(View itemView, Context context, RobotUnderstanderAdapter.OnRobotClickeItemListener listener) {
		super(itemView);
		mContext = context;
		mListener = listener;
		mLayout = itemView.findViewById(R.id.con_layout);
		mLayoutHead = itemView.findViewById(R.id.head_title);
		mImageHeadIcon = itemView.findViewById(R.id.head_icon);
		mTvHeadTitle = itemView.findViewById(R.id.head_title_tv);

		mTvConContent = itemView.findViewById(R.id.con_content_tv);
		mTvConTitle = itemView.findViewById(R.id.con_content_title);
		mTvConSubTitle = itemView.findViewById(R.id.con_content_subtitle);

		mLayoutSwitch = itemView.findViewById(R.id.switch_layout);
		mCancelTV = itemView.findViewById(R.id.cancel);
		mSaveTV = itemView.findViewById(R.id.save);
	}

	public void setContentViewHodler(int position, int size) {
		if (TextUtils.equals(item.service, RobotEntityContractKt.poetry)) {
			setPoetry(item);
			return;
		}
		if (!TextUtils.isEmpty(item.title)) {
			mTvHeadTitle.setText(item.title);
		}
		if (TextUtils.isEmpty(item.content)) {
			if (item.htmlContent != null) {
				mTvConContent.setText(item.htmlContent);
			}
		}
		else {
			mTvConContent.setText(item.content);
		}
		mTvConContent.setSingleLine(false);
		FEImageLoader.load(mContext, mImageHeadIcon, item.icon, R.drawable.robot_understander_icon);
		setListener(item, position, size);
	}

	private void setListener(RobotModuleItem item, int position, int size) {
		mLayout.setOnClickListener(v -> {
			if (mListener != null) {
				mListener.onItem(item);
			}
		});
		if (!item.isContentViewSwitch || position != (size - 1)) {
			mLayoutSwitch.setVisibility(View.GONE);
			return;
		}
		mLayoutSwitch.setVisibility(View.VISIBLE);
//        mCancelTV.setOnClickListener(v -> {
//            if (mListener != null) {
//                mListener.onSwitch(false);
//            }
//        });
//        mSaveTV.setOnClickListener(v -> {
//            if ((System.currentTimeMillis() - currentTime) < 500) {
//                return;
//            }
//            currentTime = System.currentTimeMillis();
//            if (mListener != null) {
//                mListener.onSwitch(true);
//            }
//        });
	}

	private void setPoetry(RobotModuleItem item) { //诗词
		if (CommonUtil.isEmptyList(item.results)) {
			return;
		}
		RobotResultItem resultItem = item.results.get(0);
		if (resultItem == null) {
			return;
		}
		mTvConTitle.setText(resultItem.title);
		mTvConSubTitle.setText(resultItem.dynasty + "•" + resultItem.author);
		mTvConContent.setText(RobotFilterChar.getPoetryContentList(resultItem.content));
		mLayoutSwitch.setVisibility(View.GONE);
		mLayoutHead.setVisibility(View.GONE);
		mTvConTitle.setVisibility(View.VISIBLE);
		mTvConSubTitle.setVisibility(View.VISIBLE);
	}

	@Override
	public void onDestroy() {
		mLayoutSwitch.setVisibility(View.GONE);
		mLayoutHead.setVisibility(View.VISIBLE);
		mTvConTitle.setVisibility(View.GONE);
		mTvConSubTitle.setVisibility(View.GONE);
	}
}
