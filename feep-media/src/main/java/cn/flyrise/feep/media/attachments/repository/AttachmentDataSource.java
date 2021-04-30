package cn.flyrise.feep.media.attachments.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.AttachmentControlGroup;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;

/**
 * @author ZYP
 * @since 2017-10-30 14:32
 */
public class AttachmentDataSource {

	private final ContentResolver mContentResolver;

	public AttachmentDataSource(Context context) {
		mContentResolver = context.getContentResolver();
	}

	/**
	 * 加载用户附件：feep/userId/SAFEFILE/ 目录下的所有文件
	 */
	public Observable<List<Attachment>> queryEncryptedAttachments(@NonNull Context context, @NonNull String safePath) {
		File safeDir = new File(safePath);
		if (!safeDir.exists()) {
			return Observable.create(f -> f.onNext(null));
		}

		return Observable.zip(
				queryAllControlGroup(context),                                  /* 获取所有加密附件对照数据 */
				Observable.just(safePath).map(p -> new File(p).listFiles()),    /* 获取加密文件 */
				AttachmentConverter::convertEncryptAttachments);                 /* 将加密文件转化列表数据 */
	}

	/**
	 * 查询指定用户所有正在下载的附件任务，一般也不会多
	 */
	public Observable<List<TaskInfo>> queryTaskInfos(@NonNull String userId) {
		return Observable.create((OnSubscribe<List<TaskInfo>>) f -> {
			Cursor cursor = null;
			try {
				cursor = mContentResolver.query(AttachmentProvider.DOWNLOAD_TASK_URI, null,
						"userID = ?", new String[]{userId}, null);
				List<TaskInfo> taskInfos = new ArrayList<>();
				while (cursor.moveToNext()) {
					TaskInfo task = new TaskInfo();
					task.userID = cursor.getString(cursor.getColumnIndex("userID"));
					task.taskID = cursor.getString(cursor.getColumnIndex("taskID"));
					task.url = cursor.getString(cursor.getColumnIndex("url"));
					task.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
					task.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
					task.fileSize = cursor.getLong(cursor.getColumnIndex("fileSize"));
					task.downloadSize = cursor.getLong(cursor.getColumnIndex("downLoadSize"));
					if(!taskInfos.contains(task)) {
						taskInfos.add(task);
					}
				}
				f.onNext(taskInfos);
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				f.onCompleted();
			}
		});
	}

	public TaskInfo queryTaskInfo(String userId, String taskId) {
		TaskInfo taskInfo = null;
		Cursor cursor = null;
		try {
			cursor = mContentResolver.query(AttachmentProvider.DOWNLOAD_TASK_URI, null,
					"userID = ? and taskID = ?", new String[]{userId, taskId}, null);
			if (cursor.moveToNext()) {
				taskInfo = new TaskInfo();
				taskInfo.userID = cursor.getString(cursor.getColumnIndex("userID"));
				taskInfo.taskID = cursor.getString(cursor.getColumnIndex("taskID"));
				taskInfo.url = cursor.getString(cursor.getColumnIndex("url"));
				taskInfo.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
				taskInfo.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
				taskInfo.fileSize = cursor.getLong(cursor.getColumnIndex("fileSize"));
				taskInfo.downloadSize = cursor.getLong(cursor.getColumnIndex("downLoadSize"));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return taskInfo;
	}

	/**
	 * 新增一个下载任务
	 */
	public Uri addDownloadTask(@NonNull TaskInfo task) {
		ContentValues values = new ContentValues();
		values.put("userID", task.userID);
		values.put("taskID", task.taskID);
		values.put("url", task.url);
		values.put("filePath", task.filePath);
		values.put("fileName", task.fileName);
		values.put("fileSize", task.fileSize);
		values.put("downLoadSize", task.downloadSize);
		return mContentResolver.insert(AttachmentProvider.DOWNLOAD_TASK_URI, values);
	}

	/**
	 * 删除一个下载任务
	 */
	public int deleteDownloadTask(@NonNull TaskInfo task) {
		return mContentResolver.delete(AttachmentProvider.DOWNLOAD_TASK_URI,
				"userID = ? and taskID = ?",
				new String[]{task.userID, task.taskID});
	}

	/**
	 * 更新下载任务
	 * 其他数据都是不能更新，需要更新的只有下载进度
	 */
	public int updateDownloadTask(TaskInfo task) {
		ContentValues values = new ContentValues();
		values.put("downLoadSize", task.downloadSize);
		values.put("fileSize", task.fileSize);
		return mContentResolver.update(AttachmentProvider.DOWNLOAD_TASK_URI,
				values,
				"userID = ? and taskID = ?",
				new String[]{task.userID, task.taskID});
	}

	/**
	 * 获取当前用户已下载的附件对照表
	 */
	public Observable<List<AttachmentControlGroup>> queryAllControlGroup(Context context) {
		return Observable.create((OnSubscribe<AttachmentControlGroup>) f -> {
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(AttachmentProvider.ATTACHMENT_NAME_MAP_URI, null, null, null, null);
				while (cursor.moveToNext()) {
					AttachmentControlGroup group = new AttachmentControlGroup();
					group.taskId = cursor.getString(cursor.getColumnIndex("taskId"));
					group.storageName = cursor.getString(cursor.getColumnIndex("saveName"));
					group.realName = cursor.getString(cursor.getColumnIndex("showName"));
					f.onNext(group);
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				f.onCompleted();
			}
		}).toList();
	}

	/**
	 * 查询附件对照数据
	 */
	public AttachmentControlGroup queryControlGroup(String taskId, String storageName) {
		Cursor cursor = null;
		AttachmentControlGroup controlGroup = null;
		try {
			cursor = mContentResolver.query(AttachmentProvider.ATTACHMENT_NAME_MAP_URI, null,
					"taskId = ? and saveName = ?", new String[]{taskId, storageName}, null);
			if (cursor.moveToNext()) {
				controlGroup = new AttachmentControlGroup();
				controlGroup.taskId = cursor.getString(cursor.getColumnIndex("taskId"));
				controlGroup.storageName = cursor.getString(cursor.getColumnIndex("saveName"));
				controlGroup.realName = cursor.getString(cursor.getColumnIndex("showName"));
			}
		} catch (Exception exp) {

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return controlGroup;
	}

	/**
	 * 添加附件对照数据
	 */
	public Uri addControlGroup(AttachmentControlGroup controlGroup) {
		ContentValues values = new ContentValues();
		values.put("taskId", controlGroup.taskId);
		values.put("saveName", controlGroup.storageName);
		values.put("showName", controlGroup.realName);
		return mContentResolver.insert(AttachmentProvider.ATTACHMENT_NAME_MAP_URI, values);
	}

	/**
	 * 删除附件对照数据，一般在下载管理里删除附件时调用
	 */
	public int deleteControlGroup(String taskId, String storageName) {
		return mContentResolver.delete(AttachmentProvider.ATTACHMENT_NAME_MAP_URI,
				"taskId = ? and saveName = ?",
				new String[]{taskId, storageName});
	}
}
