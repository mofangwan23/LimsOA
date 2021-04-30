package cn.flyrise.feep.collaboration.model;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2017-04-26 10:44
 */
@Keep
public class WaitingSend {
    public String id;
    public String title;
    @SerializedName("stime")
    public String sendTime;
    public String important;
    public boolean isCheck;
}
