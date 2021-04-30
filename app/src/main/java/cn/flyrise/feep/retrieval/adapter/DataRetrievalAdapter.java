package cn.flyrise.feep.retrieval.adapter;

import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_APPROVAL;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_CHAT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_CONTACT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_FILES;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_GROUP;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_MEETING;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_NEWS;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_NOTICE;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_PLAN;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_SCHEDULE;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.retrieval.IRetrievalServices;
import cn.flyrise.feep.retrieval.bean.BusinessRetrieval;
import cn.flyrise.feep.retrieval.bean.ChatRetrieval;
import cn.flyrise.feep.retrieval.bean.ContactRetrieval;
import cn.flyrise.feep.retrieval.bean.GroupRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.bean.ScheduleRetrieval;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-04-28 14:52
 */
public class DataRetrievalAdapter extends RecyclerView.Adapter<ViewHolder> {

	private final String mHost;
	private final Context mContext;
	private List<? extends Retrieval> mDataSources;
	private IRetrievalServices mRetrievalServices;
	private OnRetrievalItemClickListener mClickListener;

	public DataRetrievalAdapter(Context context) {
		this.mContext = context;
		this.mDataSources = new ArrayList<>();
		this.mHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void setOnRetrievalItemClickListener(OnRetrievalItemClickListener listener) {
		this.mClickListener = listener;
	}

	public void setRetrievalServices(IRetrievalServices retrievalServices) {
		this.mRetrievalServices = retrievalServices;
	}

	public void setDataSources(List<? extends Retrieval> retrievals) {
		this.mDataSources = retrievals;
		this.notifyDataSetChanged();
	}

	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ViewHolder viewHolder = null;
		if (viewType == Retrieval.VIEW_TYPE_HEADER) {           // header
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dr_item_retrieval_header, parent, false);
			viewHolder = new RetrievalHeadHolder(itemView);
		}
		else if (viewType == Retrieval.VIEW_TYPE_FOOTER) {      // footer
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dr_item_retrieval_footer, parent, false);
			viewHolder = new RetrievalFootHolder(itemView);
		}
		else {                                                  // content
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dr_item_retrieval_content, parent, false);
			viewHolder = new RetrievalContentHolder(itemView);
		}
		return viewHolder;
	}

	@Override public void onBindViewHolder(ViewHolder holder, int position) {
		Retrieval retrieval = mDataSources.get(position);
		if (retrieval.viewType == Retrieval.VIEW_TYPE_HEADER) {                // Header
			RetrievalHeadHolder vHolder = (RetrievalHeadHolder) holder;
			vHolder.mTvHeader.setText(retrieval.content);
			vHolder.itemView.setPadding(0, position > 0 ? PixelUtil.dipToPx(12) : 0, 0, 0);
			return;
		}

		if (retrieval.viewType == Retrieval.VIEW_TYPE_FOOTER) {                // Footer
			RetrievalFootHolder vHolder = (RetrievalFootHolder) holder;
			vHolder.mTvSearchMore.setText(retrieval.content);
			vHolder.mLayoutSearchMore.setOnClickListener(view -> {             // 搜索更多的点击事件
				if (mClickListener != null) {
					mClickListener.onFooterMoreClick(retrieval.retrievalType);
				}
			});
			return;
		}

		// title、subTitle
		RetrievalContentHolder vHolder = (RetrievalContentHolder) holder;      // Content
		if (!TextUtils.isEmpty(retrieval.content)) vHolder.tvTitle.setText(Html.fromHtml(retrieval.content));  // 设置标题
		if (TextUtils.isEmpty(retrieval.extra)) {                              // 设置子标题，如果有的话
			vHolder.tvSubTitle.setVisibility(View.GONE);
		}
		else {
			vHolder.tvSubTitle.setText(retrieval.retrievalType == TYPE_CHAT && mRetrievalServices != null
					? mRetrievalServices.formatTextFromEmoticon(mContext, retrieval.extra)
					: Html.fromHtml(retrieval.extra));
			vHolder.tvSubTitle.setVisibility(View.VISIBLE);
		}

		// icon
		if (retrieval.retrievalType == TYPE_CONTACT) {              // 通讯录
			ContactRetrieval contact = (ContactRetrieval) retrieval;
			FEImageLoader.load(mContext, vHolder.ivIcon, contact.imageHref, contact.userId, contact.username);
		}
		else if (retrieval.retrievalType == TYPE_CHAT) {            // 聊天记录
			fillChatUserIcon(vHolder.ivIcon, (ChatRetrieval) retrieval);
		}
		else if (retrieval.retrievalType == TYPE_GROUP) {
			GroupRetrieval group = (GroupRetrieval) retrieval;
			FEImageLoader.load(mContext, vHolder.ivIcon, group.imageRes);
		}
		else if (retrieval.retrievalType == TYPE_SCHEDULE) {
			ScheduleRetrieval schedule = (ScheduleRetrieval) retrieval;
			fillUserIcon(vHolder.ivIcon, schedule.userId);
		}
		else if (retrieval.retrievalType == TYPE_NEWS
				|| retrieval.retrievalType == TYPE_NOTICE
				|| retrieval.retrievalType == TYPE_PLAN
				|| retrieval.retrievalType == TYPE_APPROVAL
				|| retrieval.retrievalType == TYPE_MEETING
				|| retrieval.retrievalType == TYPE_FILES) {            // 新闻
			fillUserIcon(vHolder.ivIcon, ((BusinessRetrieval) retrieval).userId);
		}

		vHolder.layoutContent.setOnClickListener(view -> {          // 设置点击事件
			if (mClickListener != null) {
				mClickListener.onRetrievalClick(retrieval);
			}
		});
	}

	@Override public int getItemViewType(int position) {
		Retrieval retrieval = mDataSources.get(position);
		return retrieval.viewType;
	}

	@Override public int getItemCount() {
		return CommonUtil.isEmptyList(mDataSources) ? 0 : mDataSources.size();
	}

	private void fillChatUserIcon(ImageView imageView, ChatRetrieval chatRetrieval) {
		if (chatRetrieval.isGroup) {
			FEImageLoader.load(mContext, imageView, chatRetrieval.imageRes);
		}
		else {
			CoreZygote.getAddressBookServices().queryUserDetail(chatRetrieval.conversationId)
					.subscribe(addressBook -> {
						if (addressBook == null) {
							FEImageLoader.load(mContext, imageView, chatRetrieval.imageRes);
						}
						else {
							FEImageLoader.load(mContext, imageView,
									CoreZygote.getLoginUserServices().getServerAddress() + addressBook.imageHref,
									addressBook.userId, addressBook.name);
						}
					}, error -> {
						FEImageLoader.load(mContext, imageView, chatRetrieval.imageRes);
					});
		}
	}

	private void fillUserIcon(ImageView imageView, String userId) {
		CoreZygote.getAddressBookServices().queryUserDetail(userId)
				.subscribe(addressBook -> {
					if (addressBook == null) {
						FEImageLoader.load(mContext, imageView, R.drawable.administrator_icon);
					}
					else {
						FEImageLoader.load(mContext, imageView, mHost + addressBook.imageHref, addressBook.userId, addressBook.name);
					}
				}, error -> {
					FEImageLoader.load(mContext, imageView, R.drawable.administrator_icon);
				});
	}

	public interface OnRetrievalItemClickListener {

		void onFooterMoreClick(int retrievalType);

		void onRetrievalClick(Retrieval retrieval);
	}
}
