package cn.flyrise.feep.addressbook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.ContactInfo;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.image.loader.BlurTransformation;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.annotations.Route;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.chatui.domain.OnEventConversationLoad;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * ????????????????????????refactor by ZYP in 2017-02-17
 */
@Route("/addressBook/detail")
public class AddressBookDetailActivity extends BaseActivity {

	private ContactInfo mContactInfo;
	private FELoadingDialog mLoadingDialog;
	private Transformation mTransformation;
	private LinearLayout mLayoutIm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addressbook_detail);
		EventBus.getDefault().register(this);
	}

	@Override
	protected boolean optionStatusBar() {
		return FEStatusBar.setLightStatusBar(this);
	}

	@Override
	protected int statusBarColor() {
		return Color.TRANSPARENT;
	}

	@Override
	protected void onStart() {
		super.onStart();
		WMStamp.getInstance().draw(this);
	}

	@Override
	public void bindView() {
		ImageView ivBackground = (ImageView) findViewById(R.id.ivBackground);
		ImageView ivUserIcon = (ImageView) findViewById(R.id.ivUserIcon);   // ????????????
		TextView tvUserName = (TextView) findViewById(R.id.tvUserName);     // ????????????
		TextView tvPosition = (TextView) findViewById(R.id.tvPosition);     // ????????????
		TextView tvEmail = (TextView) findViewById(R.id.tvEmail);           // ????????????
		TextView tvUserTel = (TextView) findViewById(R.id.tvUserTel);                  // ??????????????????
		TextView tvUserPhone = (TextView) findViewById(R.id.tvUserPhone);              // ??????????????????
		TextView tvUserAddress = (TextView) findViewById(R.id.tvAddress);              // ????????????
		TextView tvDepartmentName = (TextView) findViewById(R.id.tvDepartment);        // ????????????

		ImageView btnTelCall = (ImageView) findViewById(R.id.ivTelCall);               // ??????????????????
		ImageView btnPhoneCall = (ImageView) findViewById(R.id.ivMobileCall);          // ??????????????????
		ImageView btnEmailSend = (ImageView) findViewById(R.id.ivEmailSend);           // ??????????????????

		mLayoutIm = findViewById(R.id.viewSendIM);//im?????????

		String userID = getIntent().getStringExtra(K.addressBook.user_id);
		userID = CoreZygote.getAddressBookServices().getActualUserId(userID);
		final String deptId = getIntent().getStringExtra(K.addressBook.department_id);
		final String host = CoreZygote.getLoginUserServices() == null ? "" : CoreZygote.getLoginUserServices().getServerAddress();

		showLoading();

		Observable<ContactInfo> observable = AddressBookRepository.get().queryUserDetailInfo(userID, deptId);
		observable
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(contactInfo -> {
					hideLoading();
					this.mContactInfo = contactInfo;

					String imageHref = host + mContactInfo.imageHref;                               // ??????????????????
					if (!TextUtils.isEmpty(imageHref) && imageHref.contains("/UserUploadFile/photo")) {
						ivBackground.setImageResource(R.drawable.personalbg);
					}
					else {
						mTransformation = new BlurTransformation(getApplicationContext(), 25, 3);
						Glide.with(this)
								.load(imageHref)
								.apply(new RequestOptions().placeholder(R.drawable.personalbg)
										.error(R.drawable.personalbg)
										.transform(mTransformation))
								.into(ivBackground);
//                        FEImageLoader.loadWithTransform(this, imageHref, ivBackground, R.drawable.personalbg, new BlurTransformation(this, 25, 3));
					}

					FEImageLoader.load(this, ivUserIcon, imageHref,                                                 // ??????????????????
							mContactInfo.userId, mContactInfo.name);

					tvUserName.setText(mContactInfo.name);                                                          // ??????
					tvPosition.setText(mContactInfo.position);                                                      // ??????

					tvUserTel.setVisibility(TextUtils.isEmpty(mContactInfo.tel) ? View.INVISIBLE : View.VISIBLE);        // ????????????
					tvUserTel.setText(mContactInfo.tel);

					tvUserPhone.setVisibility(TextUtils.isEmpty(mContactInfo.phone) ? View.INVISIBLE : View.VISIBLE);    // ????????????
					tvUserPhone.setText(mContactInfo.phone);

					tvEmail.setVisibility(TextUtils.isEmpty(mContactInfo.email) ? View.INVISIBLE : View.VISIBLE);        // ??????
					tvEmail.setText(mContactInfo.email);

					tvDepartmentName.setText(mContactInfo.deptName);                                                // ????????????
					tvUserAddress.setText(mContactInfo.address);                                                    // ??????

					ivUserIcon.setOnClickListener(v -> {
//						if (TextUtils.isEmpty(imageHref)) return;
//						Intent intent = new Intent(this, EaseShowBigImageActivity.class);
//						intent.putExtra("localUrl", imageHref);
//						startActivity(intent);
					});
				}, exception -> {
					hideLoading();
					mContactInfo = null;
					exception.printStackTrace();
					FEToast.showMessage(getResources().getString(R.string.core_data_get_error));
				});

		btnTelCall.setOnClickListener(view -> {                             // ??????????????????
			if (mContactInfo != null) {
				openDialUI(mContactInfo.tel);
			}
		});

		btnPhoneCall.setOnClickListener(view -> {                           // ??????????????????
			if (mContactInfo != null) {
				openDialUI(mContactInfo.phone);
			}
		});
		btnEmailSend.setOnClickListener(view -> {                           // ????????????
			if (mContactInfo != null) {
				if (TextUtils.isEmpty(mContactInfo.email)) {
					FEToast.showMessage(getString(R.string.contacts_detail_no_add_email));
					return;
				}
				try {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.setType("message/rfc822");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContactInfo.email});
					startActivity(intent);
				} catch (Exception exp) {
					FEToast.showMessage(getString(R.string.device_no_mailbox));
				}
			}
		});

		findViewById(R.id.viewCallTel).setOnClickListener(this::callPhone);                 // ???????????????
		findViewById(R.id.viewSaveToContact).setOnClickListener(this::saveToLocalContact);  // ????????????????????????
		if (CoreZygote.getLoginUserServices() == null) return;
		String loginUserId = CoreZygote.getLoginUserServices().getUserId();

		if (FunctionManager.hasPatch(Patches.PATCH_HUANG_XIN)
				&& !TextUtils.equals(userID, loginUserId)
				&& IMHuanXinHelper.getInstance().isImLogin()) {
			mLayoutIm.setVisibility(View.VISIBLE);
			mLayoutIm.setOnClickListener(view -> {                          // ??????????????????
				if (mContactInfo != null) {
					IMHuanXinHelper.getInstance().startChatActivity(this, mContactInfo.userId);
				}
			});
		}
		else {
			mLayoutIm.setVisibility(View.GONE);
		}
	}

	private void callPhone(View view) {
		if (mContactInfo == null) {
			FEToast.showMessage(getResources().getString(R.string.core_data_get_error));
			return;
		}

		if (TextUtils.isEmpty(mContactInfo.tel) && TextUtils.isEmpty(mContactInfo.phone)) {
			FEToast.showMessage(getResources().getString(R.string.the_contact_detail_no_tel));
			return;
		}

		List<String> phones = new ArrayList<>();
		if (!TextUtils.isEmpty(mContactInfo.tel)) {
			phones.add(getResources().getString(R.string.contact_company_tel) + " " + mContactInfo.tel);
		}

		if (!TextUtils.isEmpty(mContactInfo.phone)) {
			phones.add(getResources().getString(R.string.contact_mobile_phone) + " " + mContactInfo.phone);
		}

		new FEMaterialDialog.Builder(AddressBookDetailActivity.this)
				.setWithDivider(false)
				.setWithoutTitle(true)
				.setCancelable(true)
				.setItems(phones.toArray(new String[]{}), (dialog, v, position) -> {
					String phone = phones.get(position);
					openDialUI(phone.contains(getResources().getString(R.string.contact_company_tel))
							? mContactInfo.tel : mContactInfo.phone);
				})
				.build()
				.show();
	}

	private void saveToLocalContact(View view) {
		if (mContactInfo == null) {
			FEToast.showMessage(getResources().getString(R.string.core_data_get_error));
			return;
		}

		if (TextUtils.isEmpty(mContactInfo.tel) && TextUtils.isEmpty(mContactInfo.phone)) {
			FEToast.showMessage(getResources().getString(R.string.the_contact_detail_no_tel));
			return;
		}

		FePermissions.with(AddressBookDetailActivity.this)                          // ?????????????????????
				.permissions(new String[]{Manifest.permission.READ_CONTACTS})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_contact))
				.requestCode(PermissionCode.CONTACTS)
				.request();
	}

	@PermissionGranted(PermissionCode.CONTACTS)
	public void savePhoneContacts() {   // ???????????????????????????????????????
		int contactID = getContactID(mContactInfo.name);
		if (contactID != -1) {                                                      // ??????????????????????????????
			new FEMaterialDialog.Builder(AddressBookDetailActivity.this)
					.setCancelable(true)
					.setMessage(R.string.addressbook_detail_info_saved)
					.setPositiveButton(R.string.collaboration_recorder_ok, dialog -> startActivity(buildContactIntent()))
					.setNegativeButton(R.string.collaboration_recorder_cancel, null)
					.build()
					.show();
			return;
		}

		try {
			startActivity(buildContactIntent());
		} catch (final ActivityNotFoundException e) {                               // ???????????????????????????ActivityNotFound??????
			FELog.v("ddd", "?????????????????????ActivityNotFound" + e.toString());
		}
	}

	/**
	 * ??????????????????????????? Intent ???
	 */
	private Intent buildContactIntent() {
		final Intent intent = new Intent();
		intent.setAction(Intent.ACTION_INSERT);
		intent.setData(Contacts.CONTENT_URI);
		intent.putExtra(ContactsContract.Intents.Insert.NAME, mContactInfo.name);
		intent.putExtra(ContactsContract.Intents.Insert.PHONE, mContactInfo.tel);
		intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, Phone.TYPE_WORK);               // ????????????
		intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, mContactInfo.phone);
		intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, Phone.TYPE_MOBILE);   // ????????????
		intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, mContactInfo.address);
		intent.putExtra(ContactsContract.Intents.Insert.EMAIL, mContactInfo.email);
		return intent;
	}

	/**
	 * (??????????????????????????????????????????)
	 * @return -1????????????????????????????????????-1??????????????????????????????
	 */
	private int getContactID(String name) {
		Cursor cursor = null;
		int contactId = -1;
		try {
			cursor = getContentResolver().query(Phone.CONTENT_URI, new String[]{BaseColumns._ID},
					Contacts.DISPLAY_NAME + " = '" + name + "'", null, null);
			if (cursor.moveToNext()) {
				contactId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			contactId = -1;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return contactId;
	}

	/**
	 * ??????????????????
	 */
	private void openDialUI(String tel) {
		if (TextUtils.isEmpty(tel)) {
			FEToast.showMessage(getResources().getString(R.string.the_contact_detail_no_tel));
			return;
		}
		if (isNoOpenCurrentPhone(tel)) {
			FEToast.showMessage(getResources().getString(R.string.contacts_detail_no_open_phone));
			return;
		}
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setData(Uri.parse("tel:" + tel));
		startActivity(intent);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.AddressBookDetail);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.AddressBookDetail);
	}

	private void showLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setCancelable(true)
				.create();
		mLoadingDialog.show();
	}

	private void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.removeDismissListener();
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onHuanXinLoginSuccess(OnEventConversationLoad code) {
		if (mLayoutIm != null && mLayoutIm.getVisibility() == View.GONE) {
			mLayoutIm.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideLoading();
		if (mTransformation != null) {
			mTransformation = null;
		}
		EventBus.getDefault().unregister(this);
		WMStamp.getInstance().clearWaterMark(this);
	}

	@SuppressLint("MissingPermission")
	private boolean isNoOpenCurrentPhone(String phone) {
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm == null) return false;
		@SuppressLint("HardwareIds")
		String te1 = tm.getLine1Number();//??????????????????
		return TextUtils.equals(te1, phone);
	}
}
