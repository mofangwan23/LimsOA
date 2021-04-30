package cn.flyrise.feep.collaboration.utility;

import android.content.Context;
import android.content.Intent;

import cn.flyrise.android.protocol.entity.CollaborationDetailsResponse;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.activity.WorkFlowActivity;
import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.collaboration.model.FileInfo;
import cn.flyrise.feep.commonality.WebViewShowActivity;
import cn.flyrise.feep.core.common.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class CollaborationDetailHelper {

    /**
     * 将附件的通信模型转换为界面的模型
     *
     * @param attachments
     */
    public static ArrayList<FileInfo> convertAttachment(List<AttachmentBean> attachments) {
        final ArrayList<FileInfo> fileInfos = new ArrayList<>();
        for (final AttachmentBean attachment : attachments) {
            final FileInfo fileInfo = new FileInfo();
            fileInfo.setLocalFile();
            fileInfo.setDetailAttachment(attachment);
            fileInfo.setType(FileUtil.getFileType(attachment.name));
            fileInfo.setFileName(attachment.name);
            fileInfos.add(fileInfo);
        }
        return fileInfos;
    }

    /**
     * 跳转到查看流程页面
     */
    public static void showFlowActivity(Context context, CollaborationDetailsResponse detailsRs) {
        if (detailsRs == null) {
            return;
        }
        final Intent intent = new Intent();
        if (detailsRs.getType() == 0) {
            WorkFlowActivity.setInitData(detailsRs.getFlow(), detailsRs.getCurrentFlowNodeGUID());
            WorkFlowActivity.setFunction(WorkFlowActivity.COLLABORATION_SHOW);
            intent.setClass(context, WorkFlowActivity.class);
        } else {
            final String formFlowUrl = detailsRs.getFormFlowUrl();
            intent.putExtra(K.form.URL_DATA_KEY, formFlowUrl);
            intent.putExtra(K.form.TITLE_DATA_KEY, context.getString(R.string.flow_titleshow));
            intent.putExtra(K.form.LOAD_KEY, true);
            intent.setClass(context, WebViewShowActivity.class);
        }
        context.startActivity(intent);
    }

    public static ArrayList<Matter> relationItemToMatter(List<AttachmentBean> relationItems) {
        ArrayList<Matter> matterList = new ArrayList<>();
        for (AttachmentBean relationItem : relationItems) {
            Matter matter = new Matter();
            matter.matterType = Integer.valueOf(relationItem.type);
            matter.title = relationItem.name;
            if (matter.matterType == MatterListActivity.MATTER_KNOWLEDGE) {
                matter.id = relationItem.guid;
            } else {
                matter.id = relationItem.id;
            }
            matter.masterKey = relationItem.masterkey;
            matterList.add(matter);
        }
        return matterList;
    }


}
