package cn.flyrise.feep.location.adapter;

import static cn.flyrise.feep.core.common.utils.CommonUtil.isEmptyList;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.view.SwipeLayout;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.SignPoiItem;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索签到/搜索自定义考勤点
 */
public class LocationCustomSearchAdapter extends FEListAdapter<SignPoiItem> {

	private final List<SwipeLayout> mSwipeLayouts = new ArrayList<>();
	private OnItemClickListener mItemCLickListener;

	private boolean isEnabled = true;

	private String searchKey;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			View view = (View) msg.obj;
			if (view != null) view.setEnabled(true);
		}
	};

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}


	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemCLickListener = listener;
	}

	public SignPoiItem getItem(int position) {
		return isEmptyList(dataList) || position < 0 || position >= dataList.size() ? null : dataList.get(position);
	}

	public void setSearchKey(String key) {
		this.searchKey = key;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getDataSourceCount() {
		return CommonUtil.isEmptyList(dataList) ? 0 : dataList.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		ViewHolder holder = (ViewHolder) viewHolder;
		final SignPoiItem signPoiItem = dataList.get(position);
		if (signPoiItem == null) return;

		holder.contact.setText(getAddress(signPoiItem.poiItem, signPoiItem.saveItem));
		holder.frontView.setEnabled(isEnabled);

		String title = getTitle(signPoiItem.poiItem, signPoiItem.saveItem);
		if (!TextUtils.isEmpty(searchKey) && title.contains(searchKey)) {
			SpannableString spannableString = new SpannableString(title);
			spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#28B9FF"))
					, title.indexOf(searchKey), title.indexOf(searchKey) + searchKey.length()
					, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			holder.subject.setText(spannableString);
		}
		else {
			holder.subject.setText(title);
		}

		holder.mSwipeLayout.setOnSwipeListener(holder.mSwipeLayout.new OnSwipeListenerAdapter() {
			@Override
			public void onOpen(SwipeLayout swipeLayout) {
				closeAllSwipeView();
				mSwipeLayouts.add(swipeLayout);
			}

			@Override
			public void onStartOpen(SwipeLayout swipeLayout) {
				closeAllSwipeView();
			}
		});

		holder.button.setOnClickListener(v -> {
			if (mItemCLickListener != null) {
				mItemCLickListener.onSignInClick(holder.mSwipeLayout, poiItemTransformaLocationSaveItem(signPoiItem), position);
				handler.postDelayed(this::closeAllSwipeView, 500);
			}
		});

		holder.frontView.setOnClickListener(v -> {
			if (mItemCLickListener != null) {
				showSignButton(holder.mSwipeLayout);
				mItemCLickListener.onFrontViewClick();
				sendViewButtom(holder.button);
			}
		});
	}

	private void showSignButton(SwipeLayout swipeLayout) {
		if (swipeLayout.isOpen()) {
			swipeLayout.close();
			closeAllSwipeView();
		}
		else swipeLayout.open();
	}

	private LocationSaveItem poiItemTransformaLocationSaveItem(SignPoiItem signPoiItem) {
		if (signPoiItem.poiItem == null) return signPoiItem.saveItem;
		LocationSaveItem saveItem = new LocationSaveItem();
		saveItem.poiId = signPoiItem.poiItem.getPoiId();
		saveItem.title = signPoiItem.poiItem.getTitle();
		saveItem.content = signPoiItem.poiItem.getCityName() + signPoiItem.poiItem.getSnippet();
		saveItem.Latitude = signPoiItem.poiItem.getLatLonPoint().getLatitude();
		saveItem.Longitude = signPoiItem.poiItem.getLatLonPoint().getLongitude();
		return saveItem;
	}

	private String getTitle(PoiItem poiItem, LocationSaveItem saveItem) {
		return poiItem != null ? poiItem.getTitle() : saveItem != null ? saveItem.title : "";
	}

	private String getAddress(PoiItem poiItem, LocationSaveItem saveItem) {
		return poiItem != null ? (poiItem.getCityName() + poiItem.getSnippet()) : saveItem != null ? saveItem.content : "";
	}

	private void sendViewButtom(View view) {
		if (view == null) return;
		view.setEnabled(false);
		Message message = handler.obtainMessage();
		message.obj = view;
		handler.sendMessageDelayed(message, 320);
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_search_list_item, parent, false);
		return new ViewHolder(view);
	}

	private static class ViewHolder extends RecyclerView.ViewHolder {

		TextView subject;    // 公司名称
		TextView contact;    // 地址
		TextView exceedRange;    // 超出范围
		Button button;       // 上报的按钮
		LinearLayout frontView;

		SwipeLayout mSwipeLayout;

		public ViewHolder(View convertView) {
			super(convertView);
			mSwipeLayout = convertView.findViewById(R.id.swipe_layout);
			subject = convertView.findViewById(R.id.myItemView_subject);
			contact = convertView.findViewById(R.id.myItemView_contact);
			exceedRange = convertView.findViewById(R.id.no_sign_range);
			button = convertView.findViewById(R.id.id_location);
			frontView = convertView.findViewById(R.id.id_front);
		}
	}

	public interface OnItemClickListener {

		void onSignInClick(SwipeLayout swipeLayout, LocationSaveItem saveItem, int position);

		void onFrontViewClick();

	}

	public void closeAllSwipeView() {
		for (SwipeLayout swipeLayout : mSwipeLayouts) {
			swipeLayout.close();
		}
		mSwipeLayouts.clear();
	}
}
