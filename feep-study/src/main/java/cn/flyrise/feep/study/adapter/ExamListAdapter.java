package cn.flyrise.feep.study.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.study.adapter.ExamListAdapter.ViewHolder;
import cn.flyrise.feep.study.entity.GetQuestionResponse;
import cn.flyrise.feep.study.entity.TrainingSignResponse;
import cn.flyrise.study.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.List;

public class ExamListAdapter extends BaseQuickAdapter<TrainingSignResponse.QueryBean, ViewHolder> {


	public ExamListAdapter(int layoutResId,List<TrainingSignResponse.QueryBean> examList) {
		super(layoutResId, examList);
	}

	@Override protected void convert(ViewHolder helper, TrainingSignResponse.QueryBean item) {
		if (!TextUtils.isEmpty(item.getPAPER_NAME())){
			helper.tvName.setText(item.getPAPER_NAME());
		}

		if (!TextUtils.isEmpty(item.getID())){
			helper.tvNo.setText(item.getID());
		}

		if (!TextUtils.isEmpty(item.getSUBMITDATE())){
			helper.tvName.setText(item.getSUBMITDATE());
		}

		if (!TextUtils.isEmpty(item.getPAPERID())){
			helper.tvNo.setText(item.getPAPERID());
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



