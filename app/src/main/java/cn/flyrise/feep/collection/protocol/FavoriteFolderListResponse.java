package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.collection.bean.FavoriteFolder;
import cn.flyrise.feep.core.network.request.ResponseContent;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-23 09:29
 */
public class FavoriteFolderListResponse extends ResponseContent {

	@SerializedName("data") public List<FavoriteFolder> folders;

}
