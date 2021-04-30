package cn.flyrise.feep.addressbook.model;

import cn.flyrise.android.protocol.model.CommonGroup;

/**
 * @author ZYP
 * @since 2018-03-23 16:27
 */
public class CommonGroupEvent {

	public CommonGroup commonGroup;
	public boolean hasChange;

	public CommonGroupEvent(CommonGroup position, boolean hasChange) {
		this.commonGroup = position;
		this.hasChange = hasChange;
	}

}
