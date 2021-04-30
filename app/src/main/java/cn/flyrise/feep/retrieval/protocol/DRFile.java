package cn.flyrise.feep.retrieval.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 17:38
 */
public class DRFile {

	/**
	 * "id": "6774156E-1F94-D154-695B-EB40111F1B06",
	 * "title": "孙禄堂《论拳术内外家之别》.doc",
	 * "remark": "集团文件夹/飞企集团资料/研发流程/孙禄堂《论拳术内外家之别》.doc",
	 * "fileattr": ".doc",
	 * "editUser": "吴恺",
	 * "uploadUser": "5566"
	 */

	public String id;
	public String title;
	public String remark;
	public String fileattr;
	@SerializedName("uploadUser") public String userId;       // userId
	@SerializedName("editUser") public String username;       // username

}
