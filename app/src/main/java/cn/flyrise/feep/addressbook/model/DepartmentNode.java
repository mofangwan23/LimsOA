package cn.flyrise.feep.addressbook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-02-16 15:28
 */
public class DepartmentNode {

    public boolean isExpand;                    // 该节点是否展开
    public Department value;                    // 部门节点
    private boolean leafNode;                   // 用户判断是否是叶子节点
    private List<DepartmentNode> children;      // 当前节点的子节点

    public void addChild(DepartmentNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public List<DepartmentNode> getChildren() {
        return children;
    }

    public void removeChildren() {
        children.clear();
        children = null;
    }

    public void setLeafNode(boolean isLeafNode) {
        this.leafNode = isLeafNode;
    }

    public boolean isLeafNode() {
        return leafNode;
    }

    public static DepartmentNode build(Department department, boolean isLeafNode) {
        DepartmentNode node = new DepartmentNode();
        node.value = department;
        node.setLeafNode(isLeafNode);
        return node;
    }

}
