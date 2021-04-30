package cn.flyrise.android.protocol.entity;

import cn.flyrise.android.protocol.model.CommonGroup;
import cn.flyrise.feep.core.network.request.ResponseContent;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-03-23 15:59
 */
public class CommonGroupResponse extends ResponseContent {

	public List<CommonGroup> results;

}
