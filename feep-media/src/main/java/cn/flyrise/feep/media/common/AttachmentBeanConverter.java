package cn.flyrise.feep.media.common;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZYP
 * @since 2018-07-03 11:11
 */
public class AttachmentBeanConverter {

	/**
	 * 将远程附件 AttachmentBean 转化成 NetworkAttachment ，用于数据展示和下载
	 */
	public static List<NetworkAttachment> convert(List<AttachmentBean> attachmentBeans) {
		if (CommonUtil.isEmptyList(attachmentBeans)) {
			return null;
		}

		final String serverAddress = CoreZygote.getLoginUserServices().getServerAddress();
		List<NetworkAttachment> networkAttachments = new ArrayList<>(attachmentBeans.size());
		for (AttachmentBean attachment : attachmentBeans) {
			NetworkAttachment networkAttachment = new NetworkAttachment();
			networkAttachment.setId(attachment.id);
			networkAttachment.name = attachment.name;
			networkAttachment.path = serverAddress + attachment.href;

			String type = attachment.type;
			networkAttachment.type = (TextUtils.equals(type, "0") || TextUtils.isEmpty(type))
					? FileCategoryTable.getType(attachment.name)
					: type;

			networkAttachment.size = formatSizeToLong(attachment.size);
			networkAttachment.su00 = attachment.su00;
			networkAttachment.attachPK = attachment.attachPK;
			networkAttachment.fileGuid = attachment.fileGuid;
			networkAttachments.add(networkAttachment);
		}
		return networkAttachments;
	}

	/**
	 * 将 size 格式化成 long 类型
	 */
	private static long formatSizeToLong(String size) {
		if (TextUtils.isEmpty(size)) return 0;
		try {
			String regex = "([0-9.]+)([a-zA-Z]+)";
			Pattern pattern = Pattern.compile(regex);
			size = size.replaceAll(" ", "");
			Matcher matcher = pattern.matcher(size);

			if (!matcher.find()) {
				return CommonUtil.parseInt(size);
			}

			float num = CommonUtil.parseFloat(matcher.group(1));    // 数字
			long value = 1;
			String cap = matcher.group(2).toLowerCase();
			if (TextUtils.equals(cap, "kb") || TextUtils.equals(cap, "k")) {
				value = 1024;
			}
			else if (TextUtils.equals(cap, "mb") || TextUtils.equals(cap, "m")) {
				value = 1024 * 1024;
			}
			else if (TextUtils.equals(cap, "gb") || TextUtils.equals(cap, "g")) {
				value = 1024 * 1024 * 1024;
			}
			return (long) (value * num);
		} catch (Exception exp) {
			return 0;
		}
	}

}
