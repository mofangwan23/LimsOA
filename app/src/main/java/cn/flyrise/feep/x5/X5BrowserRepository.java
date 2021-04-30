package cn.flyrise.feep.x5;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.ReferenceItemsRequest;
import cn.flyrise.android.protocol.entity.ReferenceItemsResponse;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.CommonWordsActivity;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.commonality.bean.JsSendServiceItem;
import cn.flyrise.feep.cordova.utils.SendContactsJs;
import cn.flyrise.feep.cordova.utils.SendRecordJs;
import cn.flyrise.feep.core.common.X.JSControlType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.entry.RecordItem;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import rx.Observable;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 14:41
 */
public final class X5BrowserRepository {

	private final X5BrowserDelegate delegate;
	private final Handler handler = new Handler(Looper.getMainLooper());

	public X5BrowserRepository(X5BrowserDelegate delegate) {
		this.delegate = delegate;
	}

	public Observable<String> updateFile(List<String> attachmentFiles, String recordTime, String attachmentGuid) {
		if (CommonUtil.isEmptyList(attachmentFiles)) return null;
		return Observable.unsafeCreate(subscriber -> {

			final Context context = delegate.getContext();
			JSControlInfo jsControlInfo = delegate.getJsControlInfo();

			FileRequestContent fileRequestContent = new FileRequestContent();
			fileRequestContent.setAttachmentGUID(attachmentGuid);
			fileRequestContent.setFiles(attachmentFiles);

			if (!TextUtils.isEmpty(recordTime)) fileRequestContent.setAudioTime(recordTime);
//			if (!LoadingHint.isLoading()) LoadingHint.show(context);
			if (jsControlInfo != null && !CommonUtil.isEmptyList(jsControlInfo.sendService)) {
				Map<String, String> serviceMap = new HashMap<>();
				for (JsSendServiceItem serviceItem : jsControlInfo.sendService) {
					serviceMap.put(serviceItem.name, serviceItem.value);
				}
				fileRequestContent.setValueMap(serviceMap);
			}

			FileRequest fileRequest = new FileRequest();
			fileRequest.setFileContent(fileRequestContent);
			new UploadManager(context)
					.fileRequest(fileRequest)
					.progressUpdateListener(new OnProgressUpdateListenerImpl() {
						@Override public void onPreExecute() {
							handler.post(() -> LoadingHint.show(context));
						}

						@Override public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
							final int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
							handler.post(() -> LoadingHint.showProgress(progress));
						}

						@Override public void onPostExecute(String jsonBody) {
							handler.post(() -> LoadingHint.hide());
							subscriber.onNext(jsonBody);
						}

						@Override public void onFailExecute(Throwable ex) {
							LoadingHint.hide();
						}
					})
					.execute();
		});
	}

	public Observable<SendRecordJs> mapFromObservable(Observable<String> jsonObservable, JSControlInfo jsControlInfo,
			final String attachemntGuid) {
		return jsonObservable.map(jsonString -> {
			SendRecordJs result = new SendRecordJs();
			result.setUiControlType(jsControlInfo.getUiControlTypeValue());
			result.setUiControlId(jsControlInfo.getUiControlId());

			try {
				JSONObject properties = new JSONObject(jsonString);
				JSONObject iq = null;
				String query = "";

				if (properties.has("iq")) iq = properties.getJSONObject("iq");
				if (iq != null && iq.has("query")) query = iq.get("query").toString();

				List<RecordItem> attaItems = null;
				if (!TextUtils.isEmpty(query)) {
					CommonResponse commonResponse = GsonUtil.getInstance().fromJson(query, CommonResponse.class);
					if (commonResponse != null) attaItems = commonResponse.getAttaItems();
				}

				Map<String, String> serviceMap = new HashMap<>();
				if (attaItems != null) {
					for (RecordItem item : attaItems) {
						serviceMap.put("guid", item.getGuid());
						serviceMap.put("master_key", item.getMaster_key());
						serviceMap.put("time", item.getTime());
					}
				}
				if (!CommonUtil.isEmptyList(jsControlInfo.sendService)) {
					for (JsSendServiceItem serviceItem : jsControlInfo.sendService) {
						serviceMap.put(serviceItem.name, serviceItem.value);
					}
				}

				if (!serviceMap.containsKey("guid")) {
					serviceMap.put("guid", attachemntGuid);
				}

				List<Map<String, String>> services = new ArrayList<>();
				services.add(serviceMap);
				result.setReferenceItems(services);
			} catch (Exception exp) {
				exp.printStackTrace();
			}
			return result;
		});
	}

	public Observable<SendContactsJs> queryContacts(final Context context, final Uri contactURI) {
		return Observable.unsafeCreate(subscriber -> {
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = resolver.query(contactURI, null, null, null, null);
			cursor.moveToFirst();

			String username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

			Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
			List<String> phones = new ArrayList<>();
			while (phoneCursor != null && phoneCursor.moveToNext()) {
				String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				phones.add(phoneNumber);
			}

			SendContactsJs contactsJs = new SendContactsJs();
			contactsJs.setUiControlType(JSControlType.Contacts + "");
			contactsJs.setName(username);
			contactsJs.setPhones(phones);
			subscriber.onNext(contactsJs);
		});
	}

	public Observable<String[]> queryCommonWords(Context context) {
		return Observable.create(subscriber -> {
			FEApplication application = (FEApplication) context.getApplicationContext();
			String[] commonWords = application.getCommonWords();
			if (commonWords != null && commonWords.length > 0) {
				subscriber.onNext(CommonWordsActivity.convertCommonWord(commonWords));
				return;
			}

			ReferenceItemsRequest request = new ReferenceItemsRequest();
			request.setRequestType(ReferenceItemsRequest.TYPE_COMMON_WORDS);
			FEHttpClient.getInstance().post(request, new ResponseCallback<ReferenceItemsResponse>() {
				@Override public void onCompleted(ReferenceItemsResponse response) {
					String errorCode = response.getErrorCode();
					if (TextUtils.equals(errorCode, "-98")) {
						application.setCommonWords(context.getResources().getStringArray(R.array.words));
					}
					else {
						application.setCommonWords(CommonWordsActivity.convertCommonWords(response.getItems()));
					}
					subscriber.onNext(CommonWordsActivity.convertCommonWord(application.getCommonWords()));
				}

				@Override public void onFailure(RepositoryException repository) {
					subscriber.onError(repository.exception());
				}
			});
		});
	}

}
