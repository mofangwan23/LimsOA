package cn.flyrise.feep.addressbook.model;

/**
 * @author ZYP
 * @since 2016-12-07 17:45
 */
public class SubDepartmentEvent {

    public boolean hasChange;
    public boolean parentEvent;
    public Department subDepartment;

    public SubDepartmentEvent(Department department) {
        this(false, department);
    }

    public SubDepartmentEvent(boolean parentEvent, Department subDepartment) {
        this(parentEvent, subDepartment, false);
    }

    public SubDepartmentEvent(boolean parentEvent, Department subDepartment, boolean hasChange) {
        this.parentEvent = parentEvent;
        this.subDepartment = subDepartment;
        this.hasChange = hasChange;
    }
}
