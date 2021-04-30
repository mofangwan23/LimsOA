package cn.flyrise.feep.salary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.salary.model.Salary;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-02-17 16:55
 */
public class SalaryListActivity extends BaseSalaryActivity {

    private ExpandableListView mListView;
    private SalaryExpandableAdapter mAdapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_list);
    }

    @Override protected void onStart() {
        super.onStart();
        WMStamp.getInstance().draw(this);
    }

    @Override public void bindView() {
        super.bindView();
        mListView =  findViewById(R.id.listView);
        mListView.setAdapter(mAdapter = new SalaryExpandableAdapter());
        mListView.setGroupIndicator(null);
//        mListView.addItemDecoration(new WMAddressDecoration(WMStamp.getInstance().getWaterMarkText()));      // 设置水印
        mListView.setOnChildClickListener(this::onChildClick);
        FEToolbar mToolbar = findViewById(R.id.toolBar);
        mToolbar.setBackgroundColor(Color.parseColor("#00000000"));
        mToolbar.setTitle("");
        mToolbar.setLineVisibility(View.GONE);
        mToolbar.setDarkMode();

        mSafetyVerifyManager.startVerify(K.salary.gesture_verify_request_code, this);
    }

    private boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        String year = (String) mAdapter.getGroup(groupPosition);
        Salary salary = (Salary) mAdapter.getChild(groupPosition, childPosition);
        Intent intent = new Intent(this, SalaryDetailActivity.class);
        intent.putExtra(K.salary.show_verify_dialog, false);
        intent.putExtra(K.salary.request_month, year + "-" + salary.month);
        startActivity(intent);
        return true;
    }

    @Override public void onVerifySuccess() {
        super.onVerifySuccess();
        showLoading();
        SalaryDataSources.querySalaryMonthLists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(salaryYearMap -> {
                    hideLoading();
                    mAdapter.setYearSalaryMap(salaryYearMap);
                    mListView.expandGroup(0);
                    for (int i = 1, n = mAdapter.getGroupCount(); i < n; i++) {
                        mListView.collapseGroup(i);
                    }
                }, exception -> {
                    exception.printStackTrace();
                    hideLoading();
                    FEToast.showMessage(getResources().getString(R.string.core_data_get_error));
                });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        WMStamp.getInstance().clearWaterMark(this);
    }
}