package cn.flyrise.feep.core.network.request;

import com.google.gson.annotations.SerializedName;

public class Request<T extends RequestContent> {

    private String namespace;
    private String model;
    private String mobileVersion;
    private String version;
    private String resolution;

    @SerializedName ("query")
    private T reqContent;

    public T getReqContent () {
        return reqContent;
    }

    public void setReqContent (T query) {
        this.reqContent = query;
        this.namespace = query.getNameSpace ();
    }

    public String getNamespace () {
        return namespace;
    }

    public void setNamespace (String namespace) {
        this.namespace = namespace;
    }

    public String getModel () {
        return model;
    }

    public void setModel (String model) {
        this.model = model;
    }

    public String getMobileVersion () {
        return mobileVersion;
    }

    public void setMobileVersion (String mobileVersion) {
        this.mobileVersion = mobileVersion;
    }

    public String getVersion () {
        return version;
    }

    public void setVersion (String version) {
        this.version = version;
    }

    public String getResolution () {
        return resolution;
    }

    public void setResolution (String resolution) {
        this.resolution = resolution;
    }

}
