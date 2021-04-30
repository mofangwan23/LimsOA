package cn.flyrise.feep.core.network.callback;


import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2016-09-07 10:36
 */
public abstract class ResponseCallback<T extends ResponseContent> extends AbstractCallback<T> {

    public ResponseCallback() {
    }

    public ResponseCallback(Object object) {
        super(object);
    }

    @Override public abstract void onCompleted(T t);

}
