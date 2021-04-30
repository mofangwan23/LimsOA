/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-8-16 下午1:26:00
 */
package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import java.util.ArrayList;

import cn.flyrise.android.protocol.entity.FormNodeResponse;
import cn.flyrise.android.protocol.model.ReferenceItem;

/**
 * 类功能描述：出口到节点的数据模型</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-8-16</br> 修改备注：</br>
 */
@Keep
public class FormExitToNodeItem {
    /** 出口数据 */
    private ReferenceItem                exitNodItem;
    /** 出口到节点响应 */
    private FormNodeResponse             formNodeResponse;

    /** 节点到人员的数据集 */
    private ArrayList<FormNodeToSubNode> nodeItems;

    public ReferenceItem getExitNodItem() {
        return exitNodItem;
    }

    public void setExitNodItem(ReferenceItem referenceItem) {
        this.exitNodItem = referenceItem;
    }

    public FormNodeResponse getFormNodeResponse() {
        return formNodeResponse;
    }

    public void setFormNodeResponse(FormNodeResponse formNodeResponse) {
        this.formNodeResponse = formNodeResponse;
    }

    public ArrayList<FormNodeToSubNode> getNodeItems() {
        return nodeItems;
    }

    public void setNodeItems(ArrayList<FormNodeToSubNode> nodeItems) {
        this.nodeItems = nodeItems;
    }

    /**
     * 添加节点
     */
    public void addNodeItem(FormNodeToSubNode nodeToSubNode) {
        if (nodeItems == null) {
            nodeItems = new ArrayList<> ();
        }
        nodeItems.add(nodeToSubNode);
    }

}
