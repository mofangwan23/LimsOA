package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.adapter.HolidaySubAdapter;
import cn.flyrise.feep.robot.entity.RobotHolidayItem;

/**
 * 新建：陈冕;
 * 日期： 2017-12-5-10:23.
 */

public class HolidayViewHodler extends RobotViewHodler {

    private TextView mTvHolidayText;

    private RecyclerView mRecyclerView;

    public HolidayViewHodler(View itemView, Context mContext) {
        super(itemView);
        mTvHolidayText = itemView.findViewById(R.id.holiday_text);
        mRecyclerView = itemView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    public void setHolidayViewHodler() {
        mTvHolidayText.setText(item.content);
        List<RobotHolidayItem> holidayItems = item.holidayItems;
        if (CommonUtil.isEmptyList(holidayItems)) {
            return;
        }
        Collections.sort(holidayItems);
        mRecyclerView.setAdapter(new HolidaySubAdapter(holidayItems));
    }

    @Override
    public void onDestroy() {

    }
}
