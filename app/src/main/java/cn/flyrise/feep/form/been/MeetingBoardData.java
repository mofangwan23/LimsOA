/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-11-6 下午9:09:25
 */

package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-11-6</br> 修改备注：</br>
 */
@Keep
public class MeetingBoardData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String            uiControlId;

    private String            roomId;

    private String            roomName;

    private String            start;

    private String            end;

    private String getUiControlId () {
        return uiControlId;
    }

    private void setUiControlId (String uiControlId) {
        this.uiControlId = uiControlId;
    }

    private String getRoomId () {
        return roomId;
    }

    private void setRoomId (String roomId) {
        this.roomId = roomId;
    }

    private String getRoomName () {
        return roomName;
    }

    private void setRoomName (String roomName) {
        this.roomName = roomName;
    }

    private String getStart () {
        return start;
    }

    private void setStart (String start) {
        this.start = start;
    }

    private String getEnd () {
        return end;
    }

    private void setEnd (String end) {
        this.end = end;
    }

    // 组包
    public JSONObject getProperties() {
        final JSONObject childObject = new JSONObject();
        try {
            childObject.put("uiControlId", getUiControlId());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            childObject.put("roomId", getRoomId());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            childObject.put("roomName", getRoomName());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            childObject.put("start", getStart());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            childObject.put("end", getEnd());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return childObject;
    }

    /**
     * 解析json
     *
     * @throws JSONException
     */
    public void parseReponseJson(String jsonStr) throws JSONException {
        final JSONObject properties = new JSONObject(jsonStr);
        try {
            final JSONObject parentObj = properties.getJSONObject("userInfo");
            if (parentObj != null) {
                JSONObject childObj = null;
                try {
                    childObj = parentObj.getJSONObject("meetingBoardData");
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                if (childObj != null) {
                    try {
                        setUiControlId(childObj.getString("uiControlId"));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        setRoomId(childObj.getString("roomId"));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        setRoomName(childObj.getString("roomName"));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        setStart(childObj.getString("start"));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        setEnd(childObj.getString("end"));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

}
