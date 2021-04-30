package cn.flyrise.feep.core.network;

/**
 * @author ZYP
 * @since 2016-09-13 09:11
 * 数据层异常信息的封装
 */
public interface RepositoryException {

    boolean isReLogin();        // 判断是否需要重新登录

    boolean isLoadLogout();        // 判断是否需要加载注销接口

    int errorCode();         // RemoteException 的话 errorCode = http status code，如果请求被取消，则 errorCode = -99

    String errorMessage();

    Exception exception();

}
