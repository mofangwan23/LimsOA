package cn.flyrise.feep.particular.repository;

import cn.flyrise.android.protocol.entity.AddressBookRequest;
import cn.flyrise.android.protocol.entity.AddressBookResponse;
import cn.flyrise.android.protocol.entity.CollaborationDetailsRequest;
import cn.flyrise.android.protocol.entity.CollaborationDetailsResponse;
import cn.flyrise.android.protocol.entity.MeetingInfoResponse;
import cn.flyrise.android.protocol.entity.MeetingRequest;
import cn.flyrise.android.protocol.entity.NewsDetailsRequest;
import cn.flyrise.android.protocol.entity.NewsDetailsResponse;
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailRequest;
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.callback.Callback;
import cn.flyrise.feep.particular.ParticularActivity;

/**
 * @author ZYP
 * @since 2016-10-20 11:07
 * 数据仓库，负责 {@link ParticularActivity} 界面所需数据的获取。
 */
public class ParticularRepository {

    /**
     * 请求新闻、公告详情信息
     */
    public AsyncJob<NewsDetailsResponse> fetchNewsDetail(final String newsId, final int requestType, final String messageId) {
        return new AsyncJob<NewsDetailsResponse>() {
            @Override public void start(Callback<NewsDetailsResponse> callback) {
                NewsDetailsRequest newsDetailsRequest = new NewsDetailsRequest(newsId, requestType, messageId);
                FEHttpClient.getInstance().post(newsDetailsRequest, callback);
            }
        };
    }

    /**
     * 请求会议详情信息
     */
    public AsyncJob<MeetingInfoResponse> fetchMeetingDetail(final String meetingId, final String messageId) {
        return new AsyncJob<MeetingInfoResponse>() {
            @Override public void start(Callback<MeetingInfoResponse> callback) {
                MeetingRequest request = new MeetingRequest();
                request.setMsgId(messageId);
                request.setMeetingId(meetingId);
                request.setRequestType("9");
                FEHttpClient.getInstance().post(request, callback);
            }
        };
    }

    /**
     * 请求协同详情信息
     */
    public AsyncJob<CollaborationDetailsResponse> fetchCollaborationDetail(final String collaborationId,
                                                                           final int requestType, final String messageId) {
        return new AsyncJob<CollaborationDetailsResponse>() {
            @Override public void start(Callback<CollaborationDetailsResponse> callback) {
                CollaborationDetailsRequest detailsRequest = new CollaborationDetailsRequest();
                detailsRequest.setId(collaborationId);
                detailsRequest.setRequestType(requestType);
                detailsRequest.setMsgId(messageId);
                FEHttpClient.getInstance().post(detailsRequest, callback);
            }
        };
    }

    public AsyncJob<String> fetchCollaborationDetailString(final String collaborationId,
                                                           final int requestType, final String messageId) {
        return new AsyncJob<String>() {
            @Override public void start(Callback<String> callback) {
                CollaborationDetailsRequest detailsRequest = new CollaborationDetailsRequest();
                detailsRequest.setId(collaborationId);
                detailsRequest.setRequestType(requestType);
                detailsRequest.setMsgId(messageId);
                FEHttpClient.getInstance().post(detailsRequest, callback);
            }
        };
    }

    /**
     * 请求工作计划详情
     */
    public AsyncJob<WorkPlanDetailResponse> fetchWorkPlanDetail(final String planId, final String messageId, final String relatedUserId) {
        return new AsyncJob<WorkPlanDetailResponse>() {
            @Override public void start(Callback<WorkPlanDetailResponse> callback) {
                WorkPlanDetailRequest workplanrequest = new WorkPlanDetailRequest();
                workplanrequest.setMsgId(messageId);
                workplanrequest.setId(planId);
                workplanrequest.setRelatedUserID(relatedUserId);
                FEHttpClient.getInstance().post(workplanrequest, callback);
            }
        };
    }

    /**
     * 获取详细的 Html 内容。
     */
    public AsyncJob<String> fetchDetailContent(final String url) {
        return new AsyncJob<String>() {
            @Override public void start(Callback<String> callback) {
                FEHttpClient.getInstance().post(url, null, callback);
            }
        };
    }

    /**
     * 获取用户详细信息
     */
    public AsyncJob<AddressBookResponse> fetchUserDetailInfo(final String userId) {
        return new AsyncJob<AddressBookResponse>() {
            @Override public void start(Callback<AddressBookResponse> callback) {
                AddressBookRequest addressBookRequest = new AddressBookRequest();
                addressBookRequest.setParentItemType(AddressBookType.Staff);
                addressBookRequest.setDataSourceType(AddressBookType.Staff);
                addressBookRequest.setPerPageNums("1");
                addressBookRequest.setPage("1");
                addressBookRequest.setSearchUserID(userId);
                FEHttpClient.getInstance().post(addressBookRequest, callback);
            }
        };
    }
}
