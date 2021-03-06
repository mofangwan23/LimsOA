package cn.flyrise.feep.addressbook.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.DepartmentAdapter;
import cn.flyrise.feep.addressbook.adapter.SubDepartmentAdapter;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.DepartmentEvent;
import cn.flyrise.feep.addressbook.model.DismissEvent;
import cn.flyrise.feep.addressbook.model.SubDepartmentEvent;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * @author ZYP
 * @since 2016-12-06 10:36
 */
public class DepartmentFilterFragment extends BaseFilterFragment {

	public static final Department DEFAULT_DEPARTMENT = new Department("-65535", CommonUtil.getString(R.string.all_department));
	public static final Department DEFAULT_SUB_DEPARTMENT = new Department("-10086", CommonUtil.getString(R.string.all));
	private Department mCompany;
	private Department mDepartment;
	private Department mSubDepartment;

	private ListView mDepartmentListView;
	private List<Department> mDepartmentList;
	private DepartmentAdapter mDepartmentAdapter;

	private ListView mSubDepartmentListView;
	private List<Department> mSubDepartmentList;
	private SubDepartmentAdapter mSubDepartmentAdapter;
	private boolean isClickItemAndWithoutDismissDialog;     // Is click the department item and without dismiss this dialog.

	public static DepartmentFilterFragment newInstance(Department company, Department department, Department subDepartment) {
		DepartmentFilterFragment instance = new DepartmentFilterFragment();
		instance.initInFirstTime(company, department, subDepartment);
		return instance;
	}

	private void initInFirstTime(Department company, Department department, Department subDepartment) {
		this.mCompany = company;
		this.mDepartment = department;
		this.mSubDepartment = subDepartment;
		this.mDepartmentList = AddressBookRepository.get().queryDepartmentByCompany(mCompany.deptId);   // ?????????????????????????????????
		if (mDepartmentList == null) mDepartmentList = new ArrayList<>();
		this.mDepartmentList.add(0, DEFAULT_DEPARTMENT);

		if (mDepartment != null) {   // ?????????????????????
			this.mSubDepartmentList = AddressBookRepository.get().querySubDepartmentInDepartment(mDepartment.deptId);
			this.addAllItemInSubDepartments();
		}
	}

	public void setDefaultDepartment(Department company, Department department, Department subDepartment) {
		this.mCompany = company;
		// ???????????????????????????????????????????????????????????????????????????
		this.mDepartmentList = AddressBookRepository.get().queryDepartmentByCompany(mCompany.deptId);
		if (mDepartmentList == null) mDepartmentList = new ArrayList<>();
		this.mDepartmentList.add(0, DEFAULT_DEPARTMENT);
		this.mDepartment = department;
		this.mSubDepartmentList = null;
		this.mSubDepartment = null;
		if (mDepartmentAdapter != null) {
			mDepartmentAdapter.setDefault(mDepartment);
			mDepartmentAdapter.setData(mDepartmentList);
			mDepartmentAdapter.notifyDataSetChanged();
		}

		// ????????????????????????????????????????????????????????????????????????
		this.mDepartment = department;
		this.mSubDepartmentList = (mDepartment == null) ?
				null : AddressBookRepository.get().querySubDepartmentInDepartment(mDepartment.deptId);
		addAllItemInSubDepartments();
		mSubDepartment = subDepartment;
		if (mSubDepartmentAdapter != null) {
			mSubDepartmentAdapter.setDefault(mSubDepartment);
			mSubDepartmentAdapter.setData(mSubDepartmentList);
			mSubDepartmentAdapter.notifyDataSetChanged();
		}
	}

	@Override public View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mContentView = inflater.inflate(R.layout.fragment_ab_filter_department, container, false);
		LinearLayout departmentContainer = (LinearLayout) mContentView.findViewById(R.id.layoutDepartmentFilterContainer);
		resetContentHeight(departmentContainer);

