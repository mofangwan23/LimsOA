package cn.flyrise.feep.core.common;

import android.util.SparseArray;

import java.lang.ref.WeakReference;

/**
 * @author ZYP
 * @since 2016-12-13 09:06
 */
public class DataKeeper {

    private final SparseArray<WeakReference<Object>> mKeepDatas = new SparseArray<>();

    private DataKeeper() { }

    private static DataKeeper sInstance;

    public static DataKeeper getInstance() {
        if (sInstance == null) {
            sInstance = new DataKeeper();
        }
        return sInstance;
    }

    public void keepDatas(int key, Object value) {
        if (key == -1) return;
        WeakReference<Object> weakValue = new WeakReference<>(value);
        mKeepDatas.put(key, weakValue);
    }

    public Object getKeepDatas(int key) {
        WeakReference<Object> weakReference = mKeepDatas.get(key);
        return weakReference == null ? null : weakReference.get();
    }

    public void removeKeepData(int key) {
        mKeepDatas.remove(key);
    }
}
