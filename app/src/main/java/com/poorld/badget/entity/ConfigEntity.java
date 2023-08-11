package com.poorld.badget.entity;

import java.util.HashMap;
import java.util.Map;

public class ConfigEntity {

    private boolean enabled;

    private Map<String, PkgConfig> pkgConfigs;

    public ConfigEntity() {
        pkgConfigs = new HashMap<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, PkgConfig> getPkgConfigs() {
        return pkgConfigs;
    }

    public void addPkgConfigs(String pkgName, PkgConfig pkgConfig) {
        pkgConfigs.put(pkgName, pkgConfig);
    }

    public static class PkgConfig {
        private String pkgName;
        private String appName;
        private String jsPath;

        private String soName;
        private InteractionType type;
        private boolean enabled;

        public String getPkgName() {
            return pkgName;
        }

        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }


        public String getJsPath() {
            return jsPath;
        }

        public void setJsPath(String jsPath) {
            this.jsPath = jsPath;
        }

        public String getSoName() {
            return soName;
        }

        public void setSoName(String soName) {
            this.soName = soName;
        }

        public InteractionType getType() {
            return type;
        }

        public void setType(InteractionType type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return "PkgConfig{" +
                    "pkgName='" + pkgName + '\'' +
                    ", appName='" + appName + '\'' +
                    ", jsPath='" + jsPath + '\'' +
                    ", soName='" + soName + '\'' +
                    ", type=" + type +
                    ", enabled=" + enabled +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ConfigEntity{" +
                "enabled=" + enabled +
                ", pkgConfigs=" + pkgConfigs +
                '}';
    }
}
