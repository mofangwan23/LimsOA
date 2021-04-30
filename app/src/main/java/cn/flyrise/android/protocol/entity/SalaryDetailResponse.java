package cn.flyrise.android.protocol.entity;

import java.util.List;
import java.util.Map;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2017-02-19 20:46
 */
public class SalaryDetailResponse extends ResponseContent {

    public List<Map<String, String>> add;
    public List<Map<String, String>> subtract;
    public List<Map<String, String>> other;

}
