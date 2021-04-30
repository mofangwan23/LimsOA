package cn.flyrise.feep.core.common.utils;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZYP
 * @since 2018-02-09 10:18
 */
public class EmojiUtil {

	private static Map<String, String> sEmojiMap = new HashMap<>(80);
	private static final String sRegex = "\\[\\(A:\\d+\\)\\]+";
	private static final Pattern sPattern = Pattern.compile(sRegex);

	static {
		sEmojiMap.put("[(A:0)]", "1F604");
		sEmojiMap.put("[(A:1)]", "1F603");
		sEmojiMap.put("[(A:2)]", "1F600");
		sEmojiMap.put("[(A:3)]", "1F60A");
		sEmojiMap.put("[(A:4)]", "263A");
		sEmojiMap.put("[(A:5)]", "1F609");
		sEmojiMap.put("[(A:6)]", "1F60D");
		sEmojiMap.put("[(A:7)]", "1F618");
		sEmojiMap.put("[(A:8)]", "1F61A");
		sEmojiMap.put("[(A:9)]", "1F617");
		sEmojiMap.put("[(A:10)]", "1F619");
		sEmojiMap.put("[(A:11)]", "1F61C");
		sEmojiMap.put("[(A:12)]", "1F61D");
		sEmojiMap.put("[(A:13)]", "1F61B");
		sEmojiMap.put("[(A:14)]", "1F633");
		sEmojiMap.put("[(A:15)]", "1F601");
		sEmojiMap.put("[(A:16)]", "1F614");
		sEmojiMap.put("[(A:17)]", "1F60C");
		sEmojiMap.put("[(A:18)]", "1F612");
		sEmojiMap.put("[(A:19)]", "1F61E");
		sEmojiMap.put("[(A:20)]", "1F623");
		sEmojiMap.put("[(A:21)]", "1F622");
		sEmojiMap.put("[(A:22)]", "1F602");
		sEmojiMap.put("[(A:23)]", "1F62D");
		sEmojiMap.put("[(A:24)]", "1F62A");
		sEmojiMap.put("[(A:25)]", "1F625");
		sEmojiMap.put("[(A:26)]", "1F630");
		sEmojiMap.put("[(A:27)]", "1F605");
		sEmojiMap.put("[(A:28)]", "1F613");
		sEmojiMap.put("[(A:29)]", "1F629");
		sEmojiMap.put("[(A:30)]", "1F62B");
		sEmojiMap.put("[(A:31)]", "1F628");
		sEmojiMap.put("[(A:32)]", "1F631");
		sEmojiMap.put("[(A:33)]", "1F620");
		sEmojiMap.put("[(A:34)]", "1F621");
		sEmojiMap.put("[(A:35)]", "1F624");
		sEmojiMap.put("[(A:36)]", "1F616");
		sEmojiMap.put("[(A:37)]", "1F606");
		sEmojiMap.put("[(A:38)]", "1F60B");
		sEmojiMap.put("[(A:39)]", "1F637");
		sEmojiMap.put("[(A:40)]", "1F60E");
		sEmojiMap.put("[(A:41)]", "1F634");
		sEmojiMap.put("[(A:42)]", "1F635");
		sEmojiMap.put("[(A:43)]", "1F632");
		sEmojiMap.put("[(A:44)]", "1F61F");
		sEmojiMap.put("[(A:45)]", "1F626");
		sEmojiMap.put("[(A:46)]", "1F627");
		sEmojiMap.put("[(A:47)]", "1F608");
		sEmojiMap.put("[(A:48)]", "1F47F");
		sEmojiMap.put("[(A:49)]", "1F62E");
		sEmojiMap.put("[(A:50)]", "1F62C");
		sEmojiMap.put("[(A:51)]", "1F610");
		sEmojiMap.put("[(A:52)]", "1F615");
		sEmojiMap.put("[(A:53)]", "1F62F");
		sEmojiMap.put("[(A:54)]", "1F636");
		sEmojiMap.put("[(A:55)]", "1F607");
		sEmojiMap.put("[(A:56)]", "1F60F");
		sEmojiMap.put("[(A:57)]", "1F611");
		sEmojiMap.put("[(A:58)]", "1F472");
		sEmojiMap.put("[(A:59)]", "1F473");
		sEmojiMap.put("[(A:60)]", "1F46E");
		sEmojiMap.put("[(A:61)]", "1F477");
		sEmojiMap.put("[(A:62)]", "1F482");
		sEmojiMap.put("[(A:63)]", "1F476");
		sEmojiMap.put("[(A:64)]", "1F471");
		sEmojiMap.put("[(A:65)]", "1F467");
		sEmojiMap.put("[(A:66)]", "1F468");
		sEmojiMap.put("[(A:67)]", "1F469");
		sEmojiMap.put("[(A:68)]", "1F474");
		sEmojiMap.put("[(A:69)]", "1F475");
		sEmojiMap.put("[(A:70)]", "1F466");
		sEmojiMap.put("[(A:71)]", "1F47C");
		sEmojiMap.put("[(A:72)]", "1F478");
		sEmojiMap.put("[(A:73)]", "1F63A");
		sEmojiMap.put("[(A:74)]", "1F638");
		sEmojiMap.put("[(A:75)]", "1F63B");
		sEmojiMap.put("[(A:76)]", "1F63D");
		sEmojiMap.put("[(A:77)]", "1F63C");
		sEmojiMap.put("[(A:78)]", "1F640");
		sEmojiMap.put("[(A:79)]", "1F63F");
	}

	public static String parseEmojiText(String emojiText) {
		if (TextUtils.isEmpty(emojiText)) {
			return emojiText;
		}

		StringBuffer buffer = new StringBuffer();
		Matcher matcher = sPattern.matcher(emojiText);
		while (matcher.find()) {
			String emojiString = sEmojiMap.get(matcher.group());
			if (TextUtils.isEmpty(emojiString)) {
				continue;
			}
			int unicode = Integer.parseInt(sEmojiMap.get(matcher.group()), 16);
			matcher.appendReplacement(buffer, new String(Character.toChars(unicode)));
		}
		matcher.appendTail(buffer);

		if (buffer.length() == 0) {     // 该文本中不含任何 emoji.
			return emojiText;
		}

		return buffer.toString();
	}

}
