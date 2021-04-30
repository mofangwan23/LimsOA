package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.robot.R;

/**
 * 新建：陈冕;
 * 日期： 2017-8-7-10:58.
 * 内容居中显示的layout(温馨提示)
 */

public class RiddleViewHodler extends RobotViewHodler {

    private LinearLayout mLayoutHead; //提示框的头

    private TextView mTvConTitle;
    private TextView mTvConSubTitle; //作者名称及朝代
    private TextView mTvConContent;

    private TextView mTvRiddle;//谜语答案

    private Context mContext;

    public RiddleViewHodler(View itemView, Context context) {
        super(itemView);
        mContext = context;
        mLayoutHead = itemView.findViewById(R.id.head_title);

        mTvConContent = itemView.findViewById(R.id.con_content_tv);
        mTvConTitle = itemView.findViewById(R.id.con_content_title);
        mTvConSubTitle = itemView.findViewById(R.id.con_content_subtitle);

        mTvRiddle = itemView.findViewById(R.id.riddle_answer);
    }

    public void setContentViewHodler() {
        if (item.riddleItem == null) {
            return;
        }
        mTvConTitle.setText("猜谜语");
        mTvConContent.setText(item.riddleItem.title);
        mTvRiddle.setOnClickListener(v -> mTvRiddle.setText(item.riddleItem.answer));
        mTvRiddle.setVisibility(View.VISIBLE);
        mLayoutHead.setVisibility(View.GONE);
        mTvConTitle.setVisibility(View.VISIBLE);
        mTvConSubTitle.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mTvRiddle.setVisibility(View.GONE);
        mTvRiddle.setText(mContext.getResources().getString(R.string.robot_click_riddle));
    }
}
