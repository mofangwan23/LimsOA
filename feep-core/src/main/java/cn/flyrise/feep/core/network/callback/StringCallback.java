package cn.flyrise.feep.core.network.callback;

/**
 * @author ZYP
 * @since 2016-09-14 14:56
 */
public abstract class StringCallback extends AbstractCallback<String> {

    public StringCallback() {
    }

    public StringCallback(Object object) {
        super(object);
    }

    @Override public abstract void onCompleted(String s);

}
