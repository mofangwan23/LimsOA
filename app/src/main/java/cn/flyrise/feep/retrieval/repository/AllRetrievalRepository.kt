package cn.flyrise.feep.retrieval.repository

import cn.flyrise.feep.R
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.media.common.FileCategoryTable
import cn.flyrise.feep.retrieval.bean.*
import cn.flyrise.feep.retrieval.protocol.*
import cn.flyrise.feep.retrieval.vo.RetrievalResults
import cn.flyrise.feep.retrieval.vo.RetrievalType.*
import rx.Subscriber

/**
 * @author cm
 * @since 2018-12-22 16:29
 * 所有信息信息的检索
 */
class AllRetrievalRepository : RetrievalRepository() {

    override fun search(subscriber: Subscriber<in RetrievalResults>, keyword: String) {
        this.mKeyword = keyword
        val request = RetrievalSearchRequest.searchAll(keyword)
        FEHttpClient.getInstance().post(request, object : ResponseCallback<AllRetrievalResponse>() {
            override fun onCompleted(response: AllRetrievalResponse?) {
                val retrievals = mutableListOf<Retrieval?>()
                response?.data?.apply {
                    if (S1001.maxCount > 0) {
                        assemblyRetrieval(retrievals, TYPE_APPROVAL, S1001.results, S1001.maxCount)
                    }
                    if (S1002.maxCount > 0) {
                        assemblyRetrieval(retrievals, TYPE_FILES, S1002.results, S1002.maxCount)
                    }
                    if (S1003.maxCount > 0) {
                        assemblyRetrieval(retrievals, TYPE_SCHEDULE, S1003.results, S1003.maxCount)
                    }
                    if (S1004.maxCount > 0) {
                        assemblyRetrieval(retrievals, TYPE_PLAN, S1004.results, S1004.maxCount)
                    }
                    if (S1007.maxCount > 0) {
                        assemblyRetrieval(retrievals, TYPE_NEWS, S1007.results, S1007.maxCount)
                    }
                    if (S1008.maxCount > 0) {
                        assemblyRetrieval(retrievals, TYPE_NOTICE, S1008.results, S1008.maxCount)
                    }
                    if (S1009.maxCount > 0) {
                        assemblyRetrieval(retrievals, TYPE_MEETING, S1009.results, S1009.maxCount)
                    }
                }

                subscriber.onNext(RetrievalResults.Builder()
                        .retrievalType(type)
                        .retrievals(retrievals)
                        .create())
            }

            override fun onFailure(repositoryException: RepositoryException) {
//                FELog.e("Retrieval approval failed. Error: " + repositoryException.exception().message)
                subscriber.onNext(emptyResult())
            }
        })
    }

    private fun assemblyRetrieval(retrievals: MutableList<Retrieval?>, type: Int, results: List<Any>, maxCount: Int) {
        retrievals.add(newTag(Retrieval.VIEW_TYPE_HEADER, type, retrievalHint(type)))
        results.forEach {
            retrievals.add(when (it) {
                is DRApproval -> createRetrieval(it, type)
                is DRFile -> createRetrieval(it, type)
                is DRSchedule -> createRetrieval(it, type)
                is DRPlan -> createRetrieval(it, type)
                is DRNews -> createRetrieval(it, type)
                is DRNotice -> createRetrieval(it, type)
                is DRMeeting -> createRetrieval(it, type)
                else -> null
            })
        }

        if (maxCount >= 3) {
            retrievals.add(newTag(Retrieval.VIEW_TYPE_FOOTER, type
                    , "${mContext.getString(R.string.retrieval_repository_more)}${retrievalHint(type)}"))
        }
    }

    private fun retrievalHint(type: Int) = when (type) {
        TYPE_APPROVAL -> mContext.getString(R.string.retrieval_repository_approval)
        TYPE_FILES -> mContext.getString(R.string.retrieval_repository_file)
        TYPE_SCHEDULE -> mContext.getString(R.string.retrieval_repository_schedule)
        TYPE_PLAN -> mContext.getString(R.string.retrieval_repository_plan)
        TYPE_NEWS -> mContext.getString(R.string.retrieval_repository_new)
        TYPE_NOTICE -> mContext.getString(R.string.retrieval_repository_notice)
        TYPE_MEETING -> mContext.getString(R.string.retrieval_repository_meeting)
        else -> ""
    }

