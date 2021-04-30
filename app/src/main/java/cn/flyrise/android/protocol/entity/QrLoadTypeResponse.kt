package cn.flyrise.android.protocol.entity

import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.qrcode.model.QrLoginType

/**
 * author : klc
 * data on 2018/6/5 11:43
 * Msg : 二维码扫码登录类型
 */
class QrLoadTypeResponse : ResponseContent() {

    var data: QrLoginType? = null

}