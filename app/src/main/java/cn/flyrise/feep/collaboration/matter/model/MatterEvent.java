package cn.flyrise.feep.collaboration.matter.model;


import android.support.annotation.Keep;

/**
 * Created by klc on 2017/5/22.
 */
@Keep
public class MatterEvent {

    public int type ;//0;add 1:delete

    public Matter association;

    public MatterEvent(int type, Matter association) {
        this.type = type;
        this.association = association;
    }
}
