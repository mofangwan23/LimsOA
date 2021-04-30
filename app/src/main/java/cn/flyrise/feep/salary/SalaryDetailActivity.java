package cn.flyrise.feep.salary;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.image.loader.BlurTransformation;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.salary.model.SalaryItem;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-02-16 10:26 每次都需要先输入密码进行校验后，才能进行工资的查看。
 */
@Route("/salary/detail")
@RequestExtras({"EXTRA_REQUEST_MONTH", "EXTRA_SHOW_VERIFY"})
public class SalaryDetailActivity extends BaseSalaryActivity {

	private RecyclerView mListView;
	private SalaryDetailAdapter mAdapter;
	private TextView mTvPayable;
	private TextView mTvRealPay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_salary_detail);
	}

	@Override
	public void bindView() {
		super.bindView();
		mListView = findViewById(R.id.listView);
		mListView.setLayoutManager(new LinearLayoutManager(this));
		mListView.addItemDecoration(new WMAddressDecoration(WMStamp.getInstance().getWaterMarkText()));      // 设置水印
		mListView.setAdapter(mAdapter = new SalaryDetailAdapter());
		FEToolbar mToolbar = findViewById(R.id.toolBar);
		mToolbar.setBackgroundColor(Color.parseColor("#00000000"));
		mToolbar.setTitle("");
		mToolbar.setLineVisibility(View.GONE);
		mToolbar.setDarkMode();

		ImageView ivBackground = findViewById(R.id.ivBackground);
		ImageView ivUserIcon = findViewById(R.id.ivUserIcon);
		mTvPayable = findViewById(R.id.tvPayable);
		mTvRealPay = findViewById(R.id.tvRealPay);

		ILoginUserServices services = CoreZygote.getLoginUserServices();

		String userId = services.getUserId();
		String userName = services.getUserName();
		String host = services.getServerAddress();
		String imageHref = services.getUserImageHref();

		if (TextUtils.isEmpty(imageHref) || imageHref.contains("/UserUploadFile/photo")) {
			ivBackground.setImageResource(R.drawable.salary_detail_head);
		}
		else {
			Glide.with(SalaryDetailActivity.this)
					.load(host + imageHref)
					.apply(new RequestOptions().placeholder(R.drawable.salary_detail_head)
							.error(R.drawable.salary_detail_head)
							.transform(new BlurTransformation(this, 25, 3)))
					.into(ivBackground);
		}

		FEImageLoader.load(this, ivUserIcon, host + imageHref, userId, userName);
		mSafetyVerifyManager.startVerify(K.salary.gesture_verify_request_code, this);
	}

	@Override
	public void onVerifySuccess() {
		super.onVerifySuccess();
		showLoading();
		SalaryDataSources.querySalaryDetail(getIntent().getStringExtra(K.salary.request_month))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(detailItems -> {
					hideLoading();
					if (CommonUtil.isEmptyList(detailItems)) {
						showNotDataDialogs();
						return;
					}
					mAdapter.setSalaryItems(detailItems);
					float[] salaries = calculateSalary(detailItems);
					mTvPayable.setText(
							String.format(getString(R.string.salary_total_pay), BaseSalaryActivity.formatMonery(salaries[0] + "")));
					mTvRealPay
							.setText(String.format(getString(R.string.salary_net_pay), BaseSalaryActivity.formatMonery(salaries[1] + "")));
				}, exception -> {
					exception.printStackTrace();
					hideLoading();
					FEToast.showMessage(getResources().getString(R.string.salary_get_detail_failed));
				});
	}

	private float[] calculateSalary(List<SalaryItem> salaryItems) {
		float[] realPay = new float[2];
		float totalAdd = 0.0F;
		float totalSub = 0.0F;
		for (SalaryItem salaryItem : salaryItems) {
			if (SalaryItem.TYPE_ADD == salaryItem.type) {
				totalAdd += CommonUtil.parseFloat(salaryItem.value);
			}
			else if (SalaryItem.TYPE_SUB == salaryItem.type) {
				totalSub += CommonUtil.parseFloat(salaryItem.value);
			}
		}
		realPay[0] = totalAdd;
		realPay[1] = totalAdd - totalSub;
		return realPay;
	}

	private void showNotDataDialogs() {
		new FEMaterialDialog.Builder(this)
				.setMessage(cn.flyrise.feep.core.R.string.core_data_deleted)
				.setPositiveButton(null, dialog -> finish())
				.setDismissListener(dialog -> finish())
				.build()
				.show();
	}
}
