package cn.flyrise.feep.commonality.bean;

import android.app.Activity;

/**
 * @author ZYP
 * @since 2016-11-10 14:45
 */
public class CacheModule {
    public final int moduleId;
    public final int moduleIcon;
    public final String moduleName;
    public final String moduleIconUrl;
    public final int moduleItemType;
    public Class<? extends Activity> moduleClass;

    private CacheModule(Builder builder) {
        moduleId = builder.moduleId;
        moduleIcon = builder.moduleIcon;
        moduleName = builder.moduleName;
        moduleIconUrl = builder.moduleIconUrl;
        moduleItemType = builder.moduleItemType;
        moduleClass = builder.moduleClass;
    }

    public static class Builder {
        private int moduleId;
        private int moduleIcon;
        private String moduleName;
        private String moduleIconUrl;
        private int moduleItemType;
        private Class<? extends Activity> moduleClass;

        public Builder moduleId(int moduleId) {
            this.moduleId = moduleId;
            return this;
        }

        public Builder moduleIcon(int moduleIcon) {
            this.moduleIcon = moduleIcon;
            return this;
        }

        public Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public Builder moduleIconUrl(String moduleIconUrl) {
            this.moduleIconUrl = moduleIconUrl;
            return this;
        }

        public Builder moduleItemType(int moduleItemType) {
            this.moduleItemType = moduleItemType;
            return this;
        }

        public Builder moduleClass(Class<? extends Activity> moduleClass) {
            this.moduleClass = moduleClass;
            return this;
        }

        public CacheModule create() {
            return new CacheModule(this);
        }
    }
}
