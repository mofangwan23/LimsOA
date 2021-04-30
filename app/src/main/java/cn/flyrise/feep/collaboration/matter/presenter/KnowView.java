package cn.flyrise.feep.collaboration.matter.presenter;

import java.util.List;

import cn.flyrise.feep.collaboration.matter.model.MatterPageInfo;
import cn.flyrise.feep.collaboration.matter.model.DirectoryNode;

/**
 * Created by klc on 2017/5/18.
 */

public interface KnowView {

    void displayTopListData(List<DirectoryNode> nodeList);

    void displayLeftListData(List<DirectoryNode> nodeList);

    void displayRightListData(DirectoryNode node,MatterPageInfo pageInfo);

    void showLeftHeadView(boolean show);
}
