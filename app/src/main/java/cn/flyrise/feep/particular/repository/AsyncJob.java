package cn.flyrise.feep.particular.repository;


import cn.flyrise.feep.core.network.callback.Callback;

/**
 * @author ZYP
 * @since 2016-10-24 15:27
 */
public abstract class AsyncJob<T> {

    public abstract void start(Callback<T> callback);

}
