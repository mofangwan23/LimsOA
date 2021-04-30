package cn.flyrise.feep.addressbook.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.PositionAdapter;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.DismissEvent;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.addressbook.model.PositionEvent;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;

/**
 * @author ZYP
 * @since 2016-12-06 14:10
 */
public class PositionFilterFragment extends BaseFilterFragment {
    public static final Position DEFAULT_POSITION = new Position("-10086", "全部岗位");

    private String mDeptId;
    private ListView mListView;
    private Position mPosition;
    private List<Position> mPositions;
    private PositionAdapter mPositionAdapter;

    public static PositionFilterFragment newInstance(Department company, Department department,
                                                     Department subDepartment, Position position) {
        PositionFilterFragment instance = new PositionFilterFragment();
        instance.initInFirstTime(company, department, subDepartment, position);
        return instance;
    }

    private void initInFirstTime(Department company, Department department, Department subDepartment, Position position) {
        String deptId = null;
        if (subDepartment != null) {        // 二级部门不为空，根据二级部门的 id 查找岗位
            deptId = subDepartment.deptId;
        }
        else if (department != null) {      // 二级部门为空，尝试使用一级部门的 id 查找岗位
            deptId = department.deptId;
        }
        else if (company != null) {         // 一级部门为空，使用特么的公司 id 查找岗位
            deptId = company.deptId;
        }

        this.mPosition = position;
        this.mPositions = AddressBookRepository.get().queryPositionByDeptId(mDeptId = deptId);
        this.addAllItemInPositions();
    }

    public void setDefaultPosition(Department company, Department department, Department subDepartment, Position position) {
        String newDeptId = null;
        if (subDepartment != null) {
            newDeptId = subDepartment.deptId;
        }
        else if (department != null) {
            newDeptId = department.deptId;
        }
        else if (company != null) {
            newDeptId = company.deptId;
        }

        this.mPositions = AddressBookRepository.get().queryPositionByDeptId(mDeptId = newDeptId);
        this.addAllItemInPositions();
        this.mPosition = position;
        if (mPositionAdapter != null) {
            mPositionAdapter.setDefault(this.mPosition);
            mPositionAdapter.setData(mPositions);
            mPositionAdapter.notifyDataSetChanged();
        }
    }

    @Override public View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ab_filter_base, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        resetContentHeight(mListView = (ListView) view.findViewById(R.id.listView));
        this.initialize();
        return view;
    }

    private void initialize() {
        if (mPositionAdapter == null) {
            mPositionAdapter = new PositionAdapter();
        }

        mPositionAdapter.setDefault(mPosition);
        mPositionAdapter.setData(mPositions);
        mListView.setAdapter(mPositionAdapter);

        mListView.setOnItemClickListener((parent, itemView, position, id) -> {
            Position selectedPosition = (Position) mPositionAdapter.getItem(position);
            boolean hasChange = mPosition == null || !TextUtils.equals(mPosition.position, selectedPosition.position);
            EventBus.getDefault().post(new PositionEvent(mPosition = selectedPosition, hasChange));
            EventBus.getDefault().post(new DismissEvent());
        });
    }

    private void addAllItemInPositions() {
        if (mPositions == null) {
            mPositions = new ArrayList<>();
        }
        mPositions.add(0, DEFAULT_POSITION);
    }
}
