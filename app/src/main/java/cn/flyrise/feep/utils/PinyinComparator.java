package cn.flyrise.feep.utils;

import java.util.Comparator;

import cn.flyrise.feep.dbmodul.table.AddressBookTable;

public class PinyinComparator implements Comparator<AddressBookTable> {

    @Override public int compare(AddressBookTable o1, AddressBookTable o2) {
        final String addo1 = o1.py.toLowerCase();
        final String addo2 = o2.py.toLowerCase();
        if ("@".equals(addo1) || "#".equals(addo2)) {
            return -1;
        } else if ("#".equals(addo1) || "@".equals(addo2)) {
            return 1;
        } else {
            return addo1.compareTo(addo2);
        }
    }

}
