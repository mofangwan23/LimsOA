package cn.flyrise.feep.addressbook.model;

/**
 * @author ZYP
 * @since 2017-02-09 09:00
 * 解压后的文件信息
 */
public class ExtractInfo {

    public static final byte TYPE_JSON = 1;
    public static final byte TYPE_DB = 2;
    public static final byte TYPE_SQL = 3;

    public String path;
    public String name;
    public byte type;

}
