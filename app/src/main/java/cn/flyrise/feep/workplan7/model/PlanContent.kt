package cn.flyrise.feep.workplan7.model

import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import java.util.*

/**
 * author : klc
 * Msg :
 */
class PlanContent {

    var planID: String? = ""
    var type: Int? = null
    var startTime: Calendar? = null
    var endTime: Calendar? = null
    var title: String? = ""
    var content: String? = ""
    var receiver: List<AddressBook>? = null
    var cc: List<AddressBook>? = null
    var notifier: List<AddressBook>? = null
    var attachments: ArrayList<Attachment>? = null
    var attachmentGuid: String? = ""
    var originalAttachment: List<Attachment>? = null
    var isCreate: Boolean? = null
    var userId: String? = ""
}