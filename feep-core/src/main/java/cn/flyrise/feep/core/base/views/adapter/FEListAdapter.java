package cn.flyrise.feep.core.base.views.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KLC on 2016/12/28.
 */
abstract public class FEListAdapter<T> extends BaseRecyclerAdapter {

    protected List<T> dataList;

    @Override
    public int getDataSourceCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    abstract public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    abstract public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType);

    public void setDataList(List<T> showList) {
        this.dataList = showList;
        this.notifyDataSetChanged();
    }

    public void addDataList(List<T> showList) {
        if (this.dataList == null) {
            this.dataList = new ArrayList<>();
        }
        this.dataList.addAll(showList);
        this.notifyDataSetChanged();
    }
}
