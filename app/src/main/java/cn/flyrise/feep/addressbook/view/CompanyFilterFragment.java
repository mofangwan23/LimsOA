package cn.flyrise.feep.addressbook.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.CompanyAdapter;
import cn.flyrise.feep.addressbook.model.CompanyEvent;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.DismissEvent;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2016-12-06 10:35
 */
public class CompanyFilterFragment extends BaseFilterFragment {

    private String mUserId;
    private boolean isOnlyOneCompany;
    private ListView mListView;
    private Department mCompany;
    private List<Department> mCompanies;
    private CompanyAdapter mCompanyAdapter;

    public static CompanyFilterFragment newInstance(Department company, String userId, boolean isOnlyOneCompany) {
        CompanyFilterFragment instance = new CompanyFilterFragment();
        instance.mUserId = userId;
        instance.isOnlyOneCompany = isOnlyOneCompany;
        instance.initInFirstTime(company);
        return instance;
    }

    private void initInFirstTime(Department company) {      // 初始化，传进来的 company 有可能是公司，也有可能是部门
        this.mCompany = company;
        List<Department> allCompanies = AddressBookRepository.get().queryAllCompany();
        this.mCompanies = TextUtils.isEmpty(mUserId) ? allCompanies : AddressBookRepository.get().queryCompanyWhereUserIn(mUserId);
        if (isOnlyOneCompany) { // 只有一个公司，子部门需要提前一级
            this.mCompanies = AddressBookRepository.get().queryDepartmentByCompany(company.fatherId);
            if (CommonUtil.nonEmptyList(mCompanies) && mCompanies.size() == 1) {
                this.mCompanies = AddressBookRepository.get().queryDepartmentByCompany(company.deptId);
            }
            this.mCompanies.add(0, allCompanies.get(0));
        }
    }

    public void setDefaultCompany(Department company) {     // 设置默认公司
        this.mCompany = company;
        if (mCompanyAdapter != null) {
            mCompanyAdapter.setDefault(this.mCompany);
            mCompanyAdapter.notifyDataSetChanged();
        }
    }

    @Override public View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ab_filter_base, container, false);
        resetContentHeight(mListView = (ListView) view.findViewById(R.id.listView));
        this.initialize();
        return view;
    }

    private void initialize() {
        if (mCompanyAdapter == null) {
            mCompanyAdapter = new CompanyAdapter();
        }

        mCompanyAdapter.setDefault(mCompany);
        mCompanyAdapter.setData(mCompanies);
        mListView.setAdapter(mCompanyAdapter);

        mListView.setOnItemClickListener((parent, itemView, position, id) -> {
            Department selectedCompany = (Department) mCompanyAdapter.getItem(position);
            boolean hasChange = mCompany == null || !mCompany.equals(selectedCompany);
            EventBus.getDefault().post(new CompanyEvent(mCompany = selectedCompany, hasChange, isOnlyOneCompany));
            EventBus.getDefault().post(new DismissEvent());
        });
    }
}
