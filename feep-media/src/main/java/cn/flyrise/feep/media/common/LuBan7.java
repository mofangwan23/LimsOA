package cn.flyrise.feep.media.common;

import android.app.Activity;
import android.content.Intent;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.attachments.AttachmentListActivity;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-08 09:26
 */
public class LuBan7 {

	public static void pufferGrenades(Activity activity, List<String> localAttachments,
			List<NetworkAttachment> networkAttachments, int requestCode) {
		Intent intent = new Intent(activity, AttachmentListActivity.class);
		intent.putExtra("extra_except_path", new String[]{CoreZygote.getPathServices().getUserPath()});
		if (CommonUtil.nonEmptyList(localAttachments)) {
			intent.putStringArrayListExtra("extra_local_file", (ArrayList<String>) localAttachments);
		}

		if (CommonUtil.nonEmptyList(networkAttachments)) {
			intent.putParcelableArrayListExtra("extra_network_file", (ArrayList<NetworkAttachment>) networkAttachments);
		}
		activity.startActivityForResult(intent, requestCode);
	}

}
