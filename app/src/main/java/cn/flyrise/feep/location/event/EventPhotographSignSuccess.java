package cn.flyrise.feep.location.event;

import cn.flyrise.feep.location.event.EventLocationSignSuccess;

/**
 * cm 2017-6-26.
 * 拍照签到成功
 */

public class EventPhotographSignSuccess {

    public boolean isSearchAvitity = false;//是否从搜索界面签到

    public boolean isTakePhotoError = false;//是否为异常拍照签到

    public EventLocationSignSuccess signSuccess;
}
