package cn.flyrise.feep.userinfo.presenter;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.userinfo.contract.ModifyContract;
import cn.flyrise.feep.userinfo.modle.RemoteResponse;
import cn.flyrise.feep.userinfo.modle.UserInfoModifyBean;
import cn.flyrise.feep.userinfo.modle.UserModifyData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017-4-26.
 */

public class ModifyPresenter implements ModifyContract.presenter {

	private Context mContext;

	private ModifyContract.View mView;

	public ModifyPresenter(Context context) {
		mContext = context;
		mView = (ModifyContract.View) context;
	}

	@Override
	public boolean regexText(int type, String text) {
		if (TextUtils.isEmpty(text)) {
			return true;
		}

		if (getPhoneType()) {
			Pattern pattern = Pattern.compile(PHONE_REGEX);
			Matcher matcher = pattern.matcher(text);
			if (matcher.matches()) {
				return true;
			}
			else {
				FEToast.showMessage(mContext.getResources().getString(R.string.input_success_phone));
				return false;
			}

		}
		if (type == K.userInfo.DETAIL_EMAIL) {
			if (!isEmailAddress(text)) {
				FEToast.showMessage(mContext.getResources().getString(R.string.input_success_email));
				return false;
			}
			else {
				return true;
			}
		}
		return true;
	}

	private boolean isEmailAddress(String text) {
		return !TextUtils.isEmpty(text)&&isChatSuccess(text, "@") && isChatSuccess(text, ".") && isChatEmail(text);
	}

	private boolean isChatEmail(String text) {
		int b = getChatIndex(text, ".") - getChatIndex(text, "@");
		if (getChatIndex(text, "@") > 1 && b > 1) {
			int c = (text.length() - 1) - getChatIndex(text, ".");
			return c < 5;
		}
		return false;
	}

	private boolean isChatSuccess(String text, String chats) {
		return text.contains(chats) && getChatIndex(text, chats) != 0 && getChatIndex(text, chats) != (text.length() - 1);
	}

	private int getChatIndex(String text, String chats) {
		if (text.contains(chats)) {
			return text.lastIndexOf(chats);
		}
		return 0;
	}


	@Override
	public void successModifyText(String mText) {
		if (!mView.isSubmitText()) return;
		submitModify(mView.getType(), mText);
	}

	private void submitModify(int type, String text) {
		UserModifyData modifyData = UserModifyData.getInstance();
		if (modifyData == null) return;
		UserInfoModifyBean modifyBean = modifyData.getModifyBean();
		if (type == K.userInfo.DETAIL_PHONE) {
			modifyBean.setPhone(text);
		}
		else if (type == K.userInfo.DETAIL_TEL) {
			modifyBean.setWorkTel(text);
		}
		else if (type == K.userInfo.DETAIL_LOCATION) {
			modifyBean.setLocation(text);
		}
		else if (type == K.userInfo.DETAIL_EMAIL) {
			modifyBean.setEmail(text);
		}
		else if (type == K.userInfo.DETAIL_BIRTHDAY) {
			modifyBean.setBirthday(text);
		}
		modifyData.setModifyBean(modifyBean);
		postModifyText();
	}

