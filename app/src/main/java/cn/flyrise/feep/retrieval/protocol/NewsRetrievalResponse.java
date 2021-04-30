package cn.flyrise.feep.retrieval.protocol;

import cn.flyrise.feep.core.network.request.ResponseContent;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-09 17:57
 */
public class NewsRetrievalResponse extends ResponseContent {

	public SearchResult data;

	public class SearchResult {

		@SerializedName("numFound") public int maxCount;
		@SerializedName("doc") public List<DRNews> results;
	}

}
