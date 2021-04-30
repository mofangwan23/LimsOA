package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2017-02-19 20:45
 */
public class SalaryVerifyResponse extends ResponseContent {

    /**
     * 1. 通过校验
     * 0. 校验失败
     */
    public String code;

}
