package cn.flyrise.feep.core.network.uploader;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.entry.UploadRequestBody;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListener;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ZYP
 * @since 2016-09-07 15:49
 */
public class UploadTask implements Runnable {

	public static final String UPLOAD_PATH = "/servlet/uploadAttachmentServlet?";
	public static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");

	private final String mUploadUrl;
	private final FileRequest mFileRequest;
	private final List<String> mUploadFiles;
	private final OkHttpClient mOkHttpClient;
	private final OnProgressUpdateListener mProgressUpdateListener;
	private final ResponseCallback<? extends ResponseContent> mResponseCallback;
	private Call uploadCall;

	private UploadTask(Builder builder) {
		this.mFileRequest = builder.fileRequest;
		this.mUploadFiles = builder.uploadFiles;
		this.mOkHttpClient = builder.okHttpClient;
		this.mUploadUrl = builder.host + UPLOAD_PATH;
		this.mResponseCallback = builder.responseCallback;
		this.mProgressUpdateListener = builder.onProgressUpdateListener;
	}

	@Override
	public void run() {
		MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
		for (int i = 0; i < mUploadFiles.size(); i++) {
			String filePath = mUploadFiles.get(i);
			File file = new File(filePath);
			if (file.exists()) {
				String fileField = "file[" + i + "]";
				String fileName = file.getName();
				builder.addFormDataPart(fileField, fileName, RequestBody.create(MEDIA_TYPE, file));
			}
		}

		HashMap<String, String> params = buildUploadParams(mFileRequest.getFileContent());
		if (params != null) {
			for (String key : params.keySet()) {
				String value = params.get(key);
				builder.addFormDataPart(key, value);
			}
		}

		RequestBody requestBody = new UploadRequestBody(builder.build(), mProgressUpdateListener);
		Request request = new Request.Builder()
				.url(mUploadUrl)
				.post(requestBody)
				.addHeader("User-Agent", CoreZygote.getUserAgent())
				.build();
		uploadCall = this.mOkHttpClient.newCall(request);
		uploadCall.enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				FELog.e("UploadTask onFailure : " + e.getMessage());
				if (mProgressUpdateListener != null) {
					mProgressUpdateListener.onFailed(e);
				}
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (mProgressUpdateListener != null) {
					ResponseBody body = response.body();
					if (body != null) {
						mProgressUpdateListener.onSuccess(body.string());
					}
					else {
						mProgressUpdateListener.onFailed(new NullPointerException("The response body is null."));
					}
				}

				if (mFileRequest.getRequestContent() != null) {
					FEHttpClient.getInstance().post(mFileRequest.getRequestContent(), mResponseCallback);
				}
			}
		});
	}

	public void cancelTast() {
		if (uploadCall != null) {
			uploadCall.cancel();
		}
	}

	private HashMap<String, String> buildUploadParams(FileRequestContent fileContent) {
		try {
			JSONObject json = new JSONObject();
			JSONObject iqJsonObject = new JSONObject();
			JSONObject queryJsonObject = new JSONObject();
			if (fileContent.getAttachmentGUID() != null) {
				queryJsonObject.put("attachmentGUID", fileContent.getAttachmentGUID());
			}

			if (fileContent.getUpdateType() != null) {
				queryJsonObject.put("UpdateType", fileContent.getUpdateType());
			}

			if (!TextUtils.isEmpty(fileContent.getAudioTime())) {
				queryJsonObject.put("audioTime", fileContent.getAudioTime());
			}

			if (!TextUtils.isEmpty(fileContent.getCopyFileIds())) {
				queryJsonObject.put("copyFileIDs", fileContent.getCopyFileIds());
			}

			if (fileContent.getDeleteFileIds() != null && fileContent.getDeleteFileIds().size() > 0) {
				final JSONArray attachmentsProperty = new JSONArray();
				queryJsonObject.put("attachmentsDelete", attachmentsProperty);
				for (final String id : fileContent.getDeleteFileIds()) {
					final JSONObject attachmentItem = new JSONObject();
					attachmentItem.put("id", id);
					attachmentsProperty.put(attachmentItem);
				}
			}

			Map valueMap = fileContent.getValueMap();
			if (valueMap != null) {
				Iterator<Map.Entry<String, String>> iterator = valueMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry entry = iterator.next();
					String key = String.valueOf(entry.getKey());
					String value = String.valueOf(entry.getValue());
					queryJsonObject.put(key, value);
				}
			}

			iqJsonObject.put("query", queryJsonObject);
			iqJsonObject.put("namespace", "AttachmentUpdateRequest");
			json.put("iq", iqJsonObject);

			HashMap<String, String> params = new HashMap<>();
			params.put("json", json.toString());
			return params;
		} catch (Exception ex) {
			// Why ...
		}
		return null;
	}

	public static class Builder {

		private String host;
		private FileRequest fileRequest;
		private List<String> uploadFiles;
		private OkHttpClient okHttpClient;
		private ResponseCallback<? extends ResponseContent> responseCallback;
		private OnProgressUpdateListener onProgressUpdateListener;

		public Builder setHost(String host) {
			this.host = host;
			return this;
		}

		public Builder setFileRequest(FileRequest fileRequest) {
			this.fileRequest = fileRequest;
			return this;
		}

		public Builder setUploadFiles(List<String> uploadFiles) {
			this.uploadFiles = uploadFiles;
			return this;
		}

		public Builder setResponseCallback(ResponseCallback responseCallback) {
			this.responseCallback = responseCallback;
			return this;
		}

		public Builder setOnProgressUpdateListener(OnProgressUpdateListener onProgressUpdateListener) {
			this.onProgressUpdateListener = onProgressUpdateListener;
			return this;
		}

		public Builder setOkHttpClient(OkHttpClient okHttpClient) {
			this.okHttpClient = okHttpClient;
			return this;
		}

		public UploadTask build() {
			return new UploadTask(this);
		}
	}
}
