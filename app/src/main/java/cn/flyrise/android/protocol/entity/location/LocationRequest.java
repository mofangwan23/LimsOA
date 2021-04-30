package cn.flyrise.android.protocol.entity.location;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-7-15 下午2:48:44 <br/>
 *          类说明 :
 */
public class LocationRequest extends RequestContent {
    public static final String NAMESPACE = "LocationRequest";

    /**
     * 纬度
     */
    private String latitude;
    /**
     * 经度
     */
    private String longitude;
    private String address = "";
    private String name = "";
    private String sendType = "0";
    private String guid;
    private String userId;
    private String forced;
    private String accessToken;
    private String pdesc;                         // 图片的描述内容

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getLatitude () {
        return latitude;
    }

    public void setLatitude (String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude () {
        return longitude;
    }

    public void setLongitude (String longitude) {
        this.longitude = longitude;
    }

    public String getAddress () {
        return address;
    }

    public void setAddress (String address) {
        if (address == null) {
            return;
        }
        this.address = address;
    }

    public String getSendType () {
        return sendType;
    }

    public void setSendType (String sendType) {
        this.sendType = sendType;
    }

    @Override
    public String toString () {
        return "{latitude:" + latitude + "; longitude:" + longitude + "; address:" + address + "; sendType:" + sendType + "; name:" + name + "}";
    }

    public String getGuid () {
        return guid;
    }

    public void setGuid (String guid) {
        this.guid = guid;
    }

    public String getUserId () {
        return userId;
    }

    public void setUserId (String userId) {
        this.userId = userId;
    }

    public String getForced () {
        return forced;
    }

    public void setForced (String forced) {
        this.forced = forced;
    }

    public String getAccessToken () {
        return accessToken;
    }

    public void setAccessToken (String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPdesc () {
        return pdesc;
    }

    public void setPdesc (String pdesc) {
        this.pdesc = pdesc;
    }

}
