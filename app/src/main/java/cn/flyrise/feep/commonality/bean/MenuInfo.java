//
// LoadingImage.java
// feep
//
// Created by ZhongYJ on 2012-3-6.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.feep.commonality.bean;

import android.support.annotation.Keep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Keep
public class MenuInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String nums;
    private String name;
    private String imageHref;
    private int imageRes;
    private int chooseOnImageRes;
    private Class<?> intentClass;
    private int listType;
    public String mainMenuType;

    /**
     * 子目录一
     */
    private List<MenuInfo> children;

    private String icon;
    private String url;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNums() {
        return nums;
    }

    public void setNums(String nums) {
        this.nums = nums;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageHref() {
        return imageHref;
    }

    public void setImageHref(String imageHref) {
        this.imageHref = imageHref;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public int getChooseOnImageRes() {
        return chooseOnImageRes;
    }

    public void setChooseOnImageRes(int chooseOnImageRes) {
        this.chooseOnImageRes = chooseOnImageRes;
    }

    public Class<?> getIntentClass() {
        return intentClass;
    }

    public void setIntentClass(Class<?> intentClass) {
        this.intentClass = intentClass;
    }

    public int getListType() {
        return listType;
    }

    public void setListType(int listType) {
        this.listType = listType;
    }

    public List<MenuInfo> getChildren() {
        return children;
    }

    public void setChildren(List<MenuInfo> children) {
        this.children = children;
    }

    public void addChild(MenuInfo menuInfo) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(menuInfo);
    }

    @Override
    public String toString() {
        return "MenuInfo [id=" + id + ", nums=" + nums + ", name=" + name + ", listType=" + listType + "]";
    }

}
