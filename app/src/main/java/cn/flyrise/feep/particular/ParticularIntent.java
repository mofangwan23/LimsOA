package cn.flyrise.feep.particular;

import static cn.flyrise.feep.utils.Patches.PATCH_APPLICATION_BUBBLE;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.core.function.FunctionManager;
import java.io.Serializable;
import java.util.List;

import cn.flyrise.feep.commonality.bean.FEListItem;

/**
 * @author ZYP
 * @since 2016-10-25 08:55
 */
public class ParticularIntent {

	public static final String EXTRA_MESSAGE_ID = "extra_message_id";
	public static final String EXTRA_BUSINESS_ID = "extra_business_id";
	public static final String EXTRA_REQUEST_TYPE = "extra_request_type";
	public static final String EXTRA_FE_LIST_ITEM = "extra_fe_list_item";
	public static final String EXTRA_FROM_ASSOCIATE = "extra_from_associate";
	public static final String EXTRA_PARTICULAR_TYPE = "extra_particular_type";
	public static final String EXTRA_RELATED_USER_ID = "extra_related_user_id";
	public static final String EXTRA_FROM_MESSAGE_LIST = "extra_from_message_list";
	public static final String EXTRA_FROM_NOTIFICATION = "extra_from_notification";

	private final Context mContext;
	private final Intent mIntent;

	private String mTempBusinessId;
	private String mTempMessageId;
	private int mTempRequestType = -1;
	private int requestCode;


	public ParticularIntent(Context context, Intent intent) {
		this.mContext = context;
		this.mIntent = intent;
		this.requestCode = -1;
	}

	public ParticularIntent(Context context, Intent intent, int requestCode) {
		this.mContext = context;
		this.mIntent = intent;
		this.requestCode = requestCode;
	}

	public ParticularIntent start() {
		ComponentName componentName = mIntent.getComponent();
		if (TextUtils.equals(mContext.getClass().getCanonicalName(), componentName.getClassName())) {
			return null;
		}

		if (componentName == null) {
			throw new NullPointerException(
					"You must set the target activity class in ParticularIntent.Builder.setTargetClass() before start intent.");
		}

		List<ResolveInfo> results = mContext.getPackageManager().queryIntentActivities(mIntent, 0);
		if (results == null || results.size() == 0) {
			throw new RuntimeException("Can not resolve the intent by this class : " + componentName.getClassName());
		}

		if (mContext instanceof Activity) {
			if (requestCode == -1)
				mContext.startActivity(mIntent);
			else {
				((Activity) mContext).startActivityForResult(mIntent, requestCode);
			}
		}
		else {
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(mIntent);
		}
		return this;
	}

	public Intent getIntent() {
		return this.mIntent;
	}

	public int getParticularType() {
		return mIntent.getIntExtra(EXTRA_PARTICULAR_TYPE, -1);
	}

	public void setTempBusinessId(String tempBusinessId) {
		this.mTempBusinessId = tempBusinessId;
	}

	public String getBusinessId() {
		if (!TextUtils.isEmpty(mTempBusinessId)) {
			return mTempBusinessId;
		}
		return getIntentBusinessId();
	}

	public void setTempMessageId(String tempMessageId) {
		this.mTempMessageId = tempMessageId;
	}

	public String getMessageId() {
		if (!TextUtils.isEmpty(mTempMessageId)) {
			return mTempMessageId;
		}
		return getIntentMessageId();
	}

	public String getIntentBusinessId(){
		return mIntent.getStringExtra(EXTRA_BUSINESS_ID);
	}

	public int getIntentRequestType(){
		return mIntent.getIntExtra(EXTRA_REQUEST_TYPE, -1);
	}

	public String getIntentMessageId(){
		return mIntent.getStringExtra(EXTRA_MESSAGE_ID);
	}

	public String getRelatedUserId() {
		return mIntent.getStringExtra(EXTRA_RELATED_USER_ID);
	}

	public FEListItem getFEListItem() {
		Serializable serializableExtra = mIntent.getSerializableExtra(EXTRA_FE_LIST_ITEM);
		if (serializableExtra instanceof FEListItem) {
			return (FEListItem) serializableExtra;
		}
		return null;
	}

	public void setTempRequestType(int requestType) {
		this.mTempRequestType = requestType;
	}

	public int getTempRequestType() {
		return this.mTempRequestType;
	}

	public int getListRequestType() {
		if (mTempRequestType != -1) {
			return mTempRequestType;
		}
		return getIntentRequestType();
	}

	public boolean isFromAssociate() {
		return mIntent.getBooleanExtra(EXTRA_FROM_ASSOCIATE, false);
	}

	public boolean isFromMessageList() {
		return mIntent.getBooleanExtra(EXTRA_FROM_MESSAGE_LIST, false);
	}

	public boolean isFromNotification() {
		return mIntent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
	}

	public static class Builder {

		private Context context;
		private Intent targetIntent;
		private int requestCode = -1;

		public Builder(Context context) {
			this.context = context;
			this.targetIntent = new Intent();
		}

		public Builder setTargetClass(Class<? extends Activity> targetClass) {
			if (targetClass == null) {
				throw new NullPointerException("The target activity class must not be null.");
			}
			targetIntent.setClass(context, targetClass);
			return this;
		}

		public Builder setParticularType(int particularType) {
			targetIntent.putExtra(EXTRA_PARTICULAR_TYPE, particularType);
			return this;
		}

		public Builder setMessageId(String messageId) {
			if (!TextUtils.isEmpty(messageId)) {
				targetIntent.putExtra(EXTRA_MESSAGE_ID, messageId);
			}
			return this;
		}

		public Builder setBusinessId(String businessId) {
			if (!TextUtils.isEmpty(businessId)) {
				targetIntent.putExtra(EXTRA_BUSINESS_ID, businessId);
			}
			return this;
		}

		public Builder setRelatedUserId(String relatedUserId) {
			if (!TextUtils.isEmpty(relatedUserId)) {
				targetIntent.putExtra(EXTRA_RELATED_USER_ID, relatedUserId);
			}
			return this;
		}

		public Builder setListRequestType(int requestType) {
			targetIntent.putExtra(EXTRA_REQUEST_TYPE, requestType);
			return this;
		}

		public Builder setFEListItem(FEListItem feListItem) {
			if (feListItem != null) {
				targetIntent.putExtra(EXTRA_FE_LIST_ITEM, feListItem);
			}
			return this;
		}

		public Builder setFromAssociate(boolean isFromAssociate) {
			targetIntent.putExtra(EXTRA_FROM_ASSOCIATE, isFromAssociate);
			return this;
		}

		public Builder setFromMessageList(boolean isFromMessageList) {
			targetIntent.putExtra(EXTRA_FROM_MESSAGE_LIST, isFromMessageList);
			return this;
		}

		public Builder setFromNotification(boolean isFromNotification) {
			targetIntent.putExtra(EXTRA_FROM_NOTIFICATION, isFromNotification);
			return this;
		}

		public Builder setRequestCode(int requestCode) {
			this.requestCode = requestCode;
			return this;
		}

		public ParticularIntent create() {
			return new ParticularIntent(context, targetIntent, requestCode);
		}
	}
}
