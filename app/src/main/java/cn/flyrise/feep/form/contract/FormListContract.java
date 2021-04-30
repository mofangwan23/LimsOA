package cn.flyrise.feep.form.contract;

import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.android.protocol.model.FormTypeItem;

/**
 * Created by KLC on 2016/12/28.
 */

public class FormListContract {

    public interface View extends FEListContract.View<FormTypeItem> {
        void finish();
    }

    public interface Presenter extends FEListContract.Presenter {

        void refreshListData(String searchKey, String formListID);

        void getListDataForFormID(String formListID);

        void onBackToParent();
    }
}
