package cn.flyrise.feep.collection.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-23 09:30
 */
public class FavoriteFolder {

	public String favoriteId;
	public String favoriteName;
	@SerializedName("edit") public boolean isEdit;

}
