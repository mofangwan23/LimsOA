package cn.flyrise.feep.meeting7.repository

import android.text.TextUtils
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.meeting7.protocol.*
import cn.flyrise.feep.meeting7.ui.bean.MeetingRoomData
import cn.flyrise.feep.meeting7.ui.bean.MeetingRoomDetailData
import cn.flyrise.feep.meeting7.ui.bean.MeetingType
import cn.flyrise.feep.meeting7.ui.bean.PromptTime
import rx.Observable
import rx.Subscriber
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-20 11:51
 */
class MeetingDataRepository {

    /**
     * 请求会议列表
     */
    fun requestMeetingList(request: MeetingListRequest): Observable<MeetingListResponse> {
        return Observable
                .create { f: Subscriber<in MeetingListResponse> ->
                    FEHttpClient.getInstance().post(request, object : ResponseCallback<MeetingListResponse>() {
                        override fun onCompleted(response: MeetingListResponse?) {
                            if (response == null || !TextUtils.equals(response.errorCode, "0")) {
                                f.onError(RuntimeException("Fetch meeting list faliure."))
                            } else {
                                f.onNext(response)
                            }
                        }

                        override fun onFailure(repository: RepositoryException?) {
                            f.onError(RuntimeException(repository?.errorMessage()))
                        }
                    })
                }
    }

    /**
     * 请求会议室详情
     */
    fun requestMeetingDetail(meetingId: String, type: String): Observable<MeetingDetailResponse> {
        return Observable.create { f: Subscriber<in MeetingDetailResponse> ->
            val request = MeetingDetailRequest()
            request.meetingId = meetingId
            request.type = type
            FEHttpClient.getInstance().post(request, object : ResponseCallback<MeetingDetailResponse>() {
                override fun onCompleted(t: MeetingDetailResponse?) {
                    if (t == null || !TextUtils.equals(t.errorCode, "0")) {
                        f.onError(RuntimeException("Fetch meeting detail failure."))
                    } else {
                        f.onNext(t)
                    }
                    f.onCompleted()
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    f.onError(RuntimeException(repositoryException?.errorMessage()))
                    f.onCompleted()
                }
            })
        }
    }

    /**
     * 请求会议室列表
     */
    fun requestMeetingRoomList(time: String, page: Int): Observable<MeetingRoomData> {
        return Observable.create { f: Subscriber<in MeetingRoomData> ->
            val request = MeetingRoomListRequest.newInstance(time, page)
            FEHttpClient.getInstance().post(request, object : ResponseCallback<MeetingRoomListResponse>() {
                override fun onCompleted(response: MeetingRoomListResponse?) {
                    if (response == null || !TextUtils.equals(response.errorCode, "0")) {
                        f.onError(RuntimeException("Fetch meeting room list failure."))
                    } else {
                        f.onNext(response.data)
                    }
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    f.onError(RuntimeException(repositoryException?.errorMessage()))
                }
            })
        }
    }

    /**
     * 请求会议室详情列表
     */
    fun requestMeetingRoomDetail(roomId: String): Observable<MeetingRoomDetailData> {
        return Observable.create { f: Subscriber<in MeetingRoomDetailData> ->
            val request = MeetingUsageRequest.requestDetail(roomId)
            FEHttpClient.getInstance().post(request, object : ResponseCallback<MeetingRoomDetailResponse>() {
                override fun onCompleted(response: MeetingRoomDetailResponse?) {
                    if (response == null || !TextUtils.equals(response.errorCode, "0")) {
                        f.onNext(null)
                    } else {
                        f.onNext(response.data)
                    }
                    f.onCompleted()
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    f.onNext(null)
                    f.onCompleted()
                }
            })
        }
    }

    /**
     * 更改会议状态
     * "1"：参加
     * "2"：不参加
     */
    fun changeMeetingStatus(meetingId: String, joinId: String, content: String, status: String): Observable<Int> {
        val replyRequest = MeetingReplyRequest()
        replyRequest.id = meetingId
        replyRequest.meetingId = joinId
        replyRequest.meetingStatus = status
        replyRequest.meetingContent = content
        replyRequest.meetingAnnex = UUID.randomUUID().toString()

        return Observable.create { f: Subscriber<in Int> ->
            FEHttpClient.getInstance().post(replyRequest, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(response: ResponseContent?) {
                    if (response != null && !TextUtils.equals(response.errorCode, "0")) {
                        f.onError(RuntimeException("change meeting state failure."))
                    } else {
                        f.onNext(200)
                    }
                    f.onCompleted()
                }

                override fun onFailure(exception: RepositoryException?) {
                    f.onError(exception?.exception())
                    f.onCompleted()
                }
            })
        }
    }

    /**
     * 获取会议类型
     */
    fun requestMeetingTypes(): Observable<List<MeetingType>> {
        return Observable.create { f: Subscriber<in List<MeetingType>> ->
            val request = MeetingTypeRequest()
            FEHttpClient.getInstance().post(request, object : ResponseCallback<MeetingTypeResponse>() {
                override fun onCompleted(t: MeetingTypeResponse?) {
                    f.onNext(t?.data)
                    f.onCompleted()
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    f.onError(repositoryException?.exception())
                    f.onCompleted()
                }
            })
        }
    }

    /**
     * 提醒一下会议未办理人员
     */
    fun promptUntreatedUsers(meetingId: String): Observable<Int> {
        return Observable.create { f: Subscriber<in Int> ->
            val request = MeetingPromptRequest.newInstance(meetingId)
            FEHttpClient.getInstance().post(request, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(t: ResponseContent?) {
                    if (t != null && TextUtils.equals(t!!.errorCode, "0")) {
                        f.onNext(200)
                    } else {
                        f.onError(RuntimeException("prompt untreated user failure."))
                    }
                    f.onCompleted()
                }

                override fun onFailure(exception: RepositoryException?) {
                    f.onError(exception?.exception())
                    f.onCompleted()
                }

            })
        }
    }

    /**
     * 取消会议
     */
    fun cancelMeeting(meetingId: String, content: String): Observable<Int> {
        return Observable.unsafeCreate {
            val request = MeetingCancelRequest.newInstance(meetingId, content)
            FEHttpClient.getInstance().post(request, object : ResponseCallback<ResponseContent>() {
                override fun onCompleted(t: ResponseContent?) {
                    if (t != null && TextUtils.equals(t.errorCode, "0")) {
                        it.onNext(200)
                    } else {
                        it.onError(RuntimeException("cancel meeting failure."))
                    }
                    it.onCompleted()
                }

                override fun onFailure(exception: RepositoryException?) {
                    it.onError(exception?.exception())
                    it.onCompleted()
                }
            })
        }
    }

    /**
     * 获取会议提醒时间列表
     */
    fun requestPromptTimes(): Observable<List<PromptTime>> {
        return Observable.create { f: Subscriber<in List<PromptTime>> ->
            val request = MeetingPromptTimeRequest()
            FEHttpClient.getInstance().post(request, object : ResponseCallback<MeetingPromptTimeResponse>() {

                override fun onCompleted(t: MeetingPromptTimeResponse?) {
                    f.onNext(t?.promptTimes)
                    f.onCompleted()
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    f.onError(repositoryException?.exception())
                    f.onCompleted()
                }
            })
        }
    }


}