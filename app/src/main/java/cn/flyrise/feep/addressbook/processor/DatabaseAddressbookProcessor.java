package cn.flyrise.feep.addressbook.processor;

import cn.flyrise.feep.K;
import cn.flyrise.feep.addressbook.model.ExtractInfo;
import cn.flyrise.feep.core.common.utils.SpUtil;

/**
 * @author ZYP
 * @since 2017-02-09 11:16
 */
public class DatabaseAddressbookProcessor extends AddressBookProcessor {

    @Override public void dispose(ExtractInfo extractInfo) {
        SpUtil.put(K.preferences.address_book_version, extractInfo.name);
        if (mDisposeListener != null) {
            mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_INIT_SUCCESS, ADDRESS_BOOK_SOURCE_DB));
        }
    }
}
