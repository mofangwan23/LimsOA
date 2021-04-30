package cn.flyrise.feep.addressbook.processor;

import static cn.flyrise.feep.addressbook.model.ExtractInfo.TYPE_DB;
import static cn.flyrise.feep.addressbook.model.ExtractInfo.TYPE_JSON;
import static cn.flyrise.feep.addressbook.model.ExtractInfo.TYPE_SQL;

import android.os.Handler;
import android.os.Looper;
import cn.flyrise.feep.addressbook.model.ExtractInfo;

/**
 * @author ZYP
 * @since 2017-02-09 11:15
 * 负责处理下载的通讯录 zip 文件。
 */
public abstract class AddressBookProcessor {

    /**
     * 状态：通讯录正在下载
     */
    public static final int ADDRESS_BOOK_DOWNLOADING = 0;

    /**
     * 状态：通讯录正在下载，同时存在可用旧数据
     */
    public static final int ADDRESS_BOOK_DOWNLOADING_UPDATE = 1;

    /**
     * 状态：通讯录下载并初始化成功
     */
    public static final int ADDRESS_BOOK_INIT_SUCCESS = 2;

    /**
     * 状态：通讯录更新失败
     */
    public static final int ADDRESS_BOOK_UPDATE_FAILED = 3;

    /**
     * 状态：通讯录下载失败
     */
    public static final int ADDRESS_BOOK_DOWNLOAD_FAILED = 4;

    /**
     * 状态：通讯录解压失败
     */
    public static final int ADDRESS_BOOK_UNZIP_FAILED = 5;

    /**
     * 状态：通讯录解密失败
     */
    public static final int ADDRESS_BOOK_DECRYPT_FAILED = 6;

    /**
     * 通讯录数据来源：json 文件
     */
    public static final int ADDRESS_BOOK_SOURCE_JSON = 7;

    /**
     * 通讯录数据来源：database 文件
     */
    public static final int ADDRESS_BOOK_SOURCE_DB = 8;

    /**
     * 广播意图：通讯录下载结果
     */
    public static final String ADDRESS_BOOK_DOWNLOAD_ACTION = "fly_rise_address_book_download_action";


    protected final Handler mHandler = new Handler(Looper.getMainLooper());

    public interface IDisposeListener {

        void onDisposeSuccess(int resultCode, int sourceType);

        void onDisposeFailed(int errorCode);

    }

    protected String mUserId;
    protected IDisposeListener mDisposeListener;

    public void serUserId(String userId) {
        this.mUserId = userId;
    }

    public void setDisposeListener(IDisposeListener listener) {
        this.mDisposeListener = listener;
    }


    /**
     * 处理下载的 zip 文件
     * @param extractInfo 解压后的文件信息
     */
    public abstract void dispose(ExtractInfo extractInfo);

    public static AddressBookProcessor build(byte processorType) {
        AddressBookProcessor processor = null;
        if (TYPE_JSON == processorType) {
            processor = new JsonAddressBookProcessor();
        }
        else if (TYPE_DB == processorType) {
            processor = new DatabaseAddressbookProcessor();
        }
        else if (TYPE_SQL == processorType) {
            processor = new SqlAddressBookProcessor();
        }

        if (processor == null) {
            throw new NullPointerException("The processor type is wrong, cannot create the correct processor.");
        }
        return processor;
    }
}
