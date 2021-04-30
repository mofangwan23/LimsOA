package cn.flyrise.android.protocol.entity.knowledge;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.knowledge.model.SearchFile;

/**
 * Created by klc on 2016/9/23.
 */

public class SearchFileResponse extends ResponseContent {

    public Result result;

    public Result getResult() {
        return result;
    }

    public class Result {
        private String pageNo;
        private String pageSize;
        private List<SearchFile> doc;
        private String numFound;

        public int getPageNo() {
            return Integer.valueOf(pageNo);
        }

        public int getPageSize() {
            return Integer.valueOf(pageSize);
        }

        public List<SearchFile> getDoc() {
            return doc;
        }

        public int getNumFound() {
            return Integer.valueOf(numFound);
        }
    }
}
