package cn.flyrise.feep.robot.analysis

import android.os.Bundle
import android.text.TextUtils
import cn.flyrise.feep.robot.entity.*
import cn.flyrise.feep.robot.util.RobotFilterChar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2017-11-24-14:04.
 * AIUI反馈的数据解析
 */

object AiuiAnalysis {

    //解析回调
    fun analysis(jsonInfo: String?, bundle: Bundle?, inputType: Int?): RobotResultData? {
        try {
            val bizParamJson = JSONObject(jsonInfo)
            val data = bizParamJson.getJSONArray("data").getJSONObject(0)
            val params = data.getJSONObject("params")
            val content = data.getJSONArray("content").getJSONObject(0)

            if (!content.has("cnt_id")) {
                return null
            }
            val sub = params.optString("sub")
            if ("nlp" != sub) {
                return null
            }
            val cnt_id = content.getString("cnt_id")
            val cntJson = JSONObject(String(bundle?.getByteArray(cnt_id)!!, charset("utf-8")))
            return if (!cntJson.has("intent") || cntJson.get("intent") !is JSONObject) {
                null
            } else {
                var data = analysisFeOa(analysisIntentData(cntJson))
                data?.inputType = inputType
                return data
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return null
    }

    //解析属于oa的操作事件
    @Throws(JSONException::class)
    private fun analysisFeOa(data: RobotResultData?): RobotResultData? {
        if (data == null) return null
        val semantics = data.semantic
        if (semantics == null || semantics.size == 0) {
            return data
        }
        val semantic = semantics[0] ?: return data
        data.operationEntry = AnalysisResultFeep.analysis(semantic.slots)
        return data
    }

    //解析数据主结构
    @Throws(JSONException::class)
    private fun analysisIntentData(cntJson: JSONObject): RobotResultData? {
        val robotResultData = RobotResultData()
        val intentObject = cntJson.getJSONObject("intent") ?: return null
        robotResultData.service = if (intentObject.has("service")) intentObject.getString("service") else ""
        robotResultData.query = if (intentObject.has("query")) intentObject.getString("query") else ""
        robotResultData.text = if (intentObject.has("text")) intentObject.getString("text") else ""

        if (intentObject.has("answer") && intentObject.get("answer") is JSONObject) {
            val answer = intentObject.getJSONObject("answer")
            if (answer != null && answer.has("text")) {
                robotResultData.answerText = RobotFilterChar.filterChars(answer.getString("text"))
            }
        }
        robotResultData(robotResultData, intentObject)
        robotResultData.semantic = parsenterSemanticParsenr(intentObject)
        robotResultData.moreResults = parsenterUsedState(intentObject)
        robotResultData.weatherDatas = AnalysisResultWeather.analysis(intentObject)
        return robotResultData
    }

    //result主要有：百科
    @Throws(JSONException::class)
    private fun robotResultData(robotResultData: RobotResultData, intentObject: JSONObject?) {
        if (intentObject == null || !intentObject.has("data") || intentObject.get("data") !is JSONObject) {
            return
        }
        val dataJson = intentObject.getJSONObject("data")
        if (dataJson == null || !dataJson.has("result") || dataJson.get("result") !is JSONArray) {
            return
        }
        val results = dataJson.getJSONArray("result") ?: return
        robotResultData.results = AnalysisResultMp3.analysis(robotResultData.service, results)
        robotResultData.trainItems = AnalysisResultTrain.analysis(results)
        robotResultData.holidayItems = AnalysisResultHoliday.analysis(results)
    }

    //slots主要有：feoa的值
    @Throws(JSONException::class)
    private fun parsenterSemanticParsenr(jsonObject: JSONObject): List<SemanticParsenr> {
        val semanticParsenrs = ArrayList<SemanticParsenr>()
        if (!jsonObject.has("semantic") || jsonObject.get("semantic") !is JSONArray) {
            return semanticParsenrs
        }
        val semanticArray = jsonObject.getJSONArray("semantic")
        if (semanticArray == null || semanticArray.length() == 0) {
            return semanticParsenrs
        }
        val slotParsenrs = ArrayList<SlotParsenr>()
        var semantic: SemanticParsenr
        var `object`: JSONObject?
        var slots: JSONArray?
        var jsonObjects: JSONObject?
        var slotParsenr: SlotParsenr
        for (i in 0 until semanticArray.length()) {
            `object` = semanticArray.getJSONObject(i)
            if (`object` == null) {
                continue
            }
            semantic = SemanticParsenr()
            semantic.intent = if (`object`.has("intent")) `object`.getString("intent") else ""
            semantic.slots = slotParsenrs
            if (!`object`.has("slots") || `object`.get("slots") !is JSONArray) {
                semanticParsenrs.add(semantic)
                continue
            }
            slots = `object`.getJSONArray("slots")
            if (slots == null || slots.length() == 0) {
                continue
            }
            for (j in 0 until slots.length()) {
                if (slots.get(j) !is JSONObject) {
                    continue
                }
                jsonObjects = slots.getJSONObject(j)
                if (jsonObjects == null) {
                    continue
                }
                slotParsenr = SlotParsenr()
                slotParsenr.name = if (jsonObjects.has("name")) jsonObjects.getString("name") else ""
                slotParsenr.normValue = if (jsonObjects.has("normValue")) jsonObjects.getString("normValue") else ""
                slotParsenr.value = if (jsonObjects.has("value")) jsonObjects.getString("value") else ""
                slotParsenrs.add(slotParsenr)
                parsenterScheduleData(slotParsenr, semantic)
            }
            semanticParsenrs.add(semantic)
        }
        return semanticParsenrs
    }

    @Throws(JSONException::class)
    private fun parsenterScheduleData(slotParsenr: SlotParsenr, semanticParsenr: SemanticParsenr) {
        if (TextUtils.isEmpty(slotParsenr.name)) {
            return
        }
        if (TextUtils.equals("datetime", slotParsenr.name)) {
            semanticParsenr.timeValue = slotParsenr.value
            semanticParsenr.time = getDateTime(slotParsenr.normValue)
        } else if (TextUtils.equals("content", slotParsenr.name)) {
            semanticParsenr.content = slotParsenr.value
        } else if (TextUtils.equals("name", slotParsenr.name)) {
            semanticParsenr.name = slotParsenr.value
        }

    }

    @Throws(JSONException::class)
    private fun getDateTime(json: String): String {
        val jsonObject = JSONObject(json)
        return if (jsonObject.has("datetime")) jsonObject.getString("datetime") else ""
    }

    @Throws(JSONException::class)
    private fun parsenterUsedState(jsonObject: JSONObject): MoreResults {
        val mores = MoreResults()
        var usedState: UsedState? = null
        val answer = MoreResults.Answer()
        if (jsonObject.has("used_state") && jsonObject.get("used_state") is JSONObject) {
            val unsedState = jsonObject.getJSONObject("used_state") ?: return mores
            usedState = UsedState()
            usedState.content = if (unsedState.has("content")) unsedState.getString("content") else ""
            usedState.state = if (unsedState.has("state")) unsedState.getString("state") else ""
            usedState.datetime_time = if (unsedState.has("datetime.time")) unsedState.getString("datetime.time") else ""
            usedState.datetime_date = if (unsedState.has("datetime.date")) unsedState.getString("datetime.date") else ""
        }
        mores.mUsedState = usedState
        //在AIUI自定义的情况下返回的解析数据
        if (jsonObject.has("moreResults") && jsonObject.get("moreResults") is JSONArray && usedState == null) {
            val moreResults = jsonObject.getJSONArray("moreResults")
            if (moreResults == null || moreResults.length() == 0) {
                return mores
            }
            if (moreResults.get(0) is JSONObject) {
                val result = moreResults.getJSONObject(0)
                if (result.has("answer") && result.get("answer") is JSONObject) {
                    val answers = result.getJSONObject("answer")
                    if (answers != null) {
                        answer.text = if (answers.has("text")) answers.getString("text") else ""
                    }
                }
                if (result.has("used_state") && result.get("used_state") is JSONObject) {
                    val used_state = result.getJSONObject("used_state") ?: return mores
                    usedState = UsedState()
                    usedState.content = if (used_state.has("content")) used_state.getString("content") else ""
                    usedState.state = if (used_state.has("state")) used_state.getString("state") else ""
                    usedState.datetime_time = if (used_state.has("datetime.time")) used_state.getString("datetime.time") else ""
                    usedState.datetime_date = if (used_state.has("datetime.date")) used_state.getString("datetime.date") else ""
                }
            }
        }
        mores.mUsedState = usedState
        mores.mAnswer = answer
        return mores
    }
}