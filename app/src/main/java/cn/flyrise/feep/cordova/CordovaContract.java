package cn.flyrise.feep.cordova;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import java.util.List;
import org.apache.cordova.CordovaWebView;

/**
 * Created by Administrator on 2016-12-13.
 */

public interface CordovaContract {

	interface CordovaPresenters {

		int ADD_ATTACHMENT_REQUEST_CODE = 100;

		int ADD_PERSON_REQUEST_CODE = 200;

		int INTENT_TO_MEETINGBOARD_REQUEST_CODE = 300;

		int RECORD_RESULT = 20;

		int PHOTO_RESULT = 23;

		int SCANNING_QR_CODE = 24;//二维码扫描

		String NOTIFICATION_READY = "NotificationReady";
		String POST_NOTIFICATION_WITH_ID = "PostNotificationWithId";
		String HOST_CODE = "-";
		String COLON_CODE = ":";
		String NOTIFICATION_BEFORE = "jsBridge.popNotificationObject(";
		String NOTIFICATION_LAST = ")";
		String JAVASCRIPT = "javascript";
		String JSON_USER_INFO = "userInfo";
		String JSON_CONTROL_DEFAULT_DATA = "ControlDefaultData";
		String JSON_IQ = "iq";
		String JSON_QUERY = "query";
		String BRIDGE_BEFORE = "jsBridge.trigger('SetWebHTMLEditorContent',";
		String BRIDGE_LAST = ")";
		String TO_JSON_BEFORE = "{\"OcToJs_JSON\":";
		String TO_JSON_LAST = "}";

		int IS_RECORD_INTENT = 1001020;//从录音界面退出
		int IS_PHOTO_INTENT = 10103;//从拍照界面退出
		int IS_WRITTING_COMBO = 10104;//手写签批

		void sendHandlerMessage(int what);

		void sendToJavascript(Object sendData);

		void onActivityResult(int requestCode, int resultCode, Intent intent);

		void referencePicker();

		void showPickerView();

		void MeasuredHeight(MotionEvent event);

		boolean uploadFile(int type);//普通附件上传0

		void addAttachment();

		void clickCommonWord();

		void downLoadAttachment();

		List<String> getSelectedAttachments();
	}

	interface CordovaView {

		int MSG_PROGRESS_UPDATE = 0x110;

		String ONSCROLLCHANGED = "onScrollChanged";

		String ONPAGEFINISHED = "onPageFinished";

		String NEW_FORM_CHOOSE_NODE_DATA = "NewFormChooseNodeData";

		String ASSOCIATES_RECORD = "ASSOCIATES_RECORD";

		Handler getHandler();

		CordovaWebView getWebView();

		FragmentActivity getCordovaContext();

		Context getContexts();

		void onPostExecute(JSControlInfo controlInfo);

		void openRecord();//录音

		void openPhoto();//拍照

		void openWrittingCombo();//手写签批

		void clickPersonChoose(JSControlInfo controlInfo);

		void clickAddAttachment();

		void clickMeetingRoom(JSControlInfo controlInfo);

		void clickSendButton(JSControlInfo controlInfo);

		void onPageFinished();//网页加载完后调用此方法

		void setViewVisible(boolean isLayoutVisible);

		void clickType(int controlType);

		void playAudio(Attachment attachment, String path);

		void openAttachment(Intent intent);
	}


}
