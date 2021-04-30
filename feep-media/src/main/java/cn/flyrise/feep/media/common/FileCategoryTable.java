package cn.flyrise.feep.media.common;

import android.text.TextUtils;
import cn.flyrise.feep.media.R;
import java.util.HashMap;

/**
 * @author ZYP
 * @since 2017-10-25 14:50
 * 文件类型查询表：根据文件后缀、类型获取用于显示的图标
 */
public class FileCategoryTable {

	public static final int TYPE_UNKNOWN = 0;       // 未知
	public static final int TYPE_IMAGE = 1;         // 图片
	public static final int TYPE_AUDIO = 2;         // 音频
	public static final int TYPE_VIDEO = 3;         // 视频
	public static final int TYPE_WORD = 4;          // 文档
	public static final int TYPE_EXCEL = 5;         // 表格
	public static final int TYPE_PDF = 6;           // pdf
	public static final int TYPE_COLLABORATION = 7; // 关联事项-协同表单
	public static final int TYPE_MEETING = 8;       // 关联事项-会议
	public static final int TYPE_PPT = 9;           // PPT
	public static final int TYPE_TXT = 10;          // txt
	public static final int TYPE_ZIP = 11;          // zip

	private static final HashMap<String, Integer> sIconTable = new HashMap<>();

	static {
		sIconTable.put("dir", R.mipmap.ms_icon_thumbnail_dir);

		sIconTable.put("1", R.mipmap.ms_icon_thumbnail_img);
		sIconTable.put("jpg", R.mipmap.ms_icon_thumbnail_img);
		sIconTable.put("png", R.mipmap.ms_icon_thumbnail_img);
		sIconTable.put("gif", R.mipmap.ms_icon_thumbnail_img);
		sIconTable.put("jpeg", R.mipmap.ms_icon_thumbnail_img);
		sIconTable.put("bmp", R.mipmap.ms_icon_thumbnail_img);

		sIconTable.put("2", R.mipmap.ms_icon_thumbnail_music);
		sIconTable.put("mp3", R.mipmap.ms_icon_thumbnail_music);
		sIconTable.put("amr", R.mipmap.ms_icon_thumbnail_music);
		sIconTable.put("wav", R.mipmap.ms_icon_thumbnail_music);

		sIconTable.put("3", R.mipmap.ms_icon_thumbnail_video);
		sIconTable.put("flv", R.mipmap.ms_icon_thumbnail_video);
		sIconTable.put("rmvb", R.mipmap.ms_icon_thumbnail_video);
		sIconTable.put("wmv", R.mipmap.ms_icon_thumbnail_video);
		sIconTable.put("wav", R.mipmap.ms_icon_thumbnail_video);
		sIconTable.put("video", R.mipmap.ms_icon_thumbnail_video);
		sIconTable.put("mp4", R.mipmap.ms_icon_thumbnail_video);

		sIconTable.put("4", R.mipmap.ms_icon_thumbnail_word);
		sIconTable.put("doc", R.mipmap.ms_icon_thumbnail_word);
		sIconTable.put("docx", R.mipmap.ms_icon_thumbnail_word);
		sIconTable.put("wpd", R.mipmap.ms_icon_thumbnail_word);

		sIconTable.put("5", R.mipmap.ms_icon_thumbnail_excel);
		sIconTable.put("xls", R.mipmap.ms_icon_thumbnail_excel);
		sIconTable.put("xlsx", R.mipmap.ms_icon_thumbnail_excel);
		sIconTable.put("et", R.mipmap.ms_icon_thumbnail_excel);

		sIconTable.put("6", R.mipmap.ms_icon_thumbnail_pdf);
		sIconTable.put("pdf", R.mipmap.ms_icon_thumbnail_pdf);

		sIconTable.put("7", R.mipmap.ms_icon_thumbnail_link);
		sIconTable.put("8", R.mipmap.ms_icon_thumbnail_link);

		sIconTable.put("9", R.mipmap.ms_icon_thumbnail_ppt);
		sIconTable.put("ppt", R.mipmap.ms_icon_thumbnail_ppt);
		sIconTable.put("pptx", R.mipmap.ms_icon_thumbnail_ppt);
		sIconTable.put("dps", R.mipmap.ms_icon_thumbnail_ppt);

		sIconTable.put("10", R.mipmap.ms_icon_thumbnail_txt);
		sIconTable.put("txt", R.mipmap.ms_icon_thumbnail_txt);
		sIconTable.put("log", R.mipmap.ms_icon_thumbnail_txt);

		sIconTable.put("11", R.mipmap.ms_icon_thumbnail_zip);
		sIconTable.put("zip", R.mipmap.ms_icon_thumbnail_zip);
		sIconTable.put("rar", R.mipmap.ms_icon_thumbnail_zip);
		sIconTable.put("7z", R.mipmap.ms_icon_thumbnail_zip);
	}

