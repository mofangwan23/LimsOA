/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-7-18 下午4:09:07
 */

package cn.flyrise.feep.collaboration.utility;

import java.util.HashMap;

/**
 * 类功能描述：数据保存类-代替intent（单实例）</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-7-18</br> 修改备注：</br>
 */
public class DataStack extends HashMap<Object, Object> {
    private static final long serialVersionUID = 1L;

    private static DataStack  instance;

    private DataStack() {
    }

    /**
     * 获取实例
     */
    public static DataStack getInstance() {
        if (instance == null) {
            instance = new DataStack();
        }
        return instance;
    }
}
