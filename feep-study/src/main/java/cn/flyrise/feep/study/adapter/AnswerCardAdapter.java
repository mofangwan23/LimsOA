package cn.flyrise.feep.study.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.flyrise.feep.core.dialog.CustomDialog;
import cn.flyrise.feep.study.adapter.AnswerCardAdapter.ViewHolder;
import cn.flyrise.feep.study.entity.GetQuestionResponse;
import cn.flyrise.feep.study.entity.GetQuestionResponse.DatalistBean;
import cn.flyrise.study.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.List;

public class AnswerCardAdapter extends BaseQuickAdapter<GetQuestionResponse.DatalistBean, ViewHolder> {

	public AnswerCardAdapter(int layoutResId,List<GetQuestionResponse.DatalistBean> examList) {
		super(layoutResId, examList);
	}

	@Override protected void convert(ViewHolder helper, DatalistBean item) {
         if (TextUtils.isEmpty(item.getUserAnswer())){
         	helper.tvNo.setBackgroundResource(R.drawable.stu_shape_bg_white);
         }else {
	         helper.tvNo.setBackgroundResource(R.drawable.stu_shape_bg_blue);
         }
         helper.tvNo.setText(String.valueOf(helper.getAdapterPosition() + 1));

	}


	class ViewHolder extends BaseViewHolder {

		RelativeLayout relativeLayout;
		TextView tvNo;

		public ViewHolder(View view) {
          super(view);
            relativeLayout = view.findViewById(R.id.rlAnswerView);
			tvNo = view.findViewById(R.id.tvQuetionIndex);
		}
	}
}



