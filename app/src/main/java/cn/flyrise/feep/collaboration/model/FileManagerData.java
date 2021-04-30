/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-29 下午8:03:04
 */
package cn.flyrise.feep.collaboration.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-6-29</br> 修改备注：</br>
 */
public class FileManagerData{

    private HashMap<String, ArrayList<FileInfo>> readFiles;
    private ArrayList<FileInfo> checkedFiles;

    public FileManagerData() {
        readFiles = new HashMap<>();
        checkedFiles = new ArrayList<>();
    }

    public HashMap<String, ArrayList<FileInfo>> getReadFiles() {
        return readFiles;
    }

    public void setReadFiles(HashMap<String, ArrayList<FileInfo>> readFiles) {
        this.readFiles = readFiles;
    }

    public ArrayList<FileInfo> getCheckedFiles() {
        return checkedFiles;
    }

    public void setCheckedFiles(ArrayList<FileInfo> checkedFiles) {
        this.checkedFiles = checkedFiles;
    }

    public void addCheckedFiles(FileInfo checkedFile) {
        if (checkedFiles != null) {
            this.checkedFiles.add(checkedFile);
        }
    }

    /**
     * 清除数据
     */
    public void clearData() {
        if (readFiles != null) {
            readFiles.clear();
        }
        if (checkedFiles != null) {
            checkedFiles.clear();
        }
    }
}
