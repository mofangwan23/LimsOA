package cn.flyrise.feep.collaboration.matter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.adpater.DirectoryAdapter;
import cn.flyrise.feep.collaboration.matter.adpater.DirectoryTextAdapter;
import cn.flyrise.feep.collaboration.matter.model.DirectoryNode;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.collaboration.matter.model.MatterPageInfo;
import cn.flyrise.feep.collaboration.matter.presenter.KnowPresenter;
import cn.flyrise.feep.collaboration.matter.presenter.KnowView;

/**
 * Created by klc on 2017/5/16.
 */
public class KnowledgeFragment extends Fragment implements KnowView {

    private RecyclerView mLvLeftTree;
    private RecyclerView mLvTopTree;
    private DirectoryAdapter mLeftTreeAdapter;
    private DirectoryTextAdapter mTopTreeAdapter;
    private MatterListFragment fileFragment;
    private KnowPresenter mPresenter;
    private List<Matter> mSelectedAssociations;

    public static KnowledgeFragment newInstance() {
        return new KnowledgeFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_matter_knowledge, container, false);
        initView(contentView);
        initData();
        initListener();
        return contentView;
    }

    private void initView(View contentView) {
        mLvLeftTree = (RecyclerView) contentView.findViewById(R.id.lv_directory);
        mLvLeftTree.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLvTopTree = (RecyclerView) contentView.findViewById(R.id.lv_directorytext);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mLvTopTree.setLayoutManager(linearLayoutManager);

        fileFragment = MatterListFragment.newInstance(MatterListActivity.MATTER_KNOWLEDGE);
        fileFragment.setSelectedAssociations(mSelectedAssociations);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fts = fm.beginTransaction();
        fts.add(R.id.fl_file, fileFragment);
        fts.commit();
    }

    private void initData() {
        mLeftTreeAdapter = new DirectoryAdapter();
        mLvLeftTree.setAdapter(mLeftTreeAdapter);
        mTopTreeAdapter = new DirectoryTextAdapter();
        mLvTopTree.setAdapter(mTopTreeAdapter);
        mPresenter = new KnowPresenter(this);
        mPresenter.loadFolderTree();
    }

    private void initListener() {
        mLeftTreeAdapter.setOnItemClickListener((view, object) -> mPresenter.leftFolderClick((DirectoryNode) object));
        mLeftTreeAdapter.setOnHeadClickListener(view -> mPresenter.leftHeadClick());
        mTopTreeAdapter.setOnItemClickListener((view, object) -> mPresenter.topItemClick((DirectoryNode) object));
    }

    public void deleteAssociation(Matter association) {
        fileFragment.deleteAssociation(association);
    }

    public void addAssociation(Matter association) {
        fileFragment.addAssociation(association);
    }

    public void setSelectedAssociations(List<Matter> associations) {
        this.mSelectedAssociations = associations;
    }

    public void notifyDataSetChange() {
        this.fileFragment.notifyDataSetChange();
    }

    @Override
    public void displayTopListData(List<DirectoryNode> nodeList) {
        mTopTreeAdapter.setNodeList(nodeList);
        if (nodeList.size() != 1) {
            mLvTopTree.smoothScrollToPosition(nodeList.size());
        }
    }

    @Override
    public void displayLeftListData(List<DirectoryNode> nodeList) {
        mLeftTreeAdapter.setNodeList(nodeList);
    }

    @Override
    public void displayRightListData(DirectoryNode node, MatterPageInfo pageInfo) {
        fileFragment.setKnowledgePageInfo(node, pageInfo);
    }

    @Override
    public void showLeftHeadView(boolean show) {
        if (show) {
            mLeftTreeAdapter.setHeaderView(R.layout.item_matter_directory_head);
        }
        else {
            mLeftTreeAdapter.removeHeaderView();
        }
    }

    public DirectoryNode getDirectoryNode() {
        return mPresenter.getmCurrentNode();
    }
}
