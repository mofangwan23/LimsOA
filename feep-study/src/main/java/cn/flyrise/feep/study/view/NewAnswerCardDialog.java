package cn.flyrise.feep.study.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import cn.flyrise.feep.study.adapter.AnswerCardAdapter;
import cn.flyrise.feep.study.entity.GetQuestionRequest;
import cn.flyrise.feep.study.entity.GetQuestionResponse;
import cn.flyrise.feep.study.entity.GetQuestionResponse.DatalistBean;
import cn.flyrise.study.R;
import java.util.List;

public class NewAnswerCardDialog extends Dialog {

	private AnswerCardAdapter adapter;

	public NewAnswerCardDialog(@NonNull Context context) {
		super(context);
	}

	public NewAnswerCardDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}

	protected NewAnswerCardDialog(@NonNull Context context, boolean cancelable,
			@Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public NewAnswerCardDialog onCreate(Context context,List<DatalistBean> list){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		NewAnswerCardDialog dialog = new NewAnswerCardDialog(context, R.style.my_custom_dialog);
		View layout = inflater.inflate(R.layout.stu_answer_card_layout,null);
		RecyclerView recyclerView = layout.findViewById(R.id.answerCardRecyclerView);
		adapter = new AnswerCardAdapter(R.layout.stu_item_answer_view,list);
		recyclerView.setLayoutManager(new GridLayoutManager(context,5));
		recyclerView.setAdapter(adapter);
		dialog.setContentView(layout);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		Window dialogWindow = dialog.getWindow();
		if (dialogWindow != null) {
			WindowManager m = dialogWindow.getWindowManager();
			Display d = m.getDefaultDisplay();
			WindowManager.LayoutParams p = dialogWindow.getAttributes();
			p.width = (int) (d.getWidth() * 0.90);
			p.gravity = Gravity.CENTER;
			dialogWindow.setAttributes(p);
		}
		return dialog;
	}


}
