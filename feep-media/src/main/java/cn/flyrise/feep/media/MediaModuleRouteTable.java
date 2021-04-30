package cn.flyrise.feep.media;

import cn.squirtlez.frouter.IRouteTable;
import cn.squirtlez.frouter.RouteManager;

/**
 * @author ZYP
 * @since 2017-12-06 11:54
 */
public class MediaModuleRouteTable implements IRouteTable {

	@Override public void registerTo(RouteManager manager) {
		manager.register("/media/image/big/browser", cn.flyrise.feep.media.images.BigImageBrowserActivity.class);
		manager.register("/media/image/browser", cn.flyrise.feep.media.images.ImageBrowserActivity.class);
		manager.register("/media/attachments", cn.flyrise.feep.media.attachments.AttachmentListActivity.class);
		manager.register("/media/file/select", cn.flyrise.feep.media.files.FileSelectionActivity.class);
		manager.register("/media/recorder", cn.flyrise.feep.media.record.RecordActivity.class);
		manager.register("/media/image/select", cn.flyrise.feep.media.images.ImageSelectionActivity.class);
		manager.register("/media/single/attachments", cn.flyrise.feep.media.attachments.SingleAttachmentActivity.class);
	}
}
