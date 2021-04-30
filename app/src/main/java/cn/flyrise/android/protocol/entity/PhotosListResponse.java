package cn.flyrise.android.protocol.entity;

import java.util.Map;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.addressbook.model.PhotoVO;

public class PhotosListResponse extends ResponseContent {
    private Map<String, PhotoVO> results;

    public Map<String, PhotoVO> getResults () {
        return results;
    }

    public void setResults (Map<String, PhotoVO> results) {
        this.results = results;
    }
}
