package cn.flyrise.feep.knowledge.contract;

import java.util.List;

import cn.flyrise.feep.knowledge.model.PubAndRecFile;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;

/**
 * Created by KLC on 2016/12/6.
 */

public interface PubAndRecListContract {

    interface View<T> extends ListContract.View<T> {
        void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener);
    }

    interface Presenter {
        void refreshList();

        void loadMoreData();

        void cancelPublish(List<PubAndRecFile> dataList);

        boolean hasMore();
    }

    interface LoadListCallback<T> {

        void loadListDataSuccess(List<T> dataList, int totalPage);

        void loadListDataError();
    }

}