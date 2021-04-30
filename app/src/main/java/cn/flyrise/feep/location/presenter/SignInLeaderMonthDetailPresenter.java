package cn.flyrise.feep.location.presenter;

import android.content.Context;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.location.bean.SignInLeaderMonthDetail;
import cn.flyrise.feep.location.contract.SignInLeaderMonthDetailContract;
import cn.flyrise.feep.location.model.SignInStatisModel;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-14:03.
 */

public class SignInLeaderMonthDetailPresenter implements SignInLeaderMonthDetailContract.IPresenter {

	private SignInLeaderMonthDetailContract.IView mView;
	private SignInStatisModel mModel;
	private Context mContext;

	public SignInLeaderMonthDetailPresenter(Context context) {
		this.mContext = context;
		this.mView = (SignInLeaderMonthDetailContract.IView) context;
		mModel = new SignInStatisModel();
	}

	@Override
	public void requestMonthDetail(String month, int type) {//month:2018-03
		LoadingHint.show(mContext);
		mModel.requestLeaderMonthDetail(month, type).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe((List<SignInLeaderMonthDetail> data) -> {
					LoadingHint.hide();
					mView.resultData(data);
				}, error -> {
					LoadingHint.hide();
					mView.resultError();
					FEToast.showMessage("数据异常");
				});
	}
}
