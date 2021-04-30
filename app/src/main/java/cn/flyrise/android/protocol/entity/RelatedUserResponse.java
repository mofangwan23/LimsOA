package cn.flyrise.android.protocol.entity;

import java.util.ArrayList;

import cn.flyrise.android.protocol.model.User;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-27 上午11:36:15 <br/>
 *          类说明 :
 */
public class RelatedUserResponse extends ResponseContent {
    private ArrayList<User> users;

    public ArrayList<User> getUsers () {
        return users;
    }

    public void setUsers (ArrayList<User> users) {
        this.users = users;
    }

}
