package cn.flyrise.feep.media.attachments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.core.base.views.adapter.DividerItemDecoration;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.adapter.LocalAttachmentListAdapter;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.listener.ILocalAttachmentItemHandleListener;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-08 15:52
 * 本地附件展示界面，支持附件的查看、删除
 * 【这个傻逼玩意只用先做，具体事件操作，交由使用这个傻逼玩意的傻逼玩意来做】
 */
public class LocalAttachmentListFragment extends Fragment {

	// ScrollView 中嵌套是用的问题
	private boolean nestedScrollingEnabled;
	private List<Attachment> mAttachments;
	private ILocalAttachmentItemHandleListener mHandleListener;

	private RecyclerView mRecyclerView;
	private LocalAttachmentListAdapter mAdapter;

	public static LocalAttachmentListFragment newInstance(boolean nestedScrollingEnabled,
			List<Attachment> attachments, ILocalAttachmentItemHandleListener listener) {
		LocalAttachmentListFragment instance = new LocalAttachmentListFragment();
		instance.mAttachments = attachments;
		instance.mHandleListener = listener;
		instance.nestedScrollingEnabled = nestedScrollingEnabled;
		instance.mAdapter = new LocalAttachmentListAdapter();
		return instance;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.ms_fragment_simple_attachment_list, container, false);
		mRecyclerView = (RecyclerView) contentView.findViewById(R.id.msAttachmentList);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setItemAnimator(null);

		if (nestedScrollingEnabled) {
			mRecyclerView.setNestedScrollingEnabled(false);
		}

		Drawable drawable = getResources().getDrawable(R.drawable.ms_divider_album_item);
		DividerItemDecoration dividerDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
		dividerDecoration.setDrawable(drawable);
		mRecyclerView.addItemDecoration(dividerDecoration);

		mRecyclerView.setAdapter(mAdapter);
		mAdapter.setOnLocalAttachmentItemHandleListener(mHandleListener);
		return contentView;
	}

	public void clearToDeleteAttachments() {
		mAdapter.clearToDeleteAttachments();
	}

	public List<Attachment> getToDeleteAttachments() {
		return mAdapter.getToDeleteAttachments();
	}

	public int getToDeleteAttachmentSize() {
		return mAdapter.getToDeleteAttachmentSize();
	}

	public void addAttachmentToDelete(int position, Attachment attachment) {
		mAdapter.addAttachmentToDelete(position, attachment);
	}

	public void notifyAllAttachmentDeleteState(boolean isDeleteAll) {
		mAdapter.notifyAllAttachmentDeleteState(isDeleteAll);
	}

	public void setAttachments(List<Attachment> attachments) {
		mAdapter.setAttachments(attachments);
	}

	public void setEditMode(boolean isEditMode) {
		mAdapter.setEditMode(isEditMode);
	}

	public boolean isEditMode() {
		return mAdapter.isEditMode();
	}

	public LocalAttachmentListAdapter getAdapter() {
		return mAdapter;
	}

}
