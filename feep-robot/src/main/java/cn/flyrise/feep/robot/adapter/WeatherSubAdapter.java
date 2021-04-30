package cn.flyrise.feep.robot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.entity.WeatherResultData;
import cn.flyrise.feep.robot.util.RobotWeatherType;
import java.util.List;


/**
 * 新建：陈冕;
 * 日期： 2017-8-14-11:53.
 */

public class WeatherSubAdapter extends RecyclerView.Adapter<WeatherSubAdapter.WeathersViewHolder> {

	private Context mContext;

	private List<WeatherResultData> mWeatherDatas;

	public WeatherSubAdapter(Context context, List<WeatherResultData> weatherDatas) {
		this.mContext = context;
		this.mWeatherDatas = weatherDatas;
	}


	@Override
	public WeathersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.robot_weather_item, parent, false);
		return new WeathersViewHolder(view);
	}

	@Override
	public void onBindViewHolder(WeathersViewHolder holder, int position) {
		WeatherResultData weatherData = mWeatherDatas.get(position);
		if (weatherData == null) {
			return;
		}
		holder.mWeatherWind.setText(weatherData.wind);
		holder.mWeatherTempRange.setText(weatherData.tempRange);
		holder.mWeather.setText(weatherData.weather);
		holder.mWeatherWeek.setText(DateUtil.getDayOfWeek(mContext, weatherData.date));
		String date = DateUtil.subDateMMDD(mContext, DateUtil.str2Calendar(weatherData.date));
		if (!TextUtils.isEmpty(date)) {
			holder.mWeatherDate.setText(date);
		}
		FEImageLoader.load(mContext, holder.mWeatherIcon,
				RobotWeatherType.getInstance().getWeatheIcon(weatherData.weatherType, weatherData.lastUpdateTime), R.drawable.weather_0);
		if (position == 0) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.rightMargin = 1;
			holder.mLayout.setLayoutParams(params);
		}

		if (position == mWeatherDatas.size() - 1) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.leftMargin = 1;
			holder.mLayout.setLayoutParams(params);
		}

	}

	@Override
	public int getItemCount() {
		return mWeatherDatas == null ? 0 : mWeatherDatas.size();
	}

	class WeathersViewHolder extends RecyclerView.ViewHolder {

		TextView mWeatherDate;
		TextView mWeatherWind;
		TextView mWeatherTempRange;
		TextView mWeather;
		ImageView mWeatherIcon;
		LinearLayout mLayout;
		TextView mWeatherWeek;

		WeathersViewHolder(View itemView) {
			super(itemView);
			mLayout = itemView.findViewById(R.id.layout);
			mWeatherDate = itemView.findViewById(R.id.weather_date);
			mWeatherWind = itemView.findViewById(R.id.weather_wind);
			mWeatherTempRange = itemView.findViewById(R.id.temp_range);
			mWeather = itemView.findViewById(R.id.weather);
			mWeatherWeek = itemView.findViewById(R.id.weather_week);
			mWeatherIcon = itemView.findViewById(R.id.weather_type);
		}
	}

}
