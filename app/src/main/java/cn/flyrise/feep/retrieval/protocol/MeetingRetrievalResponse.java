package cn.flyrise.feep.retrieval.protocol;

import cn.flyrise.feep.core.network.request.ResponseContent;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-09 20:52
 */
public class MeetingRetrievalResponse extends ResponseContent {

	public SearchResult data;

	public class SearchResult {

		@SerializedName("numFound") public int maxCount;
		@SerializedName("doc") public List<DRMeeting> results;
	}

}
