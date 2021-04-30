package cn.flyrise.feep.addressbook.model;

/**
 * @author ZYP
 * @since 2016-12-05 14:28
 */
public class Department {

    public String deptId;       // 部门 id
    public String fatherId;     // 父部门 id
    public String name;         // 部门名称
    public int level;           // 部门级别
    public String grade;        // 真*部门级别 001 001001 001001002

    public Department() {
    }

    public Department(String deptId, String name) {
        this.deptId = deptId;
        this.name = name;
    }

    public Department(String deptId, String name, int level) {
        this.deptId = deptId;
        this.name = name;
        this.level = level;
    }

    public Department(String deptId, String name, int level, String fatherId) {
        this.deptId = deptId;
        this.name = name;
        this.level = level;
        this.fatherId = fatherId;
    }

    @Override public String toString() {
        return "Department{" +
                "deptId='" + deptId + '\'' +
                ", fatherId='" + fatherId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Department that = (Department) object;

        if (!deptId.equals(that.deptId)) return false;
        return name.equals(that.name);

    }

    @Override public int hashCode() {
        int result = deptId.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
