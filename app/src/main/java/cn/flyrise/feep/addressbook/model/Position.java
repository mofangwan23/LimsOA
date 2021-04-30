package cn.flyrise.feep.addressbook.model;

/**
 * @author ZYP
 * @since 2016-12-06 18:39
 */
public class Position {

    public String posId;        // 岗位 id
    public String position;     // 岗位名称

    public Position() {
    }

    public Position(String posId, String position) {
        this.posId = posId;
        this.position = position;
    }

    @Override public String toString() {
        return "Position{" +
                "posId='" + posId + '\'' +
                ", commonGroup='" + position + '\'' +
                '}';
    }
}
