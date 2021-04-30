package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2017-02-15 15:12
 * 常用联系人、我的关注
 */
public class CommonTagResponse extends ResponseContent {

    /**
     * 用户 ids
     */
    public List<String> result;

}
