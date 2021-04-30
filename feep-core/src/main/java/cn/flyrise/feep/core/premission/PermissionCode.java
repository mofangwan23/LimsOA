package cn.flyrise.feep.core.premission;

import android.content.Context;

import cn.flyrise.feep.core.R;

/**
 * @author ZYP
 * @since 2016-09-20 18:12
 */
public class PermissionCode {

    public static final int BASE = 110;                     // 存储 和 设备信息的请求码
    public static final int CONTACTS = 111;                 // 通讯录
    public static final int CALENDAR = 112;                 // 日历
    public static final int CAMERA = 113;                   // 相机
    public static final int LOCATION = 114;                 // 位置
    public static final int RECORD = 115;                   // 录音
    public static final int SMS = 116;                       // 短信
    public static final int RECORD_AUDIO = 117;             // 音频

    public static String getPromptMessage(Context context, String permission) {

        if (permission.contains("STORAGE")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_storage);
        }

        if (permission.contains("PHONE")
                || permission.contains("READ_CALL_LOG")
                || permission.contains("WRITE_CALL_LOG")
                || permission.contains("USE_SIP")
                || permission.contains("PROCESS_OUTGOING_CALLS")
                || permission.contains("ADD_VOICEMAIL")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_phone);
        }

        if (permission.contains("CONTACTS") || permission.contains("GET_ACCOUNTS")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_contact);
        }

        if (permission.contains("CALENDAR")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_calendar);
        }

        if (permission.contains("CAMERA")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_camera);
        }

        if (permission.contains("LOCATION")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_location);
        }

        if (permission.contains("RECORD_AUDIO")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_record);
        }

        if (permission.contains("SMS")
                || permission.contains("RECEIVE_MMS")
                || permission.contains("RECEIVE_WAP_PUSH")
                || permission.contains("READ_CELL_BROADCASTS")) {
            return context.getResources().getString(R.string.permission_msg_request_failed_sms);
        }

        return context.getResources().getString(R.string.permission_msg_request_failed);
    }


}
