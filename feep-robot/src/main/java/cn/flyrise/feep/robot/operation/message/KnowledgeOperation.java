package cn.flyrise.feep.robot.operation.message;

import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.module.RobotModuleItem;
import cn.squirtlez.frouter.FRouter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017-6-29.
 * 知识管理
 */

public class KnowledgeOperation extends BaseOperation {

	@Override
	public void open() {
		openMessage();
	}

	@Override
	public void search() {
		knowledgeSerch();
	}

	//搜索文档初始化
	private void knowledgeSerch() {
		if (mOperationModule.grammarResultListener != null) {
			List<RobotModuleItem> lists = new LinkedList<>();
			lists.add(getModulleDetail(2, mContext.getString(R.string.know_person_folder)));
			lists.add(getModulleDetail(3, mContext.getString(R.string.know_unit_folder)));
			lists.add(getModulleDetail(1, mContext.getString(R.string.know_group_folder)));
			mOperationModule.grammarResultListener.onGrammarResultItems(lists);
		}
	}

	public void knowledgeIntent(int searchType) {
		FRouter.build(mContext, "/knowledge/search")
				.withInt("EXTRA_FOLDERTYPES", searchType).go();
	}

}
