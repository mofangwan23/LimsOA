package cn.flyrise.feep.study.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.study.adapter.TaskListAdapter.ViewHolder;
import cn.flyrise.feep.study.entity.TrainingTaskBean;
import cn.flyrise.study.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.List;

public class TaskListAdapter extends BaseQuickAdapter<TrainingTaskBean,ViewHolder> {

	public TaskListAdapter(int layoutResId,List<TrainingTaskBean> tasklist) {
		super(layoutResId, tasklist);
	}

	@Override protected void convert(ViewHolder helper, TrainingTaskBean item) {
		if (!TextUtils.isEmpty(item.getTASKNAME())){
			helper.tvName.setText(item.getTASKNAME());
		}

		if (!TextUtils.isEmpty(item.getTASKNO())){
			helper.tvNo.setText(item.getTASKNO());
		}
	}

	class ViewHolder extends BaseViewHolder {

		TextView tvName;
		TextView tvNo;

		public ViewHolder(View view) {
          super(view);
          if (view!=null){
	          tvName = view.findViewById(R.id.item_task_name);
	          tvNo = view.findViewById(R.id.item_task_no);
          }
		}
	}
}



