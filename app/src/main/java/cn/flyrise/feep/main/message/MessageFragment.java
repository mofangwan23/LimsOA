package cn.flyrise.feep.main.message;

import android.Manifest;
import android.support.annotation.NonNull;

import com.borax12.materialdaterangepicker.DateTimePickerDialog;

import java.util.Calendar;
import java.util.Date;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.SystemScheduleUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author KLC
 * @since 2018-01-09 09:49
 */
public abstract class MessageFragment<T> extends BaseMessageFragment<T> {

    protected FELoadingDialog mLoadingDialog;
    protected Object clickObject;

    @Override
    protected void bindListener() {
        super.bindListener();
        mAdapter.setOnItemLongClickListener((view, object) -> {
            clickObject = object;
            showLongClickDialog();
        });
    }

    private void showLongClickDialog() {
        String[] menu = new String[]{"提醒"};
        new FEMaterialDialog.Builder(getActivity())
                .setWithoutTitle(true)
                .setItems(menu, (dialog, v, index) -> {
                    dialog.dismiss();
                    requestCalendarPermission();
                })
                .setCancelable(true)
                .build()
                .show();
    }

    private void requestCalendarPermission() {
        FePermissions.with(this)
                .permissions(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR})
                .rationaleMessage(getResources().getString(com.hyphenate.chatui.R.string.permission_rationale_calendar))
                .requestCode(PermissionCode.CALENDAR)
                .request();
    }

    @PermissionGranted(PermissionCode.CALENDAR)
    public void onCalendarPermissionGanted() {
        showTimeDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void showTimeDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
        dateTimePickerDialog.setDateTime(calendar);
        dateTimePickerDialog.setButtonCallBack(new DateTimePickerDialog.ButtonCallBack() {
            @Override
            public void onClearClick() {
            }

            @Override
            public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
                calendar.set(Calendar.SECOND, 0);
                if (calendar.getTimeInMillis() < System.currentTimeMillis() + 5 * 60 * 1000) {
                    FEToast.showMessage(getString(R.string.schedule_remind_time_hint));
                    return;
                }
                syncCalendarToSystem(calendar);
                dateTimePickerDialog.dismiss();
            }
        });
        dateTimePickerDialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_MIN);
        dateTimePickerDialog.show(getActivity().getFragmentManager(), "dateTimePickerDialog");
    }

    private void syncCalendarToSystem(Calendar calendar) {
        showLoading();
        String title = getMessageTitle(clickObject);
        Observable<Integer> observable = SystemScheduleUtil.addToSystemCalendar(getActivity(), title, title, calendar);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultCode -> {
                            hideLoading();
                            if (resultCode == 200) {
                                FEToast.showMessage(getString(com.hyphenate.chatui.R.string.schedule_remind_success));
                            } else {
                                FEToast.showMessage(getString(com.hyphenate.chatui.R.string.schedule_remind_error));
                            }
                        },
                        exception -> {
                            hideLoading();
                            FEToast.showMessage(getString(com.hyphenate.chatui.R.string.schedule_remind_error));
                        });
    }

    private void showLoading() {
        hideLoading();
        mLoadingDialog = new FELoadingDialog.Builder(getActivity())
                .setLoadingLabel(getResources().getString(com.hyphenate.chatui.R.string.core_loading_wait))
                .setCancelable(false)
                .create();
        mLoadingDialog.show();
    }

    private void hideLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.hide();
            mLoadingDialog = null;
        }
    }

    public abstract String getMessageTitle(Object clickObject);
}
