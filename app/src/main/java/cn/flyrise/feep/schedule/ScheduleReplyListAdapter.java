package cn.flyrise.feep.schedule;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.particular.views.RelativeElegantAdapter;
import cn.flyrise.feep.schedule.model.ScheduleReply;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2018/3/26.
 */

public class ScheduleReplyListAdapter extends RelativeElegantAdapter<ScheduleReply> {

	private String host;
	private ScheduleReplyClickListener clickListener;


	public ScheduleReplyListAdapter(Context context, int layoutId,
			List<ScheduleReply> data) {
		super(context, layoutId, data);
		host = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void setClickListener(ScheduleReplyClickListener clickListener) {
		this.clickListener = clickListener;
	}

	@Override
	public void initItemViews(View view, int position, ScheduleReply item) {
		ScheduleReply reply = mData.get(position);
		ImageView ivHead = view.findViewById(R.id.ivAvatar);
		TextView tvUserName = view.findViewById(R.id.tvUserName);
		TextView tvTime = view.findViewById(R.id.tvReplyTime);
		TextView tvContent = view.findViewById(R.id.etReplyContent);
		TextView tvEdit = view.findViewById(R.id.tvEdit);
		TextView tvDelete = view.findViewById(R.id.tvDelete);

		setUserAvatar(ivHead, reply.getUserId());
		tvUserName.setText(reply.getUserName());
		tvTime.setText((DateUtil.formatTime(Long.valueOf(reply.getTime()), "yyyy-MM-dd HH:mm")));
		if (reply.getReplyTime().equals(reply.getLastUpdateTime())) {
			tvContent.setText(reply.getReplyContent());
		}
		else {
			StringBuilder content = new StringBuilder();
			content.append(reply.getReplyContent());
			content.append("（更新于");
			content.append(DateUtil.formatTime(Long.valueOf(reply.getTime()), "yyyy-MM-dd HH:mm"));
			content.append("）");
			tvContent.setText(content.toString());
		}

		if (reply.getUserId().equals(CoreZygote.getLoginUserServices().getUserId())) {
			tvEdit.setVisibility(View.VISIBLE);
			tvDelete.setVisibility(View.VISIBLE);
		}
		else {
			tvEdit.setVisibility(View.GONE);
			tvDelete.setVisibility(View.GONE);
		}
		tvEdit.setOnClickListener(v -> clickListener.onEditClick(reply.getReplyId(), reply.getReplyContent()));
		tvDelete.setOnClickListener(v -> clickListener.onDeleteClick(reply.getReplyId()));

	}

	interface ScheduleReplyClickListener {

		void onDeleteClick(String replyId);

		void onEditClick(String replyId, String content);
	}

	private void setUserAvatar(ImageView imageView, String userId) {
		if (TextUtils.isEmpty(userId) || userId.equals("0") || userId.equals("1")) {
			imageView.setImageResource(R.drawable.administrator_icon);
		}
		else {
			CoreZygote.getAddressBookServices().queryUserDetail(userId)
					.subscribe(addressBook -> {
						if (addressBook == null) {
							imageView.setImageResource(R.drawable.administrator_icon);
						}
						else {
							FEImageLoader.load(mContext, imageView, host + addressBook.imageHref, userId, addressBook.name);
						}
					}, error -> {
						imageView.setImageResource(R.drawable.administrator_icon);
					});
		}
	}
}
