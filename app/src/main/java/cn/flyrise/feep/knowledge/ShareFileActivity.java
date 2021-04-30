package cn.flyrise.feep.knowledge;

import static cn.flyrise.feep.core.common.utils.CommonUtil.isEmptyList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.form.adapter.SpinnerAdapter;
import cn.flyrise.feep.knowledge.contract.PublicFileContract;
import cn.flyrise.feep.knowledge.presenter.PublicFilePresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

import java.util.Arrays;
import java.util.List;

/**
 * Created by KLC on 2016/12/6.
 */
public class ShareFileActivity extends DateTimeBaseActivity implements PublicFileContract.View {


    private TextView mTvPerson;
    private List<AddressBook> mSelectedPersons;
    private PublicFileContract.Presenter mPresent;
    private LinearLayout mLlReminderTime;
    private View mViewLine;


    public static void startKPublicFileActivity(Context context, String fileIDs, String folderID) {
        Intent intent = new Intent(context, ShareFileActivity.class);
        intent.putExtra(KnowKeyValue.EXTRA_PUBLISHFILEID, fileIDs);
        intent.putExtra(KnowKeyValue.EXTRA_PUBLISHFILEPARENTID, folderID);
        ((Activity) context).startActivityForResult(intent, KnowKeyValue.STARTPUBLISHCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.knowledge_public_file);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        toolbar.setTitle(R.string.know_public_file);
        toolbar.setRightText(R.string.share_file);
        toolbar.setRightTextClickListener(v -> {
            onSubmitClick();
        });
    }

    @Override
    public void bindView() {
        super.bindView();
        mTvPerson = (TextView) this.findViewById(R.id.imagetextbuton_receiver);
        mLlReminderTime = this.findViewById(R.id.file_reminder_time_ll);
        mViewLine = this.findViewById(R.id.file_reminder_time_view_line);
        mViewLine.setVisibility(View.GONE);
        mLlReminderTime.setVisibility(View.GONE);
    }

    @Override
    public void bindData() {
        super.bindData();
        mTvPerson.setText(getString(R.string.know_chose_receiver));
        String fileIDs = getIntent().getStringExtra(KnowKeyValue.EXTRA_PUBLISHFILEID);
        String folderID = getIntent().getStringExtra(KnowKeyValue.EXTRA_PUBLISHFILEPARENTID);
        String[] spinnerItems = getResources().getStringArray(R.array.Reminder_time);
        mPresent = new PublicFilePresenterImpl(this, fileIDs, folderID);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        mTvPerson.setOnClickListener(v -> {
            if (CommonUtil.nonEmptyList(mSelectedPersons)) {
                DataKeeper.getInstance().keepDatas(K.knowledge.publish_request_code, mSelectedPersons);
            }
            new ContactsIntent(ShareFileActivity.this)
                    .targetHashCode(ShareFileActivity.this.hashCode())
                    .requestCode(K.knowledge.publish_request_code)
                    .title(CommonUtil.getString(R.string.lbl_message_title_publish_choose))
                    .withSelect()
                    .open();
        });

    }

    @Override
    public void showStartTimeMaxMessage() {
        FEToast.showMessage(getString(R.string.workplan_startdate_min));
    }

    @Override
    public void showStartTimeMixMessage() {
        FEToast.showMessage(getString(R.string.workplan_enddate_max_startdate));
    }

    @Override
    void onSubmitClick() {
        if (!checkTime()) {
            return;
        }
        if (isEmptyList(mSelectedPersons)) {
            FEToast.showMessage(getString(R.string.know_please_add_receiver));
            return;
        }
        StringBuilder receiverIDs = new StringBuilder();
        for (AddressBook person : mSelectedPersons) {
            receiverIDs.append(',').append(person.userId).append("^_^1");
        }
        if (receiverIDs.length() > 0) receiverIDs.deleteCharAt(0);
        String startTime = DateUtil.calendar2StringDateTime(startCalendar);
        String endTime = DateUtil.calendar2StringDateTime(endCalendar);
        mPresent.publicFile(receiverIDs.toString(), CoreZygote.getLoginUserServices().getUserId(), startTime, endTime);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == K.knowledge.publish_request_code) {
            mSelectedPersons = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(ShareFileActivity.this.hashCode());
            mTvPerson.setText(isEmptyList(mSelectedPersons)
                    ? CommonUtil.getString(R.string.know_select_receiver)
                    : String.format(getString(R.string.knowledge_person_select), mSelectedPersons.size()));
        }
    }

    public String getRecipientIds() {
        if (isEmptyList(mSelectedPersons)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (AddressBook person : mSelectedPersons) {
            sb.append(person.userId).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataKeeper.getInstance().removeKeepData(ShareFileActivity.this.hashCode());
    }

    @Override
    public void publishSuccess() {
        setResult(RESULT_OK);
        this.finish();
    }

    @Override
    public void showDealLoading(boolean show) {
        if (show)
            LoadingHint.show(this);
        else
            LoadingHint.hide();
    }

    @Override
    public void showMessage(int resourceID) {
        FEToast.showMessage(getString(resourceID));
    }
}
