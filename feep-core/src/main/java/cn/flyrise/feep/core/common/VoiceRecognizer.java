package cn.flyrise.feep.core.common;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-15:06.
 */
public class VoiceRecognizer {

	public interface MscRecognizerListener {

		void onResult(String text);
	}
}
