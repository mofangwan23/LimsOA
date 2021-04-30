package cn.flyrise.feep.core.network.callback;

import cn.flyrise.feep.core.network.RepositoryException;

/**
 * @author ZYP
 * @since 2016-09-14 14:54
 */
public interface Callback<T> {

    void onPreExecute();

    void onCompleted(T t);

    /**
     * You can use the (link{@ResponseExceptionHandler}) to unified handle response exception.
     *
     * RepositoryExceptionHandler handler = new RepositoryExceptionHandler(context);
     * handler.handleRemoteException(responseException);
     */
    void onFailure(RepositoryException repositoryException);

    boolean isCanceled();

    void cancel();

    int key();

}
