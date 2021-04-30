package cn.flyrise.android.protocol.entity.knowledge;


import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.knowledge.model.FileAndFolder;

/**
 * Created by k on 2016/9/7.
 */
public class FolderAndFileListResponse extends ResponseContent {

    private Result result;

    public Result getResult() {
        return result;
    }

    public class Result {

        public boolean firstfolder = true;

        private String totalPage;

        private List<FileAndFolder> list;

        public void setTotalPage(String totalPage) {
            this.totalPage = totalPage;
        }

        public void setList(List<FileAndFolder> list) {
            this.list = list;
        }

        public String getTotalPage() {
            return totalPage;
        }

        public List<FileAndFolder> getList() {
            return list;
        }
    }
}
