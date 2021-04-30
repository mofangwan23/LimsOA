package cn.flyrise.android.protocol.model;

import java.io.Serializable;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-27 上午11:37:14 <br/>
 *          类说明 :
 */
public class User implements Serializable {
    private static final long serialVersionUID = 46L;
    private String id;
    private String name;

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

}
