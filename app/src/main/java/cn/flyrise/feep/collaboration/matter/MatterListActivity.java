package cn.flyrise.feep.collaboration.matter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.view.ContactsConfirmView;
import cn.flyrise.feep.collaboration.matter.model.DirectoryNode;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.collaboration.matter.model.MatterEvent;
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * Created by klc on 2017/5/12.
 * 选择关联事项列表
 */
public class MatterListActivity extends BaseActivity {

    public static final int MATTER_FLOW = 1;
    public static final int MATTER_MEETING = 2;
    public static final int MATTER_KNOWLEDGE = 3;
    public static final int MATTER_SCHEDULE = 5;
    private List<Matter> mSelectedAssociations = new ArrayList<>();
    private ContactsConfirmView mConfirmView;

    private MatterListFragment mFlowFragment;
    private MatterListFragment mMeetingFragment;
    private MatterListFragment mScheduleFragment;
    private KnowledgeFragment mKnowledgeFragment;
    private int searchType = MATTER_FLOW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_association_list);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        toolbar.setTitle(R.string.matter);
        toolbar.setRightIcon(R.drawable.search_icon_knowledge);
        toolbar.setLineVisibility(View.GONE);
        toolbar.setRightImageClickListener(v -> {
            Intent intent = new Intent(MatterListActivity.this, MatterSearchListActivity.class);
            intent.putExtra("associations", mSelectedAssociations.toArray(new Matter[]{}));
            intent.putExtra("type", searchType);
            if (searchType == MATTER_KNOWLEDGE) {
                DirectoryNode directoryNode = mKnowledgeFragment.getDirectoryNode();
                String folderID = directoryNode.id;
                String attr = directoryNode.attr;
                intent.putExtra("folderID", folderID);
                intent.putExtra("attr", attr);
            }
            startActivity(intent);
        });
    }

    @Override
    public void bindView() {
        mConfirmView = (ContactsConfirmView) findViewById(R.id.confirmView);
        mConfirmView.setConfirmClickListener(view -> {          // 确认
            Intent data = new Intent();
//            if (CommonUtil.nonEmptyList(mSelectedAssociations)) {
//                record.putExtra("selectedAssociation", mSelectedAssociations.toArray(new Matter[]{}));
//            }
            data.putExtra("selectedAssociation", mSelectedAssociations.toArray(new Matter[]{}));
            setResult(Activity.RESULT_OK, data);
            finish();
        });

        mConfirmView.setPreviewClickListener(view -> {          // 开启预览界面
            if (CommonUtil.isEmptyList(mSelectedAssociations)) {
                return;
            }

            Intent intent = new Intent(MatterListActivity.this, MatterResultActivity.class);
            intent.putExtra("associations", mSelectedAssociations.toArray(new Matter[]{}));
            startActivityForResult(intent, 1024);
        });
        this.updateSelectedResult();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024 && data != null) {
            // 获取在预览界面被用户删除的数据
            Parcelable[] parcelables = data.getParcelableArrayExtra("deleteAssociations");
            if (parcelables == null || parcelables.length == 0) {
                return;
            }
            Matter[] deletedAssociations = Arrays.copyOf(parcelables, parcelables.length, Matter[].class);
            if (deletedAssociations.length > 0) {
                for (Matter association : deletedAssociations) {
                    switch (association.matterType) {
                        case MATTER_FLOW:
                            mFlowFragment.deleteAssociation(association);
                            break;
                        case MATTER_MEETING:
                            mMeetingFragment.deleteAssociation(association);
                            break;
                        case MATTER_SCHEDULE:
                            mScheduleFragment.deleteAssociation(association);
                            break;
                        case MATTER_KNOWLEDGE:
                            mKnowledgeFragment.deleteAssociation(association);
                    }

                    if (mSelectedAssociations.contains(association)) {
                        mSelectedAssociations.remove(association);
                    }
                }

                mFlowFragment.notifyDataSetChange();
                mMeetingFragment.notifyDataSetChange();
                mScheduleFragment.notifyDataSetChange();
                mKnowledgeFragment.notifyDataSetChange();
                this.updateSelectedResult();
            }
        }
    }

    @Override
    public void bindData() {
        List<String> titles = Arrays.asList(getString(R.string.flow), getString(R.string.meeting), getString(R.string.knowledge),
                getString(R.string.schedule));
        List<Fragment> fragments =
                Arrays.asList(mFlowFragment = MatterListFragment.newInstance(MATTER_FLOW),
                        mMeetingFragment = MatterListFragment.newInstance(MATTER_MEETING),
                        mKnowledgeFragment = KnowledgeFragment.newInstance(),
                        mScheduleFragment = MatterListFragment.newInstance(MATTER_SCHEDULE));

        Parcelable[] parcelables = getIntent().getParcelableArrayExtra("selectedAssociation");
        if (parcelables != null && parcelables.length > 0) {
            Matter[] selectedAssociations = Arrays.copyOf(parcelables, parcelables.length, Matter[].class);
            mSelectedAssociations.clear();

            List<Matter> flowAssociations = new ArrayList<>();
            List<Matter> meetingAssociations = new ArrayList<>();
            List<Matter> scheduleAssociations = new ArrayList<>();
            List<Matter> knowledgeAssociations = new ArrayList<>();

            for (Matter association : selectedAssociations) {
                mSelectedAssociations.add(association);
                switch (association.matterType) {
                    case MATTER_FLOW:
                        flowAssociations.add(association);
                        break;
                    case MATTER_MEETING:
                        meetingAssociations.add(association);
                        break;
                    case MATTER_SCHEDULE:
                        scheduleAssociations.add(association);
                        break;
                    case MATTER_KNOWLEDGE:
                        knowledgeAssociations.add(association);
                        break;
                }
            }

            mFlowFragment.setSelectedAssociations(flowAssociations);
            mMeetingFragment.setSelectedAssociations(meetingAssociations);
            mScheduleFragment.setSelectedAssociations(scheduleAssociations);
            mKnowledgeFragment.setSelectedAssociations(knowledgeAssociations);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        for (String title : titles) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(title);
        }
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        BaseFragmentPagerAdapter adapter = new BaseFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        adapter.setTitles(titles);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(titles.size());
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    searchType = MATTER_FLOW;
                }
                else if (position == 1) {
                    searchType = MATTER_MEETING;
                }
                else if (position == 2) {
                    searchType = MATTER_KNOWLEDGE;
                }
                else {
                    searchType = MATTER_SCHEDULE;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        updateSelectedResult();
    }

    public void addSelectedAssociation(Matter association) {
        mSelectedAssociations.add(association);
        this.updateSelectedResult();
    }

    public void removeSelectedAssociation(Matter association) {
        mSelectedAssociations.remove(association);
        this.updateSelectedResult();
    }

    private void updateSelectedResult() {
        String text = String.format(getResources().getString(R.string.select_im_user), " " + mSelectedAssociations.size());
        mConfirmView.updateText(text);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAssociationEvent(MatterEvent event) {
        if (event.type == 1) {
            removeSelectedAssociation(event.association);
            switch (event.association.matterType) {
                case MATTER_FLOW:
                    mFlowFragment.deleteAssociation(event.association);
                    mFlowFragment.notifyDataSetChange();
                    break;
                case MATTER_MEETING:
                    mMeetingFragment.deleteAssociation(event.association);
                    mMeetingFragment.notifyDataSetChange();
                    break;
                case MATTER_SCHEDULE:
                    mScheduleFragment.deleteAssociation(event.association);
                    mScheduleFragment.notifyDataSetChange();
                    break;
                case MATTER_KNOWLEDGE:
                    mKnowledgeFragment.deleteAssociation(event.association);
                    mKnowledgeFragment.notifyDataSetChange();
            }
        }
        else {
            addSelectedAssociation(event.association);
            switch (event.association.matterType) {
                case MATTER_FLOW:
                    mFlowFragment.addAssociation(event.association);
                    mFlowFragment.notifyDataSetChange();
                    break;
                case MATTER_MEETING:
                    mMeetingFragment.addAssociation(event.association);
                    mMeetingFragment.notifyDataSetChange();
                    break;
                case MATTER_SCHEDULE:
                    mScheduleFragment.addAssociation(event.association);
                    mScheduleFragment.notifyDataSetChange();
                    break;
                case MATTER_KNOWLEDGE:
                    mKnowledgeFragment.addAssociation(event.association);
                    mKnowledgeFragment.notifyDataSetChange();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
