package cn.flyrise.feep.form.model;


import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.android.protocol.entity.FormTypeResponse;
import cn.flyrise.android.protocol.entity.FromTypeListRequest;
import cn.flyrise.android.protocol.model.FormTypeItem;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * Created by KLC on 2017/1/13.
 */

public class FormListRepository {

    private Map<String, List<FormTypeItem>> listMap;

    public FormListRepository() {
        this.listMap = new HashMap<>();
    }

    public List<FormTypeItem> getLocalFormList(String formListID) {
        if (TextUtils.isEmpty(formListID))
            formListID = "root";
        return listMap.get(formListID);
    }

    public void requestFormList(int page, int perPageNum, String searchKey, final String formListID, FEListContract
            .LoadListCallback<FormTypeItem> callback) {
        final FromTypeListRequest formListRequest = new FromTypeListRequest();
        formListRequest.setPage(String.valueOf(page));
        formListRequest.setPerPageNums(String.valueOf(perPageNum));
        formListRequest.setSearchKey(searchKey);
        formListRequest.setFormListId(formListID);
        FEHttpClient.getInstance().post(formListRequest, new ResponseCallback<FormTypeResponse>(this) {
            @Override
            public void onCompleted(FormTypeResponse responseContent) {
                if ("0".equals(responseContent.getErrorCode())) {
                    int totalNum = Integer.valueOf(responseContent.getTotalNums());
                    List<FormTypeItem> listData = responseContent.getFormTypeItems();
                    callback.loadListDataSuccess(listData, totalNum);
                    if (TextUtils.isEmpty(searchKey)) {
                        String saveKey = TextUtils.isEmpty(formListID) ? "root" : formListID;
                        if (page == 1)
                            listMap.put(saveKey, listData);
                        else
                            listMap.get(saveKey).addAll(listData);
                    }
                }
                else
                    callback.loadListDataError();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callback.loadListDataError();
            }
        });
    }
}
