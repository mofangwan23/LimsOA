package cn.flyrise.feep.core.network.callback;


import cn.flyrise.feep.core.network.RepositoryException;

/**
 * @author ZYP
 * @since 2016-11-14 11:21
 */
public abstract class AbstractCallback<T> implements Callback<T> {

    public static final int DEFAULT_KEY = 20161114;
    protected boolean isCancel;
    protected Class mClass;

    public AbstractCallback() {
        this(new Object());
    }

    public AbstractCallback(Object object) {
        mClass = object == null ? null : object.getClass();
    }

    @Override public void onPreExecute() { }

    @Override public void onFailure(RepositoryException repositoryException) { }

    @Override public boolean isCanceled() {
        return isCancel;
    }

    @Override public void cancel() {
        isCancel = true;
    }

    @Override public int key() {
        if (mClass == null) {
            return DEFAULT_KEY;
        }
//        if (mClass.getCanonicalName() == null || mClass.getCanonicalName().equals("")) {
//            return DEFAULT_KEY;
//        }
//        return mClass.getCanonicalName().hashCode();
        return mClass.hashCode();
    }
}
