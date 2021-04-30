package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-05-23 09:28
 */
public class FavoriteFolderRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "CollectRequest";
	}

	public String method;
	public String favoriteId;   // 收藏夹 Id
	public String name;         // 收藏夹 Name

	// 请求收藏夹列表
	public static FavoriteFolderRequest requestFavoriteFolderList() {
		FavoriteFolderRequest request = new FavoriteFolderRequest();
		request.method = "CollectFolderList";
		return request;
	}

	// 请求创建收藏夹
	public static FavoriteFolderRequest requestCreateFolder(String folderName) {
		FavoriteFolderRequest request = new FavoriteFolderRequest();
		request.method = "CollectFolderAdd";
		request.name = folderName;
		return request;
	}

	// 请求修改收藏夹
	public static FavoriteFolderRequest requestUpdateFolder(String favoriteId, String favoriteName) {
		FavoriteFolderRequest request = new FavoriteFolderRequest();
		request.method = "CollectFolderEdit";
		request.favoriteId = favoriteId;
		request.name = favoriteName;
		return request;
	}

	// 请求删除收藏夹
	public static FavoriteFolderRequest requestDeleteFolder(String favoriteId) {
		FavoriteFolderRequest request = new FavoriteFolderRequest();
		request.method = "CollectFolderDel";
		request.favoriteId = favoriteId;
		return request;
	}


}
