package cn.flyrise.feep.collaboration.matter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.adpater.MatterListAdapter;
import cn.flyrise.feep.collaboration.matter.adpater.MatterResultAdapter;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-05-12 10:14
 * 已选事项界面
 */
public class MatterResultActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private MatterResultAdapter mAdapter;
    private List<Matter> mDeletedAssociations = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matter_result);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(R.string.select_matter);
        toolbar.setRightText(R.string.collaboration_recorder_ok);
        toolbar.setRightTextClickListener(view -> {
            if (CommonUtil.nonEmptyList(mDeletedAssociations)) {
                Intent data = new Intent();
                data.putExtra("deleteAssociations", mDeletedAssociations.toArray(new Matter[]{}));
                setResult(Activity.RESULT_OK, data);
            }
            finish();
        });
    }

    @Override public void bindView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter = new MatterResultAdapter());

        Parcelable[] parcelables = getIntent().getParcelableArrayExtra("associations");
        Matter[] associations = Arrays.copyOf(parcelables, parcelables.length, Matter[].class);

        List<Matter> selectedAssociations = new ArrayList<>();
        for (Matter association : associations) {
            selectedAssociations.add(association);
            mAdapter.addSelectedAssociation(association);
        }

        Collections.sort(selectedAssociations, (o1, o2) ->
                (o1.matterType < o2.matterType) ? -1 : ((o1.matterType == o2.matterType) ? 0 : 1));
        mAdapter.setAssociationList(selectedAssociations);

        mAdapter.setOnItemClickListener(new MatterListAdapter.OnAssociationCheckChangeListener() {
            @Override public void onAssociationAdd(Matter association) {
                if (mDeletedAssociations.contains(association)) {
                    mDeletedAssociations.remove(association);
                }
            }

            @Override public void onAssociationDelete(Matter deletedAssociation) {
                if (!mDeletedAssociations.contains(associations)) {
                    mDeletedAssociations.add(deletedAssociation);
                }
            }
        });
    }
}
