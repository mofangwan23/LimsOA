package cn.flyrise.feep.collaboration.matter.model;


import android.support.annotation.Keep;

import java.util.List;

/**
 * Created by klc on 2017/5/12.
 */
@Keep
public class MatterPageInfo {

    public boolean hasMore;

    public int currentPage;

    public List<Matter> dataList;


}
