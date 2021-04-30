/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-8-18 上午11:55:37
 */
package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.protocol.model.ReferenceItem;

/**
 * 类功能描述：人员节点的信息</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-8-18</br> 修改备注：</br>
 */
@Keep
public class FormSubNodeInfo {
    /** 节点数据 */
    private FormNodeItem        nodeItem;
    private ReferenceItem       referenceItem;
    private boolean             isNeedAddState = true;
    private int nodeType;

    public FormNodeItem getNodeItem() {
        return nodeItem;
    }

    public void setNodeItem(FormNodeItem nodeItem) {
        this.nodeItem = nodeItem;
    }

    public ReferenceItem getReferenceItem() {
        return referenceItem;
    }

    public void setReferenceItem(ReferenceItem referenceItem) {
        this.referenceItem = referenceItem;
    }

    public boolean isNeedAddState() {
        return isNeedAddState;
    }

    public void setNeedAddState(boolean isNeedAddState) {
        this.isNeedAddState = isNeedAddState;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

}
