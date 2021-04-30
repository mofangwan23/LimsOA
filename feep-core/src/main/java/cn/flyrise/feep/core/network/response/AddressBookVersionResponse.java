package cn.flyrise.feep.core.network.response;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Create by cm132 on 2018/12/4.
 * Describe:数据库版本
 */
public class AddressBookVersionResponse extends ResponseContent {

	public int result = 56;//如果这个数大于0,则需要请求addressBook.zip,否则是不需要请求addressBook.zip,默认下载

}
