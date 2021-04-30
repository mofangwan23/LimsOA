//
// FormDataProvider.java
// feep
//
// Created by ZhongYJ on 2012-03-02.
// Copyright 2011 flyrise. All rights reserved.
//

package cn.flyrise.feep.form.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.FormExportRequest;
import cn.flyrise.android.protocol.entity.FormExportResponse;
import cn.flyrise.android.protocol.entity.FormNodeRequest;
import cn.flyrise.android.protocol.entity.FormNodeResponse;
import cn.flyrise.android.protocol.entity.FormSendDoRequest;
import cn.flyrise.android.protocol.entity.FormSubnodeRequest;
import cn.flyrise.android.protocol.entity.FormSubnodeResponse;
import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.search.ApprovalSearchActivity;
import cn.flyrise.feep.commonality.ApprovalCollaborationListActivity;
import cn.flyrise.feep.commonality.MessageSearchActivity;
import cn.flyrise.feep.commonality.util.CachePath;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.X.FormExitType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.event.EventMessageDisposeSuccess;
import cn.flyrise.feep.form.FormListActivity;
import cn.flyrise.feep.form.been.FormExitToNodeItem;
import cn.flyrise.feep.form.been.FormNodeToSubNode;
import cn.flyrise.feep.form.been.FormSubNodeInfo;
import cn.flyrise.feep.main.message.other.SystemMessageActivity;
import cn.flyrise.feep.main.message.task.TaskMessageActivity;
import cn.flyrise.feep.meeting.old.MeetingListActivity;
import cn.flyrise.feep.workplan7.Plan6MainActivity;
import cn.flyrise.feep.workplan7.Plan7MainActivity;
import com.hyphenate.chatui.ui.ChatActivity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FormDataProvider {

	public static final int EXIT_HANDLER_WHAT = 10;
	public static final int NODE_HANDLER_WHAT = 20;
	public static final int PERSON_HANDLER_WHAT = 30;
	private static final int REQUEST_FAIL = 40;

	private final Handler mHandler;
	private final Context mContext;
	private final String mId;

	/**
	 * 存放所有加载的数据
	 */
	private final ArrayList<FormExitToNodeItem> formExitToNodeItems;
	private List<ReferenceItem> mExitItems;
	private int exitRequestType;

	public boolean isAllowSend = true;

	public FormDataProvider(Context context, String id, Handler handler) {
		mContext = context;
		mId = id;
		mHandler = handler;
		formExitToNodeItems = new ArrayList<>();
		mExitItems = new ArrayList<>();
	}

	/**
	 * 请求表单送办出口
	 */
	public void requestExport(int requestType) {
		this.exitRequestType = requestType;
		final FormExportRequest request = new FormExportRequest();
		request.setRequestType(requestType);
		request.setId(mId);
		FEHttpClient.getInstance().post(request, new ResponseCallback<FormExportResponse>(this) {
			@Override
			public void onCompleted(FormExportResponse formExportResponse) {
				try {
					mExitItems = formExportResponse.getReferenceItems();
					final int len = mExitItems.size();
					if (len == 0) {
						hideLoadActivity();
					}
					formExitToNodeItems.clear();
					for (final ReferenceItem referenceItem : mExitItems) {
						final FormExitToNodeItem formExitToNodeItem = new FormExitToNodeItem();
						formExitToNodeItem.setExitNodItem(referenceItem);
						formExitToNodeItems.add(formExitToNodeItem);
					}
					mHandler.sendEmptyMessage(EXIT_HANDLER_WHAT);
				} catch (final Exception e) {
					mHandler.sendEmptyMessage(REQUEST_FAIL);
					e.printStackTrace();
					hideLoadActivity();
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mHandler.sendEmptyMessage(REQUEST_FAIL);
			}
		});
	}

	/**
	 * 判断RequiredData是不是json格式数据 by 罗展健
	 */
	private boolean isJsonObject(String RequiredData) {

		if (RequiredData == null || "".equals(RequiredData)) {
			return false;
		}
		else {
			try {
				new JSONObject(RequiredData);
				return true;
			} catch (final JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * 请求表单送办节点
	 */
	public void requsetNode(int requestType, String chukouID, String RequiredData) {
		final FormNodeRequest request = new FormNodeRequest();
		request.setChukouID(chukouID);
		request.setId(mId);
		request.setRequestType(requestType);
		if (isJsonObject(RequiredData)) {// 如果是json格式，说明是条件选择
			request.setRequiredData(RequiredData);
		}
		FEHttpClient.getInstance().post(request, new ResponseCallback<FormNodeResponse>(this) {
			@Override
			public void onCompleted(FormNodeResponse responseContent) {
				try {
					final List<FormNodeItem> nodeItems = responseContent.getNodes();
					final String key = responseContent.getChukouID();
					final int requestType = responseContent.getRequestType();
					if (requestType == X.FormRequestType.Return) {
						formExitToNodeItems.clear();
						final FormExitToNodeItem exitToNodeItem = new FormExitToNodeItem();
						exitToNodeItem.setExitNodItem(new ReferenceItem());
						formExitToNodeItems.add(exitToNodeItem);
					}
					for (int i = 0; i < formExitToNodeItems.size(); i++) {
						final FormExitToNodeItem exitToNodeItem = formExitToNodeItems.get(i);
						if ((requestType == X.FormRequestType.SendDo && key != null && !"".equals(key) && key
								.equals(exitToNodeItem.getExitNodItem().getKey()))
								|| requestType == X.FormRequestType.Return) {
							exitToNodeItem.setFormNodeResponse(responseContent);
							for (final FormNodeItem nodeItem : nodeItems) {
								final FormNodeToSubNode nodeToSubNode = new FormNodeToSubNode();
								nodeToSubNode.setFormNodeItem(nodeItem);
								exitToNodeItem.addNodeItem(nodeToSubNode);
							}
						}
					}
					if ((nodeItems == null || nodeItems.size() == 0)) {
						hideLoadActivity();
					}
				} catch (final Exception e) {
					e.printStackTrace();
					hideLoadActivity();
				}
				mHandler.sendEmptyMessage(NODE_HANDLER_WHAT);
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mHandler.sendEmptyMessage(REQUEST_FAIL);
			}
		});
	}


	/**
	 * 请求表单送办子节点
	 */
	public void requestSubnode(int requestType, int type, String nodeId, String searchText) {
		if (requestType == X.FormRequestType.NewForm) {
			requestType = X.FormRequestType.SendDo;
		}
		final FormSubnodeRequest request = new FormSubnodeRequest();
		request.setId(nodeId);
		request.setRequestType(requestType);
		request.setType(type);
		request.setWfInfoID(mId);
		FEHttpClient.getInstance().post(request, new ResponseCallback<FormSubnodeResponse>(this) {
			@Override
			public void onCompleted(FormSubnodeResponse responseContent) {
				try {
					final String nodeId = responseContent.getId();
					Log.i("dd-->", nodeId);
					final List<ReferenceItem> references = responseContent.getItems();
					for (final FormExitToNodeItem exitToNodeItem : formExitToNodeItems) {// 获取节点到人员的数据模型
						final List<FormNodeToSubNode> nodeItems = exitToNodeItem.getNodeItems();
						if (nodeItems != null) {
							for (final FormNodeToSubNode nodeToSubNode : nodeItems) {// 遍历“节点到人员”数据
								final int subNodeType = responseContent.getType();
								final ArrayList<FormSubNodeInfo> subNodeInfo = transformSubNodes(nodeToSubNode, subNodeType, references);

								List<FormSubNodeInfo> filterNodeInfo = null;
								if (TextUtils.isEmpty(searchText)) {
									filterNodeInfo = subNodeInfo;
								}
								else {
									filterNodeInfo = new ArrayList<>();
									for (FormSubNodeInfo nodeInfo : subNodeInfo) {
										ReferenceItem referenceItem = nodeInfo.getReferenceItem();
										final String value = referenceItem.getValue();
										if (value.contains(searchText)) {
											filterNodeInfo.add(nodeInfo);
										}
									}
								}

								// 如果已经加载的数据中的节点id与新加载的数据的节点id相等的话
								if (nodeId != null && nodeId.equals(nodeToSubNode.getFormNodeItem().getId())) {
									if (subNodeType == AddressBookType.Staff) {
										nodeToSubNode.setPersonSubNodes(filterNodeInfo);
									}
									else {
										nodeToSubNode.setPositionSubNodes(filterNodeInfo);
									}
								}
							}
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
					hideLoadActivity();
				}
				mHandler.sendEmptyMessage(PERSON_HANDLER_WHAT);
				hideLoadActivity();
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mHandler.sendEmptyMessage(REQUEST_FAIL);
			}
		});
	}

	/*---------------------------------------表单送办提交------------------------------------------*/

	public void submit(FormSendDoRequest request, List<String> attachemnts) {
		if (request == null) {
			isAllowSend = true;
			return;
		}
		if (CommonUtil.isEmptyList(attachemnts)) {
			submit(request);
		}
		else {
			final String guid = UUID.randomUUID().toString();
			request.attachment = guid;
			final FileRequest fileRequest = new FileRequest();
			final FileRequestContent filerequestcontent = new FileRequestContent();
			filerequestcontent.setAttachmentGUID(guid);
			filerequestcontent.setFiles(attachemnts);
			filerequestcontent.setDeleteFileIds(null);
			fileRequest.setFileContent(filerequestcontent);
			new UploadManager(mContext)
					.fileRequest(fileRequest)//只上传附件，不上传表单数据
					.progressUpdateListener(new OnProgressUpdateListenerImpl() {
						@Override
						public void onPreExecute() {//附件开始上次回调
							LoadingHint.show(mContext);
						}

						@Override
						public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
							int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
							LoadingHint.showProgress(progress);
						}

						@Override
						public void onPostExecute(String jsonBody) {//附件上传成功回调
							super.onPostExecute(jsonBody);
							LoadingHint.hide();
							submit(request);
						}

						@Override
						public void onFailExecute(Throwable ex) {//附件上传失败回调
							super.onFailExecute(ex);
							LoadingHint.hide();
							submit(request);
						}
					})
					.execute();
		}
	}

	//表单送办提交
	public void submit(FormSendDoRequest request) {
		final String writtenGUID = request.getAttachmentGUID();
		if (null == writtenGUID || "".equals(writtenGUID)) { // 意见是输入文本不是手写 直接提交
			LoadingHint.show(mContext);
			FEHttpClient.getInstance().post(request, new ResponseCallback<FormSubnodeResponse>(this) {
				@Override
				public void onCompleted(FormSubnodeResponse responseContent) {
					try {
						final String result = responseContent.getErrorCode();
						final String description = responseContent.getErrorMessage();
						if ("0".equals(result)) {
							FEToast.showMessage(mContext.getString(R.string.message_operation_alert));
							EventBus.getDefault().post(new EventMessageDisposeSuccess());
							final Intent intent = new Intent();
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
							if (CoreZygote.getApplicationServices().activityInStacks(MessageSearchActivity.class)) {
								intent.setClass(mContext, MessageSearchActivity.class);
							}
							else if (CoreZygote.getApplicationServices().activityInStacks(ChatActivity.class)) {
								intent.setClass(mContext, ChatActivity.class);
							}
							else if (exitRequestType == FormExitType.NewForm) {
								intent.setClass(mContext, FormListActivity.class);
							}
							else if (CoreZygote.getApplicationServices().activityInStacks(ApprovalCollaborationListActivity.class)) {
								intent.setClass(mContext, ApprovalCollaborationListActivity.class);
							}
							else if (CoreZygote.getApplicationServices().activityInStacks(TaskMessageActivity.class)) {
								intent.setClass(mContext, TaskMessageActivity.class);
							}
							else if (CoreZygote.getApplicationServices().activityInStacks(SystemMessageActivity.class)) {
								intent.setClass(mContext, SystemMessageActivity.class);
							}
							else {
								intent.setClass(mContext, FEMainActivity.class);
							}
							mContext.startActivity(intent);
							//送办结束后要清除被清除暂存的缓存文件
							File cacheFile = new File(CachePath.getCachePath(CachePath.FORM, mId, request.getRequestType()));
							if (cacheFile.exists()) {
								cacheFile.delete();
							}
							isAllowSend = true;
						}
						else {
							isAllowSend = true;
							FEToast.showMessage(description == null ? mContext.getString(R.string.message_operation_fail) : description);
						}
						hideLoadActivity();
					} catch (final Exception e) {
						isAllowSend = true;
						e.printStackTrace();
						hideLoadActivity();
					}
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					isAllowSend = true;
					if (mHandler != null) {
						mHandler.sendEmptyMessage(REQUEST_FAIL);
					}
				}
			});
		}
		else { // 手写意见 需要做附件相关的处理
			final String filePath = CoreZygote.getPathServices().getTempFilePath() + "/handwrittenFiles/" + writtenGUID + ".png";
			modifyWritten(filePath).subscribe(is -> submitWrittenRequest(writtenGUID, request, filePath));
		}
	}

	private Observable modifyWritten(String path) {//手写签批固定宽高，防止pc段显示异常
		return Observable
				.unsafeCreate(f -> {
					Bitmap bitmap = new BitmapFactory().decodeFile(path);
					if (bitmap == null) return;
					Bitmap bitmapSave = Bitmap.createBitmap(DevicesUtil.getScreenWidth(), PixelUtil.dipToPx(85f), Bitmap.Config.ARGB_4444);
					Canvas canvas = new Canvas(bitmapSave);
					Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
					canvas.drawColor(-0x1); // 白色背景
					canvas.drawBitmap(bitmap, 0f, 0f, paint);
					File file = new File(path);
					if (file.exists()) {
						file.delete();
					}

					FileOutputStream bf = null;
					try {
						bf = new FileOutputStream(file);
						bitmapSave.compress(Bitmap.CompressFormat.PNG, 100, bf);
						bf.flush();
						bf.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (bf != null) {
							try {
								bf.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					bitmap.recycle();

					FileUtil.deleteFiles(CoreZygote.getPathServices().getSlateTempPath());
					f.onNext(true);
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}

	//提交带有手写签批请求
	private void submitWrittenRequest(final String writtenGUID, FormSendDoRequest request, final String filePath) {
		final FileRequest fileRequest = new FileRequest();
		final FileRequestContent filerequestcontent = new FileRequestContent();

		filerequestcontent.setAttachmentGUID(writtenGUID);
		final List<String> files = new ArrayList<>();
		files.add(filePath);
		filerequestcontent.setFiles(files);
		filerequestcontent.setDeleteFileIds(null);

		fileRequest.setFileContent(filerequestcontent);
		fileRequest.setRequestContent(request);

		new UploadManager(mContext)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						LoadingHint.show(mContext);
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						LoadingHint.showProgress(progress);
					}
				})
				.responseCallback(new ResponseCallback<ResponseContent>() {
					@Override
					public void onCompleted(ResponseContent responseContent) {
						LoadingHint.hide();
						final String errorCode = responseContent.getErrorCode();
						final String errorMessage = responseContent.getErrorMessage();
						if (!TextUtils.equals("0", errorCode)) {
							FEToast.showMessage(errorMessage);
							isAllowSend = true;
							return;
						}

						FEToast.showMessage(mContext.getString(R.string.message_operation_alert));
						File cacheFile = new File(CachePath.getCachePath(CachePath.FORM, mId, request.getRequestType()));
						if (cacheFile.exists()) {
							cacheFile.delete();
						}
						EventBus.getDefault().post(new EventMessageDisposeSuccess());
						mContext.startActivity(FormDataProvider.buildIntent(mContext, FEMainActivity.class));
						isAllowSend = true;
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						isAllowSend = true;
						LoadingHint.hide();
					}
				})
				.execute();
	}

	/*---------------------------------------END------------------------------------------*/

	/**
	 * 转换人员节点数据数据(把ReferenceItem类型的数据装换成FormSubNodeInfo,并把的封装的集合中返回)
	 * @param nodeToSubs “节点到人员”节点的数据
	 */
	private ArrayList<FormSubNodeInfo> transformSubNodes(FormNodeToSubNode nodeToSubs, int subNodeType,
			List<ReferenceItem> subNodes) {
		if (nodeToSubs == null || subNodes == null) {
			return null;
		}
		final ArrayList<FormSubNodeInfo> subNodeInfos = new ArrayList<>();
		final FormNodeItem nodeItem = nodeToSubs.getFormNodeItem();
		for (final ReferenceItem subItem : subNodes) {
			final FormSubNodeInfo subNodeInfo = new FormSubNodeInfo();
			subNodeInfo.setNodeItem(nodeItem);
			subNodeInfo.setNodeType(subNodeType);
			subNodeInfo.setReferenceItem(subItem);
			subNodeInfo.setNeedAddState(true);
			final String figureID = nodeItem.getFigureID();
			String[] defaultSendDo = figureID.split(",");
			if (!TextUtils.isEmpty(figureID)) {
				for (String sendDo : defaultSendDo) {
					if (sendDo.equals(subItem.getKey())) {// 如果默认办理人跟人员节点中的某一人是同一个人
						subNodeInfo.setNeedAddState(false);// 设置它的选择状态为选中
					}
				}
			}
			subNodeInfos.add(subNodeInfo);
		}
		return subNodeInfos;
	}

	/**
	 * 隐藏LoadingHint
	 */
	private void hideLoadActivity() {
		if (LoadingHint.isLoading()) {
			LoadingHint.hide();
		}
	}

	public ArrayList<FormExitToNodeItem> getFormExitToNodeItems() {
		return formExitToNodeItems;
	}

	public List<ReferenceItem> getExitItems() {
		return mExitItems;
	}

	public void onDestory() {
		FEHttpClient.cancel(this);
	}

	public static Intent buildIntent(Context context, Class defaultClass) {
		final Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if (CoreZygote.getApplicationServices().activityInStacks(MessageSearchActivity.class)) {
			intent.setClass(context, MessageSearchActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(ApprovalSearchActivity.class)) {
			intent.setClass(context, ApprovalSearchActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(ApprovalCollaborationListActivity.class)) {
			intent.setClass(context, ApprovalCollaborationListActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(Plan6MainActivity.class)) {
			intent.setClass(context, Plan6MainActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(Plan7MainActivity.class)) {
			intent.setClass(context, Plan7MainActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(MeetingListActivity.class)) {
			intent.setClass(context, MeetingListActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(TaskMessageActivity.class)) {
			intent.setClass(context, TaskMessageActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(SystemMessageActivity.class)) {
			intent.setClass(context, SystemMessageActivity.class);
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(ChatActivity.class)) {
			intent.setClass(context, ChatActivity.class);
		}
		else {
			intent.setClass(context, defaultClass);
		}
		return intent;
	}
}
