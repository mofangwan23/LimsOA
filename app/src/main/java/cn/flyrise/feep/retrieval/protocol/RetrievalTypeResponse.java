package cn.flyrise.feep.retrieval.protocol;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-07 14:52
 */
public class RetrievalTypeResponse extends ResponseContent {

	@SerializedName("data") public List<RetrievalType> searchResults;

}
