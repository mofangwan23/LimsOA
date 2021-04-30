package cn.flyrise.feep.collaboration.model;

import java.util.List;

import cn.flyrise.android.protocol.model.Flow;

/**
 * Created by klc on 2017/6/20.
 */

public class CollaborationHoldData {

	public String content;

	public List<String> attachmentPath;

	public Flow flow;

	public boolean hideOpinion;

	public boolean trace;

	public boolean isAddSigned;

	public boolean isWait;

	public boolean isBackToStartNode;

	public boolean isReturnToThisNodeAfterHandle ;
}
