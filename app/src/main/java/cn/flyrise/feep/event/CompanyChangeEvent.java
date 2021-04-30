package cn.flyrise.feep.event;

import cn.flyrise.feep.addressbook.model.Department;

/**
 * @author 社会主义接班人
 * @since 2018-08-03 17:56
 */
public class CompanyChangeEvent {

	public String selectedCompanyId;

	public String selectedCompanyName;

	public CompanyChangeEvent(Department company) {
		this.selectedCompanyId = company.deptId;
		this.selectedCompanyName = company.name;
	}



	public CompanyChangeEvent() { }

}
