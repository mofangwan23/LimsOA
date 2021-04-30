package cn.flyrise.feep.robot.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.flyrise.feep.robot.module.RobotModuleItem;

/**
 * 新建：陈冕;
 * 日期： 2017-12-4-15:56.
 */

public abstract class RobotViewHodler extends RecyclerView.ViewHolder {

    protected RobotModuleItem item;

    public RobotViewHodler(View itemView) {
        super(itemView);
    }

    public void setRobotModuleItem(RobotModuleItem item) {
        this.item = item;
    }

    public abstract void onDestroy();
}
