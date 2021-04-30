package cn.flyrise.android.protocol.entity.schedule;

import java.util.List;

import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by yj on 2016/7/19.
 */
public class PromptResponse extends ResponseContent {

    public List<ReferenceItem> referenceItems;

}
