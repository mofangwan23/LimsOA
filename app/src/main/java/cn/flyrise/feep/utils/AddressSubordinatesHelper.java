package cn.flyrise.feep.utils;

import static cn.flyrise.feep.addressbook.selection.SelectionContractKt.newSubordinatesPresenter;

import cn.flyrise.feep.addressbook.selection.ContactSelectionView;
import cn.flyrise.feep.addressbook.selection.presenter.SelectionPresenter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class AddressSubordinatesHelper implements ContactSelectionView {

	private SelectionPresenter mSelectionPresenter;

	public AddressSubordinatesHelper() {
		mSelectionPresenter = newSubordinatesPresenter(1);
		mSelectionPresenter.selectionView = this;
		mSelectionPresenter.start();
	}

	@Override
	public void showContacts(@Nullable List<? extends AddressBook> addressBooks, @Nullable List<? extends AddressBook> deptUser) {
		SpUtil.put(PreferencesUtils.HAS_SUBORDINATES, !CommonUtil.isEmptyList(addressBooks));
		SpUtil.put(PreferencesUtils.HAS_SUB_SUBORDINATES, !CommonUtil.isEmptyList(addressBooks) || !CommonUtil.isEmptyList(deptUser));
	}

	@Override
	public void showLoading() {

	}

	@Override
	public void hideLoading() {

	}
}
