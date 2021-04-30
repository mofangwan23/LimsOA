package cn.flyrise.feep.main.adapter;

import android.text.TextUtils;

/**
 * @author ZYP
 * @since 2017-02-13 11:23
 */
public class MainContactModel {

    public int type;
    public String tag;
    public int iconRes;
    public String iconUrl;
    public String name;
    public String subName;
    public String userId;
    public boolean hasArrow;
    public boolean hasDivider;
    public String deptId;
    public int departmentSize;
    public boolean hasLongSpliteLine;

    public static class Builder {
        private int type;
        private String tag;
        private String iconUrl;
        private int iconRes = -1;
        private String name;
        private String subName;
        private String userId;
        private boolean hasArrow;
        private boolean hasDivider;
        private String deptId;
        private int departmentSize;
        private boolean hasLongSpliteLine;

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder setIconRes(int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSubName(String subName) {
            this.subName = subName;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setArrowVisibility(boolean arrowVisibility) {
            this.hasArrow = arrowVisibility;
            return this;
        }

        public Builder setDividerVisiblity(boolean dividerVisiblity) {
            this.hasDivider = dividerVisiblity;
            return this;
        }

        public Builder setDepartmentId(String deptId) {
            this.deptId = deptId;
            return this;
        }

        public Builder setDepartmentsize(int departmentSize){
            this.departmentSize = departmentSize;
            return this;
        }

        public Builder setLongSpliteLineVisiblity(boolean hasLongSpliteLine){
            this.hasLongSpliteLine = hasLongSpliteLine;
            return this;
        }


        public MainContactModel build() {
            MainContactModel model = new MainContactModel();
            if (!TextUtils.isEmpty(tag)) {
                model.tag = tag;
                return model;
            }

            if (!TextUtils.isEmpty(iconUrl)) {
                model.iconUrl = iconUrl;
            }
            else if (iconRes != -1) {
                model.iconRes = iconRes;
            }
            model.type = type;
            model.hasArrow = hasArrow;
            model.hasDivider =  hasDivider;
            model.userId = userId;
            model.name = name;
            model.subName = subName;
            model.deptId = deptId;
            model.departmentSize = departmentSize;
            model.hasLongSpliteLine = hasLongSpliteLine;
            return model;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MainContactModel model = (MainContactModel) o;

        return type == model.type;
    }

    @Override
    public int hashCode() {
        return type;
    }
}
