/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-8-16 下午1:33:16
 */
package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.FormNodeItem;

/**
 * 类功能描述：节点到人员的数据模型</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-8-16</br> 修改备注：</br>
 */
@Keep
public class FormNodeToSubNode {
    /** 节点数据 */
    private FormNodeItem          formNodeItem;
    /** 人员数据集合 */
    private List<FormSubNodeInfo> personSubNodes;
    /** 岗位数据集合 */
    private List<FormSubNodeInfo> positionSubNodes;

    public FormNodeItem getFormNodeItem() {
        return formNodeItem;
    }

    public void setFormNodeItem(FormNodeItem formNodeItem) {
        this.formNodeItem = formNodeItem;
    }

    public List<FormSubNodeInfo> getPersonSubNodes() {
        return personSubNodes;
    }

    public void setPersonSubNodes(List<FormSubNodeInfo> personSubNodes) {
        this.personSubNodes = personSubNodes;
    }

    public void addPersonSubNode(FormSubNodeInfo personSubNode) {
        if (personSubNodes == null) {
            personSubNodes = new ArrayList<> ();
        }
        this.personSubNodes.add(personSubNode);
    }

    public List<FormSubNodeInfo> getPositionSubNodes() {
        return positionSubNodes;
    }

    public void setPositionSubNodes(List<FormSubNodeInfo> positionSubNodes) {
        this.positionSubNodes = positionSubNodes;
    }

}
