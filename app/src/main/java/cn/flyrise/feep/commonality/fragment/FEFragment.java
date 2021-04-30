/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-28 上午9:43:35
 */
package cn.flyrise.feep.commonality.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * 类功能描述：</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-2-28</br> 修改备注：我的意见这个可以入library库--017</br>
 * 傻逼~
 */
public class FEFragment extends Fragment {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        bindData();
        setListener();
    }

    protected void findView () {
    }

    /**
     * 建议在此处执行控件对象数据绑定,于findView()之后自动调用
     */
    protected void bindData () {
    }

    /**
     * 建议在此处执行控件对象监听器设置,于bindData()之后自动调用
     */
    protected void setListener () {
    }

    /**
     * 根据Id查询View
     */
    protected View findViewById (int id) {
        if (getView() == null) {
            return null;
        }
        return getView().findViewById(id);
    }
}
