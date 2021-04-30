package cn.flyrise.feep.location.util;

import java.util.Comparator;

import cn.flyrise.feep.location.bean.LocusPersonLists;

public class PinyinComparator implements Comparator<LocusPersonLists> {

    @Override
    public int compare(LocusPersonLists o1, LocusPersonLists o2) {
        if ("@".equals(o1.getIsChar()) || "#".equals(o2.getIsChar())) {
            return -1;
        } else if ("#".equals(o1.getIsChar()) || "@".equals(o2.getIsChar())) {
            return 1;
        } else {
            return o1.getIsChar().compareTo(o2.getIsChar());
        }
    }

}
