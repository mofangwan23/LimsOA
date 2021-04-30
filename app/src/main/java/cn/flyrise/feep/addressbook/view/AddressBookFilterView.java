package cn.flyrise.feep.addressbook.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author ZYP
 * @since 2016-12-05 16:46
 */
public class AddressBookFilterView extends LinearLayout {

	private ViewGroup mCompanyView;          // 公司筛选视图
	private ViewGroup mDepartmentView;       // 部门筛选视图
	private ViewGroup mPositionView;         // 岗位筛选视图
	private ViewGroup mCommonlyView;         // 常用组视图

	private TextView mTvCompany;
	private TextView mTvDepartment;
	private TextView mTvPosition;
	private TextView mTvCommonly;

	public AddressBookFilterView(Context context) {
		this(context, null);
	}

	public AddressBookFilterView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AddressBookFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOrientation(HORIZONTAL);
	}

	@Override protected void onFinishInflate() {
		super.onFinishInflate();
		if (getChildCount() != 4) {
			throw new IllegalArgumentException("This ViewGroup must contains three view.(Company, Department and Position)");
		}

		this.mCompanyView = (ViewGroup) getChildAt(0);
		this.mTvCompany = (TextView) mCompanyView.getChildAt(0);

		this.mDepartmentView = (ViewGroup) getChildAt(1);
		this.mTvDepartment = (TextView) mDepartmentView.getChildAt(0);

		this.mPositionView = (ViewGroup) getChildAt(2);
		this.mTvPosition = (TextView) mPositionView.getChildAt(0);

		this.mCommonlyView = (ViewGroup) getChildAt(3);
		this.mTvCommonly = (TextView) mCommonlyView.getChildAt(0);
	}

	public void setCompanyClickListener(OnClickListener listener) {
		if (listener != null) {
			this.mCompanyView.setOnClickListener(listener);
		}
	}

	public void setDepartmentClickListener(OnClickListener listener) {
		if (listener != null) {
			this.mDepartmentView.setOnClickListener(listener);
		}
	}

	public void setPositionClickListener(OnClickListener listener) {
		if (listener != null) {
			this.mPositionView.setOnClickListener(listener);
		}
	}

	public void setCommonlyClickListener(OnClickListener listener) {
		if (listener != null) {
			this.mCommonlyView.setOnClickListener(listener);
		}
	}

	public void setCompanyName(String companyName) {
		if (!TextUtils.isEmpty(companyName)) {
			mTvCompany.setText(companyName);
		}
	}

	public void setDepartmentName(String departmentName) {
		if (!TextUtils.isEmpty(departmentName)) {
			mTvDepartment.setText(departmentName);
		}
	}

	public void setPositionName(String positionName) {
		if (!TextUtils.isEmpty(positionName)) {
			mTvPosition.setText(positionName);
		}
	}

	public void setCommonlyName(String commonlyName) {
		if (!TextUtils.isEmpty(commonlyName)) {
			mTvCommonly.setText(commonlyName);
		}
	}

	public void hideCommonlyGroup() {
		this.mCommonlyView.setVisibility(View.GONE);
		this.mTvCommonly.setVisibility(View.GONE);
	}

	public void showCommonlyGroup() {
		this.mCommonlyView.setVisibility(View.VISIBLE);
		this.mTvCommonly.setVisibility(View.VISIBLE);
	}

}
