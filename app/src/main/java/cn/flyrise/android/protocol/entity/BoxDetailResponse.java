package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.android.protocol.model.Mail;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2016/7/13 09:51
 */
public class BoxDetailResponse extends ResponseContent {

    /**
     * 总共有多少条记录
     */
    public int total;

    /**
     * 当前是第几页
     */
    public int currentPage;

    /**
     * 总共有多少页
     */
    @SerializedName("PageCount") public int pageCount;

    /**
     * 邮件列表
     */
    public List<Mail> mailList;


}
