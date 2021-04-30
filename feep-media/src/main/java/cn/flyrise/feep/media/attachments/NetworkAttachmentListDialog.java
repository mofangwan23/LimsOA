package cn.flyrise.feep.media.attachments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.attachments.listener.IAttachmentItemClickInterceptor;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-08 14:20
 */
public class NetworkAttachmentListDialog extends DialogFragment {

	private List<NetworkAttachment> mAttachments;
	private IAttachmentItemClickInterceptor mItemHandleInterceptor;

	public static NetworkAttachmentListDialog newInstance(List<NetworkAttachment> attachments
			, IAttachmentItemClickInterceptor itemHandeInterceptor) {
		NetworkAttachmentListDialog instance = new NetworkAttachmentListDialog();
		instance.mAttachments = attachments;
		instance.mItemHandleInterceptor = itemHandeInterceptor;
		return instance;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View contentView = inflater.inflate(R.layout.ms_fragment_network_dialog_attachment_list, container, false);
		Fragment fragment = NetworkAttachmentListFragment.newInstance(mAttachments, mItemHandleInterceptor);
		getChildFragmentManager().beginTransaction()
				.add(R.id.msLayoutAttachments, fragment)
				.show(fragment)
				.commit();
		return contentView;
	}
}
