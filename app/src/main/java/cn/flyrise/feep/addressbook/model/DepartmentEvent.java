package cn.flyrise.feep.addressbook.model;

/**
 * @author ZYP
 * @since 2016-12-07 17:44
 */
public class DepartmentEvent {
    public Department department;
    public boolean hasChange;
    public boolean refresh;

    public DepartmentEvent(Department department, boolean hasChange) {
        this(department, hasChange, false);
    }

    public DepartmentEvent(Department department, boolean hasChange, boolean refresh) {
        this.department = department;
        this.hasChange = hasChange;
        this.refresh = refresh;
    }
}