		mDepartmentListView = (ListView) mContentView.findViewById(R.id.departmentListView);
		mSubDepartmentListView = (ListView) mContentView.findViewById(R.id.subDepartmentListView);
		this.initialize();
		return mContentView;
	}

	private void initialize() {
		if (mDepartmentAdapter == null) {
			mDepartmentAdapter = new DepartmentAdapter();
		}
		mDepartmentAdapter.setData(mDepartmentList);
		mDepartmentAdapter.setDefault(mDepartment);
		mDepartmentListView.setAdapter(mDepartmentAdapter);

		if (mSubDepartmentAdapter == null) {
			mSubDepartmentAdapter = new SubDepartmentAdapter();
		}
		mSubDepartmentAdapter.setData(mSubDepartmentList);
		mSubDepartmentAdapter.setDefault(mSubDepartment);
		mSubDepartmentListView.setAdapter(mSubDepartmentAdapter);

		mDepartmentListView.setOnItemClickListener(this::onItemClickListener);
		mSubDepartmentListView.setOnItemClickListener(this::onSubItemClickListener);
	}

	private void onItemClickListener(AdapterView parent, View view, int position, long id) {
		// ????????????????????????
		Department selectedDepartment = (Department) mDepartmentAdapter.getItem(position);
		boolean hasChange = mDepartment == null || !mDepartment.equals(selectedDepartment);
		if (hasChange) {                    // ?????????????????????????????????????????????
			this.mDepartment = selectedDepartment;
			this.mSubDepartment = null;
			this.mSubDepartmentList = AddressBookRepository.get().querySubDepartmentInDepartment(mDepartment.deptId);
			this.addAllItemInSubDepartments();
			if (hasSubDepartment()) {         // ???????????????
				this.isClickItemAndWithoutDismissDialog = true;
				// ?????????????????????????????????
				// EventBus ?????? Activity ?????????????????????????????????????????????
				DepartmentEvent event = new DepartmentEvent(mDepartment, hasChange);
				EventBus.getDefault().post(event);
			}
			else {                             // ??????????????????
				// ????????????
				// EventBus ?????? Acitvity ?????????????????????????????????????????????
				this.isClickItemAndWithoutDismissDialog = false;
				this.mSubDepartment = DEFAULT_SUB_DEPARTMENT;
				DepartmentEvent event = new DepartmentEvent(mDepartment, hasChange);
				event.refresh = true;
				EventBus.getDefault().post(event);
				EventBus.getDefault().post(new DismissEvent());
			}

			mDepartmentAdapter.setDefault(mDepartment);
			mDepartmentAdapter.notifyDataSetChanged();

			mSubDepartmentAdapter.setDefault(mSubDepartment);
			mSubDepartmentAdapter.setData(mSubDepartmentList);
			mSubDepartmentAdapter.notifyDataSetChanged();
		}
		else {
			if (!hasSubDepartment()) {
				// EventBus ?????? Activity ??????????????????????????????????????????????????????
				DepartmentEvent event = new DepartmentEvent(mDepartment, hasChange);
				EventBus.getDefault().post(event);
				EventBus.getDefault().post(new DismissEvent());
			}
		}
	}

	private void onSubItemClickListener(AdapterView parent, View view, int position, long id) {
		Department selectedSubDepartment = (Department) mSubDepartmentAdapter.getItem(position);
		boolean hasChange = mSubDepartment == null || !mSubDepartment.equals(selectedSubDepartment);
		if (TextUtils.equals(selectedSubDepartment.deptId, DEFAULT_SUB_DEPARTMENT.deptId)) {
			mSubDepartment = DEFAULT_SUB_DEPARTMENT;
			EventBus.getDefault().post(new DepartmentEvent(mDepartment, true, true));
		}
		else {
			EventBus.getDefault().post(new SubDepartmentEvent(false, mSubDepartment = selectedSubDepartment, hasChange));
		}
		EventBus.getDefault().post(new DismissEvent());
	}

	private void addAllItemInSubDepartments() {
		if (CommonUtil.isEmptyList(mSubDepartmentList)) {
			mSubDepartmentList = new ArrayList<>();
		}
		mSubDepartmentList.add(0, DEFAULT_SUB_DEPARTMENT);
	}

	public boolean hasSubDepartment() {
		if (CommonUtil.isEmptyList(mSubDepartmentList)) {
			return false;
		}

		if (CommonUtil.nonEmptyList(mSubDepartmentList) && mSubDepartmentList.size() == 1) {  // ?????????????????????
			Department department = mSubDepartmentList.get(0);
			String deptId = department.deptId;
			String nDeptId = DepartmentFilterFragment.DEFAULT_SUB_DEPARTMENT.deptId;
			if (TextUtils.equals(deptId, nDeptId)) {
				return false;
			}
		}
		return true;
	}

	@Override public void onHiddenChanged(boolean hidden) {
		if (hidden) {
			if (isClickItemAndWithoutDismissDialog && mSubDepartment == null) {
				DepartmentEvent event = new DepartmentEvent(mDepartment, true);
				event.refresh = true;
				EventBus.getDefault().post(event);
			}
			isClickItemAndWithoutDismissDialog = false;
		}
	}
}