    private fun createRetrieval(approval: DRApproval, type: Int): ApprovalRetrieval {
        val retrieval = ApprovalRetrieval()
        retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT
        retrieval.retrievalType = type
        retrieval.content = fontDeepen(approval.title, mKeyword)
        retrieval.extra = approval.important + " " + approval.sendTime

        retrieval.businessId = approval.id
        retrieval.userId = approval.userId
        retrieval.username = approval.username
        retrieval.type = approval.type
        return retrieval
    }

    private fun createRetrieval(file: DRFile, type: Int): FileRetrieval {
        val retrieval = FileRetrieval()
        retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT
        retrieval.retrievalType = type
        retrieval.content = fontDeepen(file.title, mKeyword)
        retrieval.extra = fontDeepen("来自 " + file.remark, mKeyword)

        retrieval.businessId = file.id
        retrieval.userId = file.userId
        retrieval.username = file.username

        retrieval.url = FEHttpClient.getInstance().host + FEHttpClient.KNOWLEDGE_DOWNLOAD_PATH + file.id
        retrieval.filename = file.remark.substring(file.remark.lastIndexOf("/") + 1)
        retrieval.iconRes = FileCategoryTable.getIcon(FileCategoryTable.getType(file.fileattr))
        return retrieval
    }

    private fun createRetrieval(meeting: DRMeeting, type: Int): MeetingRetrieval {
        val retrieval = MeetingRetrieval()
        retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT
        retrieval.retrievalType = type
        retrieval.content = fontDeepen(meeting.title, mKeyword)
        retrieval.extra = fontDeepen("来自 " + meeting.username, mKeyword)

        retrieval.businessId = meeting.id
        retrieval.userId = meeting.userId
        retrieval.username = meeting.username
        return retrieval
    }

    private fun createRetrieval(news: DRNews, type: Int): NewsRetrieval {
        val retrieval = NewsRetrieval()
        retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT
        retrieval.retrievalType = type
        retrieval.content = fontDeepen(news.title, mKeyword)
        val sendTime = news.sendTime.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        retrieval.extra = fontDeepen(news.category + " " + sendTime, mKeyword)

        retrieval.businessId = news.id
        retrieval.userId = news.userId
        retrieval.username = news.userName
        return retrieval
    }

    private fun createRetrieval(notice: DRNotice, type: Int): NoticeRetrieval {
        val retrieval = NoticeRetrieval()
        retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT
        retrieval.retrievalType = type
        retrieval.content = fontDeepen(notice.title, mKeyword)
        val sendTime = notice.sendTime.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        retrieval.extra = fontDeepen(notice.category + " " + sendTime, mKeyword)

        retrieval.businessId = notice.id
        retrieval.userId = notice.userId
        retrieval.username = notice.userName
        return retrieval
    }

    private fun createRetrieval(plan: DRPlan, type: Int): PlanRetrieval {
        val retrieval = PlanRetrieval()
        retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT
        retrieval.retrievalType = type
        retrieval.content = fontDeepen(plan.title, mKeyword)
        retrieval.extra = plan.content

        retrieval.businessId = plan.id
        retrieval.userId = plan.userId
        retrieval.username = plan.username
        return retrieval
    }

    private fun createRetrieval(schedule: DRSchedule, type: Int): ScheduleRetrieval {
        val retrieval = ScheduleRetrieval()
        retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT
        retrieval.retrievalType = type
        retrieval.content = fontDeepen(schedule.title, mKeyword)
        retrieval.extra = fontDeepen(schedule.content, mKeyword)

        retrieval.userId = schedule.userId
        retrieval.scheduleId = schedule.id
        retrieval.meetingId = schedule.meetingId
        retrieval.eventSource = schedule.eventSource
        retrieval.eventSourceId = schedule.eventSourceId
        return retrieval
    }

    private fun newTag(viewType: Int, messageType: Int, content: String): Retrieval {
        val retrieval = newRetrieval()
        retrieval.retrievalType = messageType
        retrieval.viewType = viewType
        retrieval.content = content
        return retrieval
    }

    override fun getType(): Int {
        return TYPE_ALL_MESSAGE
    }

    override fun newRetrieval(): Retrieval {
        return Retrieval()
    }
}
