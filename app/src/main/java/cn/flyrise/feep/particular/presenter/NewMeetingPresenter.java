package cn.flyrise.feep.particular.presenter;

import android.content.Context;
import android.content.Intent;
import cn.flyrise.android.protocol.entity.MeetingInfoResponse;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.meeting7.ui.MeetingDetailActivity;
import cn.flyrise.feep.particular.repository.ParticularRepository;

/**
 * @author ZYP
 * @since 2016-10-24 16:50
 */
public class NewMeetingPresenter {

	private MeetingInfoResponse mResponse;
	private ParticularRepository mParticularRepository;
	private String businessId;
	private String messageId;
	private Context mContext;

	public NewMeetingPresenter(Context context, String businessId, String messageId) {
		this.businessId = businessId;
		this.messageId = messageId;
		this.mContext = context;
	}

	public void start() {
		if (mParticularRepository == null) {
			mParticularRepository = new ParticularRepository();
		}
		mParticularRepository
				.fetchMeetingDetail(businessId, messageId)
				.start(new ResponseCallback<MeetingInfoResponse>() {
					@Override
					public void onCompleted(MeetingInfoResponse response) {
						if ("-95".equals(response.getErrorCode())) {
							if(response.getErrorMessage()!=null){
								FEToast.showMessage(response.getErrorMessage());
							}
							return;
						}
						mResponse = response;
						Intent intent = new Intent(mContext, MeetingDetailActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("meetingId", mResponse.getId());
						intent.putExtra("requestType", "1");
						mContext.startActivity(intent);
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {

					}
				});
	}
}
