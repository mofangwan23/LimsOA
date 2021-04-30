/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-8
 */
package cn.flyrise.feep.collaboration.view.workflow;

import cn.flyrise.feep.collaboration.view.Avatar;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 流程图内部节点数据模型 </div>
 *
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class WorkFlowNode implements Serializable {
    private static final long serialVersionUID = -1128273411468436708L;
    private String ggId;
    private String nodeId;
    private String nodeName;
    private NodeType nodeType = NodeType.normal;
    private int status;
    private String wflag;
    private int type;
    private boolean endorse;
    private String endorseby = "";
    private boolean newnode;
    private WorkFlowNode parent;
    private Avatar avatar;
    // private boolean lock = false;
    private int nodeX;
    private int nodeY;
    private String imageHref;

    public String getGgId() {
        return ggId;
    }

    public void setGgId(String ggId) {
        this.ggId = ggId;
    }

    public String getWflag() {
        return wflag;
    }

    public void setWflag(String wflag) {
        this.wflag = wflag;
    }

    public String getImageHref() {
        return imageHref;
    }

    public void setImageHref(String imageHref) {
        this.imageHref = imageHref;
    }

    private List<WorkFlowNode> childNodes = new ArrayList<>();
    @Deprecated
    private Object carryInfo;

    public enum NodeType {
        /*--
         * 0-新建出来默认
         * 1-刚添加新来的
         * 2-加签时用,好像没用过...
         * 3-加签时已有节点设置为锁定
         * 4-标记用户节点
         * 5-就是unlock...
         */
        normal(0), adding(1), unlock(2), locked(3), user(4), existed(5);
        private final int value;

        NodeType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isEndorse() {
        return endorse;
    }

    public void setEndorse(boolean endorse) {
        this.endorse = endorse;
    }

    public String getEndorseby() {
        return endorseby;
    }

    public void setEndorseby(String endorseby) {
        this.endorseby = endorseby;
    }

    public boolean isNewnode() {
        return newnode;
    }

    public void setNewnode(boolean newnode) {
        this.newnode = newnode;
    }

    public WorkFlowNode getParent() {
        return parent;
    }

    public void setParent(WorkFlowNode parent) {
        this.parent = parent;
    }

    public int getNodeX() {
        return nodeX;
    }

    public void setNodeX(int nodeX) {
        this.nodeX = nodeX;
    }

    public int getNodeY() {
        return nodeY;
    }

    public void setNodeY(int nodeY) {
        this.nodeY = nodeY;
    }

    public List<WorkFlowNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<WorkFlowNode> childNodes) {
        this.childNodes = childNodes;
    }

    @Deprecated
    public Object getCarryInfo() {
        return carryInfo;
    }

    @Deprecated
    public void setCarryInfo(Object carryInfo) {
        this.carryInfo = carryInfo;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "--" + getNodeName() + "--nodeId:" + getNodeId() + "--nodeType:" + getNodeType() + "--status:" + getStatus() + "--type:" + getType()
                + "---endorse:" + isEndorse() + "--newnode" + isNewnode() + "--parent:" + getParent() + "--childNodes:" + getChildNodes().size()
                + "--avatar:" + getAvatar();
    }
}
