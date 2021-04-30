package cn.flyrise.feep.location.util;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;

/**
 * 新建：陈冕;
 * 日期： 2018-1-15-15:20.
 */

public class GpsStateUtils {

	private Context mContext;

	private boolean gpsLastState;

	private GpsStateListener listener;

	public GpsStateUtils(Context context) {
		this(context, (GpsStateListener) context);
	}

	public GpsStateUtils(Context context, GpsStateListener listener) {
		this.mContext = context;
		this.listener = listener;
		mContext.getContentResolver()
				.registerContentObserver(Settings.Secure.getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED), false, mGpsMonitor);
	}

	public void destroy() {
		mContext.getContentResolver().unregisterContentObserver(mGpsMonitor);
	}

	public void requsetGpsIsState() {
		gpsLastState = gpsIsOpen();
	}

	public boolean gpsIsOpen() {//GPS是否开启
		LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}


	public void openGPSSetting(String hintMessage) {
		if (!gpsIsOpen()) {
			new FEMaterialDialog.Builder(mContext)
					.setMessage(hintMessage)
					.setPositiveButton(mContext.getResources().getString(R.string.lockpattern_setting_button_text), v -> startSeeting())
					.setNeutralButton(mContext.getResources().getString(R.string.cancel_group_chat), v -> listener.cancleDialog())
					.setDismissListener(dialog -> listener.onDismiss())
					.build().show();
		}
	}

	private void startSeeting() {//转到手机设置界面，用户设置GPS
		((AppCompatActivity) mContext).startActivityForResult(new Intent
				(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0); // 设置完成后返回到原来的界面
	}

	private final ContentObserver mGpsMonitor = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (gpsLastState == gpsIsOpen()) {
				return;
			}
			else {
				gpsLastState = !gpsLastState;
			}
			((AppCompatActivity) mContext).runOnUiThread(() -> listener.onGpsState(gpsLastState));
		}
	};

	public interface GpsStateListener {

		void onGpsState(boolean isGpsState);

		void cancleDialog();

		void onDismiss();
	}

}
