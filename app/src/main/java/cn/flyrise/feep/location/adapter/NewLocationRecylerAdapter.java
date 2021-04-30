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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.location;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.view.SwipeLayout;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.SignPoiItem;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2016/8/18 11:26
 * 替换旧的 LocationReportAdapter
 */
public class NewLocationRecylerAdapter extends BaseMessageRecyclerAdapter<SignPoiItem> {

	private final List<SwipeLayout> mSwipeLayouts = new ArrayList<>();
	private boolean isNotAllowSuperRange = false;//不允许超范围
	private boolean isEnabled = true;
	private boolean isCanReport = true;

	private ViewHolder mHolder;//当前选中的

	private int mLocationType = K.location.LOCATION_SIGN;
	private int selectedPosition = -1;
	private String selectedPoiId; //当前点击的PoiId
	private String searchKey;

	private OnItemClickListener mItemCLickListener;

	public void setNotAllowSuperRange(boolean isNotAllowSuperRange) {//不允许超范围签到，置灰超范围的地点
		this.isNotAllowSuperRange = isNotAllowSuperRange;
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			View view = (View) msg.obj;
			if (view != null) view.setEnabled(true);
		}
	};

	public void setCleanAllSignIcon() {//取消掉所有打卡成功标签
		if (CommonUtil.isEmptyList(dataList)) return;
		for (SignPoiItem item : dataList) {
			item.isSignSuccess = false;
		}
	}

	public void setCanReport(boolean isCanReport) {
		if (this.isCanReport != isCanReport) notifyDataSetChanged();
		this.isCanReport = isCanReport;
	}

	public void setWorkingState(String time) {//设置考勤组状态
		if (CommonUtil.isEmptyList(dataList) || TextUtils.isEmpty(time) || mHolder == null) return;
		mHolder.mTvSignIn.setText(isCanReport ? "" : time);
	}

	public int getSelectedPosition() {
		return CommonUtil.isEmptyList(dataList) && selectedPosition >= dataList.size() ? -1 : selectedPosition;
	}

	public List<PoiItem> getPoiItem() {
		if (CommonUtil.isEmptyList(dataList)) return null;
		List<PoiItem> poiItems = new ArrayList<>();
		for (SignPoiItem item : dataList) {
			poiItems.add(item.poiItem);
		}
		return poiItems;
	}

	public void setLocationType(int type) {
		mLocationType = type;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
		notifyDataSetChanged();
	}

	public void addPoiItems(List<SignPoiItem> items) {
		if (isEmptyList(items)) return;
		if (dataList == null) dataList = new ArrayList<>(items.size());
		dataList.addAll(items);
		notifyDataSetChanged();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemCLickListener = listener;
	}

	public SignPoiItem getItem(int position) {
		return isEmptyList(dataList) || position < 0 || position >= dataList.size() ? null : dataList.get(position);
	}

	public void signSuccessSealPoiItem() {//签到成功盖章
		if (TextUtils.isEmpty(selectedPoiId) || CommonUtil.isEmptyList(dataList)) return;
		for (SignPoiItem signPoiItem : dataList) {
			if (isSelectedPoiIdEquals(getPoiId(signPoiItem.poiItem, signPoiItem.saveItem), selectedPoiId)) {
				signPoiItem.isSignSuccess = true;
				selectedPoiId = "";
				break;
			}
		}
		notifyDataSetChanged();
	}

	private boolean isSelectedPoiIdEquals(String poiId, String selectedPoiId) {
		return TextUtils.equals(poiId, selectedPoiId);
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
		String title = getTitle(signPoiItem.poiItem, signPoiItem.saveItem);

		holder.mTvTitle.setTextSize(15);
		holder.mTvAddress.setTextSize(12);
		holder.mTvAddress.setTextColor(Color.parseColor("#9DA3A6"));
		holder.mTvTitle.setText(mLocationType == location.LOCATION_SEARCH ? getSearchTitle(title) : title);

		holder.mTvAddress.setText(getAddress(signPoiItem.poiItem, signPoiItem.saveItem));
		holder.frontView.setEnabled(isEnabled);
		holder.mTvSignIn.setText("");
		if (isCanReport) holder.mTvSignIn.setBackgroundResource(R.drawable.btn_sign);
		else holder.mTvSignIn.setBackgroundColor(Color.parseColor("#DDDDDD"));

		if (isNotAllowSuperRange) {
			holder.exceedRange.setVisibility(signPoiItem.isUltraRange ? View.GONE : View.VISIBLE);
			holder.frontView.setEnabled(signPoiItem.isUltraRange && isEnabled);
			holder.mTvTitle.setTextColor(Color.parseColor(signPoiItem.isUltraRange ? "#535353" : "#55000000"));
			holder.mTvAddress.setTextColor(Color.parseColor(signPoiItem.isUltraRange ? "#FF6F6F6F" : "#32000000"));
		}

		holder.mSwipeLayout.setOnSwipeListener(holder.mSwipeLayout.new OnSwipeListenerAdapter() {
			@Override
			public void onOpen(SwipeLayout swipeLayout) {
				if (holder.favorite.getTag() != getPoiId(signPoiItem.poiItem, signPoiItem.saveItem)) return;
				closeAllSwipeView();
				mSwipeLayouts.add(swipeLayout);
			}

			@Override
			public void onStartOpen(SwipeLayout swipeLayout) {
				closeAllSwipeView();
			}
		});

		holder.mTvSignIn.setOnClickListener(v -> {
			if (mItemCLickListener == null) return;
			if (!isCanReport) {
				mItemCLickListener.onSignWorkingClick();
				return;
			}
			selectedPoiId = getPoiId(signPoiItem.poiItem, signPoiItem.saveItem);
			selectedPosition = position;
			mItemCLickListener.onSignInClick(holder.mSwipeLayout, poiItemTransformaLocationSaveItem(signPoiItem), position);
			handler.postDelayed(this::closeAllSwipeView, 500);
		});

		if (mLocationType == location.LOCATION_SEARCH) {
			holder.frontView.setOnClickListener(v -> {
				if (mItemCLickListener == null) return;
				mItemCLickListener.onSignInClick(holder.mSwipeLayout, poiItemTransformaLocationSaveItem(signPoiItem), position);
			});
			holder.mIcon.setVisibility(View.GONE);
		}
		else {
			holder.frontView.setOnClickListener(v -> {
				if (mItemCLickListener == null) return;
				mHolder = holder;
				mHolder.mTvSignIn.setText("");
				holder.favorite.setTag(getPoiId(signPoiItem.poiItem, signPoiItem.saveItem));
				showSignButton(holder.mSwipeLayout);
				mItemCLickListener.onFrontViewClick();
				sendViewButtom(holder.mTvSignIn);
			});
		}
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

	private SpannableString getSearchTitle(String title) {
		if (!TextUtils.isEmpty(searchKey) && title.contains(searchKey)) {
			SpannableString spannableString = new SpannableString(title);
			spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#28B9FF"))
					, title.indexOf(searchKey), title.indexOf(searchKey) + searchKey.length()
					, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			return spannableString;
		}
		return new SpannableString(title);
	}

	private String getTitle(PoiItem poiItem, LocationSaveItem saveItem) {
		return poiItem != null ? poiItem.getTitle() : saveItem != null ? saveItem.title : "";
	}

	private String getAddress(PoiItem poiItem, LocationSaveItem saveItem) {
		return poiItem != null ? (poiItem.getCityName() + poiItem.getSnippet()) : saveItem != null ? saveItem.content : "";
	}

	private String getPoiId(PoiItem poiItem, LocationSaveItem saveItem) {
		return poiItem != null ? poiItem.getPoiId() : saveItem != null ? saveItem.poiId : "";
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
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.location_report_item, parent, false));
	}

	private static class ViewHolder extends RecyclerView.ViewHolder {

		TextView mTvTitle;    // 公司名称
		TextView mTvAddress;    // 地址
		TextView favorite;   // 距离
		TextView exceedRange;    // 超出范围
		TextView mTvSignIn;
		LinearLayout frontView;
		ImageView mIcon;

		SwipeLayout mSwipeLayout;

		public ViewHolder(View convertView) {
			super(convertView);
			mSwipeLayout = convertView.findViewById(R.id.swipe_layout);
			mTvTitle = convertView.findViewById(R.id.myItemView_subject);
			mTvAddress = convertView.findViewById(R.id.myItemView_contact);
			favorite = convertView.findViewById(R.id.myItemView_favorite);
			exceedRange = convertView.findViewById(R.id.no_sign_range);
			mTvSignIn = convertView.findViewById(R.id.id_woking_time);
			frontView = convertView.findViewById(R.id.id_front);
			mIcon = convertView.findViewById(R.id.myItemView_report);
		}
	}

	public interface OnItemClickListener {

		void onSignInClick(SwipeLayout swipeLayout, LocationSaveItem saveItem, int position);

		void onFrontViewClick();

		void onSignWorkingClick();

	}

	public void closeAllSwipeView() {
		for (SwipeLayout swipeLayout : mSwipeLayouts) {
			swipeLayout.close();
		}
		mSwipeLayouts.clear();
	}
}