	//修改普通文本
	private void postModifyText() {
		mView.showLoading();
		Observable
				.create((Subscriber<? super String> f) -> {
					postModifyText(f);
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> {
					mView.hideLoading();
					mView.successModify();
					FEToast.showMessage(mContext.getResources().getString(R.string.modify_info_success));
				}, exception -> {
					mView.hideLoading();
					FEToast.showMessage(mContext.getResources().getString(R.string.modify_info_error));
				});
	}

	private void postModifyText(Subscriber<? super String> f) {
		FEHttpClient.getInstance().post(UserModifyData.getDetailRequest(), new ResponseCallback<RemoteResponse>(mContext) {
			@Override
			public void onCompleted(RemoteResponse response) {
				if (response == null || !TextUtils.equals(response.getErrorCode(), SUCCESS_COUNT)) {
					f.onError(new NullPointerException("Request userifno failed."));
					return;
				}

				f.onNext("");
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				f.onError(new NullPointerException("Request message failed."));
			}
		});
	}

	@Override
	public void setBeforeText(int type, String beforeText) {
		if (TextUtils.isEmpty(beforeText)) return;
		if (type == K.userInfo.DETAIL_TEL) {
			if (beforeText.length() == 11 && beforeText.indexOf("1") == 0) {
				setTextToPhone(beforeText);
			}
			else {
				setContentEt(beforeText);
			}
		}
		else if (type == K.userInfo.DETAIL_PHONE) {
			if (beforeText.length() == 11 && beforeText.indexOf("1") == 0) {
				setTextToPhone(beforeText);
			}
			else {
				setContentEt(beforeText);
			}
		}
		else {
			setContentEt(beforeText);
		}
	}

	private void setContentEt(String text) {
		mView.getContextEt().setText(text);
		mView.getContextEt().setSelection(text.length());
	}

	@Override
	public void setTextToPhone(String phone) {
		if (TextUtils.isEmpty(phone)) return;
		StringBuilder sb = new StringBuilder(phone);
		if (phone.length() < 8 && phone.length() >= 3) {
			sb.insert(3, PHONE_LINE);
		}
		if (phone.length() >= 8) {
			sb.insert(3, PHONE_LINE);
			sb.insert(8, PHONE_LINE);
		}
		if (mView.getContextEt() == null) {
			return;
		}
		setContentEt(sb.toString());
	}

	/**
	 * 将文本转换成电话格式
	 **/
	@Override
	public void getTextToPhone(CharSequence s, int start, int before) {
		if (s == null || s.length() == 0 || s.length() > 12) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (i != 3 && i != 8 && s.charAt(i) == '-') {
				continue;
			}
			else {
				sb.append(s.charAt(i));
				if ((sb.length() == 4 || sb.length() == 9)
						&& sb.charAt(sb.length() - 1) != '-') {
					sb.insert(sb.length() - 1, '-');
				}
			}
		}
		if (!sb.toString().equals(s.toString())) {
			int index = start + 1;
			if (sb.charAt(start) == '-') {
				if (before == 0) {
					index++;
				}
				else {
					index--;
				}
			}
			else {
				if (before == 1) {
					index--;
				}
			}
			if (mView.getContextEt() == null) {
				return;
			}
			mView.getContextEt().setText(sb.toString());
			mView.getContextEt().setSelection(index);
		}
	}

	/**
	 * 将电话转换成文本
	 **/
	@Override
	public String getPhoneToText(String phone) {
		if (TextUtils.isEmpty(phone)) {
			return phone;
		}
		if (!phone.contains(PHONE_LINE)) {
			return phone;
		}
		return phone.replaceAll(PHONE_LINE, "");
	}

	@Override
	public boolean getPhoneType() {
		return mView.getType() == K.userInfo.DETAIL_PHONE;
	}

	//source新输入字符，dest输入前EditText中已有的字符
	@Override
	public InputFilter[] addressFileter() {
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = (source, start, end, dest, dstart, dend) -> {

			if (mView.getType() == K.userInfo.DETAIL_LOCATION) { //地址
				Pattern emoji = Pattern.compile(ADDRESS_TYPE, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
				Matcher emojiMatcher = emoji.matcher(source);
				if (!emojiMatcher.find()) {
					return "";
				}
			}
			else if (mView.getType() == K.userInfo.DETAIL_EMAIL) { //邮件
				Pattern email_type = Pattern.compile(EMAIL_TYPE, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
				Matcher emailType = email_type.matcher(source);
				if (!emailType.find()) {
					return "";
				}

			}

			int destLen = getCharacterNum(dest.toString());
			if (getCharacterNum(source.toString()) + destLen > addressMaxNums) {
				if (destLen >= addressMaxNums) {
					return "";
				}
				int num = addressMaxNums - destLen;
				if (num <= 0 || TextUtils.isEmpty(source)) {
					return "";
				}
				return subSource(source.toString(), num);
			}
			return source;
		};
		return FilterArray;
	}

	private String subSource(String s, int num) {
		int changdu = getCharacterNum(s);
		if (changdu > num) {
			s = s.substring(0, s.length() - 1);
			s = subSource(s, num);
		}
		return s;
	}

	@Override
	public int getCharacterNum(String text) {
		if (TextUtils.isEmpty(text)) {
			return 0;
		}
		try {
			return text.length();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}
