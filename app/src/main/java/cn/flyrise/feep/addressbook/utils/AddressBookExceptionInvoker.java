package cn.flyrise.feep.addressbook.utils;

import android.app.Activity;
import android.text.TextUtils;

import cn.flyrise.feep.addressbook.processor.AddressBookProcessor;
import java.io.File;

import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.dbmodul.table.ContactsVerionsTable;
import cn.flyrise.feep.dbmodul.utils.ContactsVersionUtils;

/**
 * @author ZYP
 * @since 2017-03-07 19:45
 * 通讯录下载失败的异常处理器
 */
public class AddressBookExceptionInvoker {

    public static void showAddressBookExceptionDialog(Activity activity) {
        new FEMaterialDialog.Builder(activity)
                .setCancelable(false)
                .setMessage(CommonUtil.getString(R.string.lbl_text_contact_download_error))
                .setPositiveButton(null, dialog -> activity.finish())
                .build()
                .show();
    }

    /**
     * 通讯录下载失败：可能下载失败，可能解压失败，尝试在本地查找旧的数据
     * @return -1 尝试使用旧版本通讯录失败
     */
    public static int tryRestoreOldVersion() {
        String databasePath = getDatabaseInSdcard();        // 检查 sdcard 是否存在可用数据库文件
        if (!TextUtils.isEmpty(databasePath)) {
            return AddressBookProcessor.ADDRESS_BOOK_SOURCE_DB;
        }

        SpUtil.put(K.preferences.address_book_version, "");                                     // 不存在外部数据库文件
        ContactsVerionsTable versionTable = ContactsVersionUtils.select();                      // 旧的数据库通讯录机制
        if (versionTable != null && !TextUtils.isEmpty(versionTable.userId)) {                  // 存在 userId
            String userId = "";                                                                      // 当前登录用户
            if(CoreZygote.getLoginUserServices()!=null){
                userId = CoreZygote.getLoginUserServices().getUserId();
            }
            if (!TextUtils.isEmpty(userId) && TextUtils.equals(versionTable.userId, userId)) {  // 相同的用户，可以使用旧的数据库机制
                return AddressBookProcessor.ADDRESS_BOOK_SOURCE_JSON;
            }
        }
        return -1;
    }


    private static String getDatabaseInSdcard() {
        String databasePath = null;
        String addressBookPath = CoreZygote.getPathServices().getAddressBookPath();
        File addressBookDir = new File(addressBookPath);
        if (!addressBookDir.exists()) {
            return null;
        }

        File[] files = addressBookDir.listFiles();
        if (files == null || files.length == 0) return null;

        String databaseName = SpUtil.get(K.preferences.address_book_version, "");   // 必须得知道数据库的名字啊
        if (TextUtils.isEmpty(databaseName)) return null;

        for (File file : files) {
            String fileName = file.getName();
            if (TextUtils.equals(databaseName, fileName)) {
                databasePath = file.getAbsolutePath();
                break;
            }
        }

        return databasePath;
    }
}
