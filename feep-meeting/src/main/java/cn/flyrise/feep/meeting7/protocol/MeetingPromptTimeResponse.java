package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.meeting7.ui.bean.PromptTime;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MeetingPromptTimeResponse extends ResponseContent {

	@SerializedName("referenceItems") public List<PromptTime> promptTimes;

}
