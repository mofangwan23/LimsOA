package cn.flyrise.feep.study.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.study.adapter.AnswerResultAdapter.ViewHolder;
import cn.flyrise.feep.study.entity.GetQuestionResponse;
import cn.flyrise.feep.study.entity.GetQuestionResponse.DatalistBean;
import cn.flyrise.study.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.List;

public class AnswerResultAdapter extends BaseQuickAdapter<GetQuestionResponse.DatalistBean, ViewHolder> {

	private String right = "1";
	private String wrong = "-1";
	private String notPiGai = "0";

	public AnswerResultAdapter(int layoutResId,List<GetQuestionResponse.DatalistBean> examList) {
		super(layoutResId, examList);
	}

	@Override protected void convert(ViewHolder helper, DatalistBean item) {
		if (TextUtils.equals(item.getRW(),right)){
			helper.tvNo.setBackgroundResource(R.drawable.stu_shape_bg_green);
		}else if(TextUtils.equals(item.getRW(),wrong)){
			helper.tvNo.setBackgroundResource(R.drawable.stu_shape_bg_red);
		}else {
			helper.tvNo.setBackgroundResource(R.drawable.stu_shape_bg_white);
		}
		helper.tvNo.setText(String.valueOf(helper.getAdapterPosition() + 1));
		helper.addOnClickListener(R.id.rlAnswerView);
	}


	class ViewHolder extends BaseViewHolder{
		RelativeLayout relativeLayout;
		TextView tvNo;

		public ViewHolder(View view) {
			super(view);
			relativeLayout = view.findViewById(R.id.rlAnswerView);
			tvNo = view.findViewById(R.id.tvQuetionIndex);
		}
	}
}



