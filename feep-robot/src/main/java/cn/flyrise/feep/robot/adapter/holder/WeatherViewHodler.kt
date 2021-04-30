package cn.flyrise.feep.robot.adapter.holder

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.robot.R
import cn.flyrise.feep.robot.adapter.WeatherSubAdapter
import cn.flyrise.feep.robot.entity.WeatherResultData
import cn.flyrise.feep.robot.module.RobotModuleItem
import cn.flyrise.feep.robot.util.RobotWeatherType

/**
 * 新建：陈冕;
 * 日期： 2017-8-7-10:58.
 * 内容居中显示的layout(天气预报等)
 */

class WeatherViewHodler(itemView: View, private val mContext: Context) : RobotViewHodler(itemView) {

    private val conContentTv: TextView
    private val mWeatherCity: TextView
    private val mWeatherDate: TextView
    private val mWeatherWind: TextView
    private val mWeatherTempRange: TextView
    private val mWeather: TextView
    private val mWeatherWeek: TextView
    private val mWeatherIcon: ImageView

    private val mRecyclerWeather: RecyclerView

    init {
        conContentTv = itemView.findViewById(R.id.con_content_tv)
        mWeatherCity = itemView.findViewById(R.id.city_weather)
        mWeatherDate = itemView.findViewById(R.id.weather_date)
        mWeatherWind = itemView.findViewById(R.id.weather_wind)
        mWeatherTempRange = itemView.findViewById(R.id.temp_range)
        mWeather = itemView.findViewById(R.id.weather)
        mWeatherWeek = itemView.findViewById(R.id.weather_week)
        mWeatherIcon = itemView.findViewById(R.id.weather_type)
        mRecyclerWeather = itemView.findViewById(R.id.recycler_weather)
    }

    fun setWeatherViewHodler() {
        if (!TextUtils.isEmpty(item.content)) {
            conContentTv.text = item.content
        }
        conContentTv.setSingleLine(false)

        val weatherData = rquestWeather(item)
        if (weatherData != null) {
            mWeatherCity.text = weatherData.city
            mWeatherDate.text = weatherData.date
            mWeatherWind.text = weatherData.wind
            mWeatherTempRange.text = weatherData.tempRange
            mWeather.text = weatherData.weather
            mWeatherWeek.text = DateUtil.getDayOfWeek(mContext, weatherData.date)
            FEImageLoader.load(mContext, mWeatherIcon,
                    RobotWeatherType.getInstance().getWeatheIcon(weatherData.weatherType, weatherData.lastUpdateTime),
                    R.drawable.weather_0)
        }
        mRecyclerWeather.layoutManager = GridLayoutManager(mContext, 3)
        val weatherSubAdapter = WeatherSubAdapter(mContext, weathers(item))
        mRecyclerWeather.adapter = weatherSubAdapter
    }

    private fun rquestWeather(item: RobotModuleItem): WeatherResultData? {
        if (CommonUtil.isEmptyList(item.weatherDatas)) return null
        val date = if (item.date?.length ?: 0 > 10) item.date.substring(0, 10) else item.date
        return if (TextUtils.isEmpty(date)) item.weatherDatas[0]
        else item.weatherDatas.firstOrNull { TextUtils.equals(it.date, date) }
    }

    private fun weathers(item: RobotModuleItem): List<WeatherResultData>? {
        if (CommonUtil.isEmptyList(item.weatherDatas)) return null
        val date = if (item.date?.length ?: 0 > 10) item.date.substring(0, 10) else item.date
        return getThreeWeatherDate(if (TextUtils.isEmpty(date)) item.weatherDatas.subList(1, item.weatherDatas.size)
        else item.weatherDatas.filterNot { TextUtils.equals(it.date, date) })
    }

    private fun getThreeWeatherDate(weatherDatas: List<WeatherResultData>): List<WeatherResultData> {
        return weatherDatas.subList(0, if (weatherDatas.size > 3) 3 else weatherDatas.size)
    }

    override fun onDestroy() {

    }
}
