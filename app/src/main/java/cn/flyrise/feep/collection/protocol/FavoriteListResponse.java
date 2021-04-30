package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.collection.bean.FavoriteData;
import cn.flyrise.feep.core.network.request.ResponseContent;
import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-23 09:45
 */
public class FavoriteListResponse extends ResponseContent {

	@SerializedName("data") public FavoriteData result;

}
