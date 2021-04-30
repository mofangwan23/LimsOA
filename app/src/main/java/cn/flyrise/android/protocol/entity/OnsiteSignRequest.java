package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class OnsiteSignRequest extends RequestContent {

    private static final String NAME_SPACE = "LocationExpansionRequest";

    /**
     * 纬度
     */
    private String latitude;
    /**
     * 经度
     */
    private String longitude;
    private String name = "";
    private String guid = "";

    @Override
    public String getNameSpace () {
        return NAME_SPACE;
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

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getGUID () {
        return guid;
    }

    public void setGUID (String gUID) {
        guid = gUID;
    }

}
