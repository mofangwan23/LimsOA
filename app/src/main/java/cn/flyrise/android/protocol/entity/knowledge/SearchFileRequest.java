package cn.flyrise.android.protocol.entity.knowledge;


import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by k on 2016/9/9.
 */
public class SearchFileRequest extends KnowledgeBaseRequest {

	private String param1;
	private String param2;
	private String param3;
	private String param4;

	public SearchFileRequest(String key, int mCurrentPage, int pageSize, int folderType) {
		count = "4";
		obj = "remoteIndexSearch";
		method = "listQueryResult";
		param1 = key;
		param2 = String.valueOf(mCurrentPage);
		param3 = String.valueOf(pageSize);
		if (folderType == KnowKeyValue.FOLDERTYPE_PERSON)
			param4 = "个人文件夹";
		else if (folderType == KnowKeyValue.FOLDERTYPE_UNIT)
			param4 = "单位文件夹";
		else if (folderType == KnowKeyValue.FOLDERTYPE_GROUP)
			param4 = "集团文件夹";
		else
			param4 = folderType + "";
	}
}
