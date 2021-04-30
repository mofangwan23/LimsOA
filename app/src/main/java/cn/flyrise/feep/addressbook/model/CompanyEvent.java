package cn.flyrise.feep.addressbook.model;

/**
 * @author ZYP
 * @since 2016-12-07 17:46
 */
public class CompanyEvent {

    public Department company;
    public boolean hasChange;
    public boolean isOnlyOneCompany;

    public CompanyEvent(Department department, boolean hasChange, boolean isOnlyOneCompany) {
        this.company = department;
        this.hasChange = hasChange;
        this.isOnlyOneCompany = isOnlyOneCompany;
    }
}
