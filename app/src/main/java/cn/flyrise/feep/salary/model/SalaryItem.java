package cn.flyrise.feep.salary.model;

/**
 * @author ZYP
 * @since 2017-02-22 10:10
 */
public class SalaryItem {

    public static final byte TYPE_ADD = 1;      // 加的工资，
    public static final byte TYPE_SUB = 2;      // 减的工资
    public static final byte TYPE_OTHER = 3;    // 备注

    public byte type;
    public String key;
    public String value;

}
