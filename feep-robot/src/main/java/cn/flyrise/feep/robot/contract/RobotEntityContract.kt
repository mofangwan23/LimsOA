package cn.flyrise.feep.robot.contract

import cn.flyrise.feep.robot.module.RobotModuleItem

/**
 * 新建：陈冕;
 * 日期： 2017-12-1-9:53
 * 语义理解返回具体类型
 */

const val feoaMessage = "MUSICFEEP.feoa_message" //feoa
const val scheduleX = "scheduleX"//日程提醒
const val weather = "weather" //天气
const val poetry = "poetry"//诗词对答
const val openQA = "openQA" //语音闲聊
const val baike = "baike"//百科
const val calc = "calc"//计算
const val cookbook = "cookbook"//菜谱
const val datetime = "datetime"//时间
const val flight = "flight"//航班（灰机）
const val motorViolation = "motorViolation"//违章查询
const val musicX = "musicX"//音乐
const val stock = "stock"//股票
const val train = "train"//火车
const val translation = "translation"//翻译
const val news = "news"//新闻
const val joke = "joke" //讲笑话
const val story = "story"//故事
const val englishEveryday = "englishEveryday"//每天一句英语
const val wordFinding = "wordFinding"//近反义祠
const val riddle = "riddle"//猜谜语
const val brainTeaser = "LEIQIAO.brainTeaser"//脑筋急转弯
const val holiday = "holiday"//节假日查询
const val dream = "dream"//周公解梦
const val chineseZodiac = "chineseZodiac"//生肖

interface OnRobotClickeItemListener {
    fun robotClickeItem(detail: RobotModuleItem?)
}
