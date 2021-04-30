package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.addressbook.model.AddressBookVO;

public class UserDetailsResponse extends ResponseContent {
    private AddressBookVO result;

    public AddressBookVO getResult () {
        return result;
    }

    public void setResult (AddressBookVO result) {
        this.result = result;
    }
}
