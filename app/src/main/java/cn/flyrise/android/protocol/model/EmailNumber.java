package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2016/7/13 10:07
 */
public abstract class EmailNumber {

    public static final String INBOX_INNER = "InBox/Inner";     // 内部收件箱
    public static final String INBOX = "InBox";                 // 外部收件箱
    public static final String DRAFT = "Draft";
    public static final String SENT = "Sent";
    public static final String TRASH = "Trash";

    public int read;        // 收件箱的未读邮件
    public int total;       // 收件箱的总邮件数
    public int size;        // 收件箱的占用空间(单位为k)

    public abstract String getType();

    public abstract String getBoxName();

    public abstract int getIcon();

    public class InBox extends EmailNumber {
        @Override public String getType() {
            return CommonUtil.getString(R.string.lbl_text_mail_box);
        }

        @Override public String getBoxName() {
            return INBOX_INNER;
        }

        @Override public int getIcon() {
            return R.drawable.icon_shoujian;
        }
    }

    public class Draft extends EmailNumber {
        @Override public String getType() {
            return CommonUtil.getString(R.string.lbl_text_mail_draft);
        }

        @Override public String getBoxName() {
            return DRAFT;
        }

        @Override public int getIcon() {
            return R.drawable.icon_caogaoxiang;
        }
    }

    public class Sent extends EmailNumber {
        @Override public String getType() {
            return CommonUtil.getString(R.string.lbl_text_mail_send);
        }

        @Override public String getBoxName() {
            return SENT;
        }

        @Override public int getIcon() {
            return R.drawable.icon_sends;
        }
    }

    public class Trash extends EmailNumber {
        @Override public String getType() {
            return CommonUtil.getString(R.string.lbl_text_mail_trash);
        }

        @Override public String getBoxName() {
            return TRASH;
        }

        @Override public int getIcon() {
            return R.drawable.icon_lajit;
        }
    }
}
