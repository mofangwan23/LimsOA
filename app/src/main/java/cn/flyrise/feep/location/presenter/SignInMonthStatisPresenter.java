package cn.flyrise.feep.location.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.location.bean.SignInMonthStatisItem;
import cn.flyrise.feep.location.contract.SignInMonthStatisContract;
import cn.flyrise.feep.location.contract.SignInMonthStatisContract.IView;
import cn.flyrise.feep.location.model.SignInStatisModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-14:03.
 */

public class SignInMonthStatisPresenter implements SignInMonthStatisContract.IPresenter {

	private SignInMonthStatisContract.IView mView;
	private SignInStatisModel mModel;
	private Context mContext;

	public SignInMonthStatisPresenter(Context context) {
		this.mContext = context;
		this.mView = (IView) context;
		mModel = new SignInStatisModel();
	}

	@Override
	public void requestMonthAndUserId(String month, String userId) {//month:2018-03
		LoadingHint.show(mContext);
		mModel.requestMonth(month, userId).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe((List<SignInMonthStatisItem> data) -> {
					LoadingHint.hide();
					mView.resultData(data);
				}, error -> {
					LoadingHint.hide();
					mView.resultError();
					FEToast.showMessage("数据异常");
				});
	}

	@SuppressLint("SimpleDateFormat")
	public Date textToDate(String yearMonth) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		try {
			return sdf.parse(yearMonth);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();
	}


}
