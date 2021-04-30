package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocusDates;

/**
 * 类描述：考勤轨迹的时间Adapter
 *
 * @author 罗展健
 * @version 1.0
 * @date 2015年3月20日 下午4:48:03
 */
public class LocationDateAdapter extends RecyclerView.Adapter<LocationDateAdapter.ViewHolder> {

    private final Context mContext;
    private final List<LocusDates> dates;
    private int position = 0;

    private OnDateClickeItemListener mListener;

    public LocationDateAdapter(Context mContext, List<LocusDates> dates, OnDateClickeItemListener listener) {
        this.mContext = mContext;
        this.dates = dates;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.location_locus_date_item, null, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LocusDates date = dates.get(position);
        holder.tv.setText(date.getName());
        holder.iv.setVisibility(this.position == position ? View.VISIBLE : View.GONE);
        holder.back.setBackgroundColor(mContext.getResources().getColor(R.color.all_background_color));
        holder.back.setOnClickListener(view -> {
            setCurrentPosition(position);
            mListener.onDateClickeItem(date, position);
        });
    }

    @Override
    public int getItemCount() {
        return CommonUtil.isEmptyList(dates) ? 0 : dates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView iv;
        View back;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.items_textview);
            iv = itemView.findViewById(R.id.items_date_select_img);
            back = itemView.findViewById(R.id.background_layout_date);
        }
    }

    private void setCurrentPosition(int position) {
        this.position = position;
        notifyDataSetChanged();
    }

    public int getCurrenterPostion() {
        return this.position;
    }

    public interface OnDateClickeItemListener {
        void onDateClickeItem(LocusDates dates, int position);
    }
}
