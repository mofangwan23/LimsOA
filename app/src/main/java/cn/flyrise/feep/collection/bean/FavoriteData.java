package cn.flyrise.feep.collection.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-23 09:46
 */
public class FavoriteData {

	/**
	 * "page":1,
	 * "size":10,
	 * "totalNum":1,
	 * "totalPage":1,
	 * "hasNextPage":false,
	 * "list":[]
	 */

	public boolean hasNextPage;
	@SerializedName("list") public List<Favorite> favorites;

}
