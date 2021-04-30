/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-25 下午2:18:03
 */

package cn.flyrise.feep.collaboration.model;


import android.support.annotation.Keep;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-6-25</br> 修改备注：</br>
 */
@Keep
public class FileInfo implements Comparable<FileInfo>, Cloneable {

    public static final int NO_CHECK_STATE_TYPE = 0;

    public static final int ALL_CHECK_STATE_TYPE = 1;

    public static final int PART_CHECK_STATE_TYPE = 2;

    /***************************************
     * 邮箱附件相关
     *****************************/
    public static final int MAIL_CHECK_STATE_TYPE = 3;

    /***************************************
     * 邮箱附件相关
     *****************************/

    private File file;

    private String parentFilePath;

    private FileInfo parentFile;

    private int stateType = 0;

    private String fileName;

    private ArrayList<FileInfo> childFiles;

    private AttachmentBean detailAttachment;
    private String type;


    /**
     * 判断是否是本地文件
     */
    private boolean isLocalFile = true;

    //存放录音时间
    private String recordItemTime;

    public String getRecordItemTime() {
        return recordItemTime;
    }

    public void setRecordItemTime(String recordItemTime) {
        this.recordItemTime = recordItemTime;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        if (file != null && fileName == null) {
            return file.getName();
        }
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileInfo getParentFile() {
        return parentFile;
    }

    public void setParentFile(FileInfo parentFile) {
        this.parentFile = parentFile;
    }

    public void setChildFiles(ArrayList<FileInfo> childFiles) {
        this.childFiles = childFiles;
    }

    public String getSize() {
        if (file != null) {
            if (file.isFile()) {
                return FileUtil.getFileSize(file.length());
            } else {
                return (file.listFiles() == null ? 0 : file.listFiles().length) + CommonUtil.getString(R.string.util_item);
            }
        }
        return null;
    }

    public String getSize(List<String> filters) {
        if (file != null) {
            if (file.isFile()) {
                return FileUtil.getFileSize(file.length());
            }
            List<File> files = filterFile(file.listFiles(), filters);
            return (CommonUtil.isEmptyList(files) ? 0 : files.size()) + CommonUtil.getString(R.string.util_item);
        }
        return null;
    }

    public static List<File> filterFile(File[] files, List<String> filters) {
        if (files == null) {
            return null;
        }
        List<File> filterFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                filterFiles.add(file);
            }
            if (file.isFile()) {
                String fileName = file.getName();
                FELog.e("File : " + fileName);
                int suffixIndex = fileName.lastIndexOf(".");
                if (suffixIndex == -1) {
                    continue;                // 排除傻逼文件。
                }
                if (file.length() == 0) {
                    continue;                //排除文件大小为0的文件。
                }
                if (fileName.lastIndexOf(".") == 0) {
                    continue;    //可能存在更加傻逼的文件，没有文件名，有后缀。。。
                }
                String suffix = fileName.substring(suffixIndex);
                if (filters.contains(suffix)) {
                    filterFiles.add(file);
                }
            }
        }
        return filterFiles;
    }

    public String getType() {
        if (file != null) {
            if (file.isDirectory()) {
                type = CommonUtil.getString(R.string.util_folder);
            } else {
                final String name = getFileName().toLowerCase();
                if (name.contains(".")) {
                    type = name.substring(name.lastIndexOf("."), name.length());
                }
            }
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        if (file != null) {
            return file.getPath();
        }
        return null;
    }

    public int getStateType() {
        return stateType;
    }

    public void setStateType(int stateType) {
        this.stateType = stateType;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public ArrayList<FileInfo> getChildFiles() {
        return childFiles;
    }

    public AttachmentBean getDetailAttachment() {
        return detailAttachment;
    }

    public void setDetailAttachment(AttachmentBean detailAttachment) {
        this.detailAttachment = detailAttachment;
    }

    public boolean isLocalFile() {
        return isLocalFile;
    }

    public void setLocalFile() {
        this.isLocalFile = false;
    }

    private boolean isTransmitFile;

    public void setTransmitFile(boolean isTransmitFile) {
        this.isTransmitFile = isTransmitFile;
    }

    public boolean isTransmitFile() {
        return this.isTransmitFile;
    }

    public String getParentFilePath() {
        return parentFilePath;
    }

    public void setParentFilePath(String parentFilePath) {
        this.parentFilePath = parentFilePath;
    }

    /**
     * 排序（文件夹在先并且按首字母排序）
     **/
    @Override
    public int compareTo(FileInfo another) {
        if (this.isFile() && another.isDirectory()) {
            return 1;
        } else if (this.isDirectory() && another.isFile()) {
            return -1;
        }
        return this.getFileName().compareToIgnoreCase(another.getFileName());
    }

    @Override
    public FileInfo clone() {
        FileInfo clone = null;
        try {
            clone = (FileInfo) super.clone();

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);  // won't happen
        }

        return clone;
    }

}
