package cn.flyrise.feep.knowledge;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.borax12.materialdaterangepicker.DateTimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.workplan7.view.BottomWheelSelectionDialog;

/**
 * Created by klc
 */

public abstract class DateTimeBaseActivity extends BaseActivity implements DateTimePickerDialog.ButtonCallBack,
        DateTimePickerDialog.DismissListener {

    private TextView startTimeYMD;
    private TextView startTimeHm;
    private TextView endTimeYMD;
    private TextView endTimeHm;
    protected LinearLayout startLayout, endLayout, reminderLayout;
    private boolean isClickStartDate = false;
    public Calendar startCalendar;
    public Calendar endCalendar;
    private Calendar minCalendar;

    @Override
    public void bindView() {
        super.bindView();
		startLayout = (LinearLayout) this.findViewById(R.id.file_start_time_ll);
		endLayout = (LinearLayout) this.findViewById(R.id.file_end_time_ll);
        reminderLayout = (LinearLayout) this.findViewById(R.id.file_reminder_time_ll);
        startTimeYMD = (TextView) this.findViewById(R.id.file_start_time_date);
        startTimeHm = (TextView) this.findViewById(R.id.file_start_time);
        endTimeYMD = this.findViewById(R.id.file_end_time_date);
        endTimeHm = (TextView) this.findViewById(R.id.file_end_time);
    }

    @Override
    public void bindData() {
        super.bindData();
        startCalendar = Calendar.getInstance();
        minCalendar = (Calendar) startCalendar.clone();
        startTimeYMD.setText(DateUtil.subDateYYYYMMDD(this, startCalendar));
        startTimeHm.setText(DateUtil.subDatehm(startCalendar));
        endTimeYMD.setText("");
        endTimeHm.setText(getString(R.string.know_infinite));

    }

    @Override
    public void bindListener() {
        super.bindListener();

        startLayout.setOnClickListener(v -> {
            isClickStartDate = true;
            openDatePicket(startCalendar, false);
        });


        endLayout.setOnClickListener(v -> {
            if (endCalendar == null) {
                openDatePicket(Calendar.getInstance(), true);
            } else {
                openDatePicket(endCalendar, true);
            }
        });


    }


    protected void openDatePicket(Calendar calendar, boolean canClear) {
        DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
        dateTimePickerDialog.setDateTime(calendar);
        dateTimePickerDialog.setButtonCallBack(this);
        dateTimePickerDialog.setDismissListener(this);
        dateTimePickerDialog.setCanClear(canClear);
        dateTimePickerDialog.setMinCalendar(minCalendar);
        dateTimePickerDialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_MIN);
        dateTimePickerDialog.show(getFragmentManager(), "dateTimePickerDialog");
    }

    @Override
    public void onClearClick() {
        endCalendar = null;
        endTimeYMD.setText("");
        endTimeHm.setText(R.string.know_infinite);
    }

    @Override
    public void onOkClick(Calendar result, DateTimePickerDialog dialog) {
        if (isClickStartDate) {
            startCalendar = result;
            startTimeHm.setText(DateUtil.subDatehm(startCalendar));
            startTimeYMD.setText(DateUtil.subDateYYYYMMDD(this, startCalendar));
        } else {
            endCalendar = result;
            endTimeYMD.setText(DateUtil.subDateYYYYMMDD(this, endCalendar));
            endTimeHm.setText(DateUtil.subDatehm(endCalendar));
        }
        dialog.dismiss();
    }

    @Override
    public void onDismiss(DateTimePickerDialog dateTimePickerDialog) {
        isClickStartDate = false;
    }

    public boolean checkTime() {
        boolean result = true;
        if (startCalendar.compareTo(minCalendar) == -1) {
            showStartTimeMaxMessage();
            return false;
        }
        if (endCalendar == null) {
            result = true;
        } else if (startCalendar.compareTo(endCalendar) >= 0) {
            result = false;
        }
        if (!result) {
            showStartTimeMixMessage();
        }
        return result;
    }

    abstract public void showStartTimeMaxMessage();

    abstract public void showStartTimeMixMessage();

    abstract void onSubmitClick();
}
