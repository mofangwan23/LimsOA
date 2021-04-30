package cn.flyrise.feep.core.network;

import okhttp3.Response;

/**
 * @author ZYP
 * @since 2016-09-13 09:15
 * 网络层异常信息的封装
 */
public class RemoteException implements RepositoryException {


    public static final int CODE_REQUEST_CANCEL = -99;
    public static final int CODE_MAC_CHECK_ERROR = 186;
    public static final int CODE_NET_ERROR = 188;
    public static final int CODE_UNKNOWN_ERROR = 189;

    private final int mErrorCode;
    private final String mMessage;
    private final boolean isReLogin;
    private final Response mResponse;
    private final Exception mException;
    private final boolean isLoadLogout;

    private RemoteException(Builder builder) {
        mMessage = builder.message;
        isReLogin = builder.isReLogin;
        mResponse = builder.response;
        mException = builder.exception;
        isLoadLogout = builder.isLoadLogout;
        mErrorCode = (builder.errorCode() != 0) ? builder.errorCode() : (mResponse != null) ? mResponse.code() : -1;
    }

    @Override public boolean isReLogin() {
        return this.isReLogin;
    }

    @Override public boolean isLoadLogout() {
        return isLoadLogout;
    }

    @Override public int errorCode() {
        return mErrorCode;
    }

    @Override public String errorMessage() {
        return this.mMessage;
    }

    @Override public Exception exception() {
        return this.mException;
    }

    public static class Builder {

        private int errorCode;
        private String message;
        private boolean isReLogin;
        private Response response;
        private Exception exception;
        private boolean isLoadLogout;

        public Builder errorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder isLoadLogout(boolean isLoadLogout) {
            this.isLoadLogout = isLoadLogout;
            return this;
        }

        public Builder canceled(boolean isCanceled) {
            this.errorCode = isCanceled ? RemoteException.CODE_REQUEST_CANCEL : 0;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder isReLogin(boolean isReLogin) {
            this.isReLogin = isReLogin;
            return this;
        }

        public Builder response(Response response) {
            this.response = response;
            return this;
        }

        public Builder exception(Exception exception) {
            this.exception = exception;
            return this;
        }

        public int errorCode() {
            return this.errorCode;
        }

        public RemoteException build() {
            return new RemoteException(this);
        }
    }

}
