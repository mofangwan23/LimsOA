package cn.flyrise.feep.addressbook.source;

import android.text.TextUtils;

import java.util.Comparator;

import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * @author ZYP
 * @since 2017-05-26 13:38
 */
public class AddressBookComparator implements Comparator<AddressBook> {

    @Override public int compare(AddressBook lhs, AddressBook rhs) {
        if (lhs == null || rhs == null) return 0;
        if (TextUtils.isEmpty(lhs.pinyin) || TextUtils.isEmpty(rhs.pinyin)) return 0;

        String p1 = chatAt0(lhs.pinyin);
        String p2 = chatAt0(rhs.pinyin);
        int pResult = p1.toLowerCase().compareTo(p2.toLowerCase());
        if (pResult != 0) {
            return pResult;
        }

        String n1 = chatAt0(lhs.name);
        String n2 = chatAt0(rhs.name);
        int nResult = n1.compareTo(n2);
        if (nResult != 0) {
            return nResult;
        }

        String d1 = lhs.deptGrade;
        String d2 = rhs.deptGrade;
        if (TextUtils.isEmpty(d1) || TextUtils.isEmpty(d2)) {
            return nResult;
        }
        int compareResult = d1.compareTo(d2);
        int dResult = compareResult == 0 ? 0 : compareResult > 0 ? -1 : 1;
        if (dResult != 0) {
            return dResult;
        }

        String s1 = lhs.sortNo;
        String s2 = rhs.sortNo;
        if (TextUtils.isEmpty(s1) || TextUtils.isEmpty(s2)) {
            return dResult;
        }
        return s1.compareTo(s2);
    }

    private String chatAt0(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }

        return value.charAt(0) + "";
    }
}
