package cn.flyrise.android.protocol.entity.knowledge;


import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;

/**
 * Created by k on 2016/9/7.
 */
public class PublishAndReceiveListResponse extends ResponseContent {

    private Result result ;

    public Result getResult() {
        return result;
    }

    public class Result{

        @SerializedName("TotalPage")
        private int totalPage;

        private List<PubAndRecFile> list;

        public void setTotalPage(int totalPage) {
            this.totalPage = totalPage;
        }

        public void setList(List<PubAndRecFile> list) {
            this.list = list;
        }

        public int getTotalPage() {
            return totalPage;
        }

        public List<PubAndRecFile> getList() {
            return list;
        }
    }
}