	/**
	 * 根据文件的后缀、或类型获取用于显示的图标，
	 * suffix : dir、mp3、amr、wav...
	 * type : 1(图片), 2(音频), 3(视频), 4(文档), 5(表格), 6(pdf), 7(关联事项-协同表单), 8(关联事项-会议)
	 */
	public static int getIcon(String suffixOrType) {
		if (!TextUtils.isEmpty(suffixOrType)) {
			suffixOrType = suffixOrType.toLowerCase();
			if (sIconTable.containsKey(suffixOrType)) {
				return sIconTable.get(suffixOrType);
			}
		}
		return R.mipmap.ms_icon_thumbnail_unknow;
	}

	/**
	 * 根据文件路径、后缀获取文件指定的 type
	 */
	public static String getType(String pathOrSuffix) {
		if (TextUtils.isEmpty(pathOrSuffix)) {
			return Integer.toString(TYPE_UNKNOWN);
		}

		pathOrSuffix = pathOrSuffix.toLowerCase();
		if (pathOrSuffix.endsWith("jpg")
				|| pathOrSuffix.endsWith("png")
				|| pathOrSuffix.endsWith("gif")
				|| pathOrSuffix.endsWith("jpeg")
				|| pathOrSuffix.endsWith("bmp")) {
			return Integer.toString(TYPE_IMAGE);
		}

		if (pathOrSuffix.endsWith("mp3")
				|| pathOrSuffix.endsWith("amr")
				|| pathOrSuffix.endsWith("wav")) {
			return Integer.toString(TYPE_AUDIO);
		}

		if (pathOrSuffix.endsWith("flv")
				|| pathOrSuffix.endsWith("rmvb")
				|| pathOrSuffix.endsWith("wmv")
				|| pathOrSuffix.endsWith("mp4")) {
			return Integer.toString(TYPE_VIDEO);
		}

		if (pathOrSuffix.endsWith("doc")
				|| pathOrSuffix.endsWith("docx")
				|| pathOrSuffix.endsWith("wpd")) {
			return Integer.toString(TYPE_WORD);
		}

		if (pathOrSuffix.endsWith("xls")
				|| pathOrSuffix.endsWith("xlsx")
				|| pathOrSuffix.endsWith("et")) {
			return Integer.toString(TYPE_EXCEL);
		}

		if (pathOrSuffix.endsWith("pdf")) {
			return Integer.toString(TYPE_PDF);
		}

		if (pathOrSuffix.endsWith("ppt")
				|| pathOrSuffix.endsWith("pptx")
				|| pathOrSuffix.endsWith("dps")) {
			return Integer.toString(TYPE_PPT);
		}

		if (pathOrSuffix.endsWith("txt")
				|| pathOrSuffix.endsWith("log")) {
			return Integer.toString(TYPE_TXT);
		}

		if (pathOrSuffix.endsWith("zip")
				|| pathOrSuffix.endsWith("rar")
				|| pathOrSuffix.endsWith("7z")) {
			return Integer.toString(TYPE_ZIP);
		}

		return Integer.toString(TYPE_UNKNOWN);
	}

}
