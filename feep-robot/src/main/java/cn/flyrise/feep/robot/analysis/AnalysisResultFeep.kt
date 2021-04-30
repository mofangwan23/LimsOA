package cn.flyrise.feep.robot.analysis

import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.robot.Robot
import cn.flyrise.feep.robot.entity.FeepOperationEntry
import cn.flyrise.feep.robot.entity.SlotParsenr
import org.json.JSONException
import org.json.JSONObject

/**
 * 新建：陈冕;
 * 日期： 2017-12-28-17:13.
 * 关于oa操作的解析
 */

object AnalysisResultFeep {

    @Throws(JSONException::class)
    fun analysis(slots: List<SlotParsenr>): FeepOperationEntry {
        val feepEntity = FeepOperationEntry()
        slots.forEach {
            when {
                isFeOaOperation(it.name) -> feepEntity.operationType = it.normValue
                isFeOaMessage(it.name) -> feepEntity.messageType = it.normValue
                isPersonName(it.name) -> feepEntity.userName = getUserName(it.value)
                isTime(it.name) -> feepEntity.dateTime = getDateTime(it.normValue)
                isWildcard(it.name) -> feepEntity.wildcard = it.normValue
            }
        }
        if (isDateTimeTypeAutoSearch(feepEntity)) {//存在日期类型，并且是打开操作，自动转换为搜索操作
            feepEntity.operationType = Robot.operation.searchType
        }
        return feepEntity
    }

    private fun isTime(value: String): Boolean {//日期：搜索今天、昨天、以及具体日期
        return TextUtils.equals("time", value)
    }

    private fun isPersonName(value: String): Boolean { //用户的名字
        return TextUtils.equals("userName", value)
    }

    private fun isWildcard(value: String): Boolean {//通配符 场景：搜索
        return TextUtils.equals(value, "wildcard")
    }

    private fun isDateTimeTypeAutoSearch(feepEntity: FeepOperationEntry): Boolean {
        return !TextUtils.isEmpty(feepEntity.dateTime) && TextUtils.equals(feepEntity.operationType, Robot.operation.openType)
    }

    private fun getUserName(value: String): String {//用户名动态实体 场景：针对某一用户的操作
        return if (TextUtils.equals("我", value)) CoreZygote.getLoginUserServices().userName//我为用户自己
        else value
    }

    private fun isFeOaMessage(typeName: String): Boolean {//是否为oa服务的消息类型
        return TextUtils.equals("message", typeName) || TextUtils.equals("schedule", typeName)
    }

    private fun isFeOaOperation(typeName: String): Boolean {//是否为oa服务的操作类型
        return (TextUtils.equals("operation", typeName)
                || TextUtils.equals("open", typeName)
                || TextUtils.equals("search", typeName)
                || TextUtils.equals("create", typeName)
                || TextUtils.equals("invita", typeName))
    }

    @Throws(JSONException::class)
    private fun getDateTime(json: String): String {
        val jsonObject = JSONObject(json)
        return if (jsonObject.has("datetime")) jsonObject.getString("datetime") else ""
    }

}
