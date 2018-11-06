package com.xxl.job.core.enums;

/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2018年09月20日 8:48
 */
public enum ResourceTypeEnum {
    JAR(".jar"),
    SQL(".sql"),
    SH(".sh"),
    PY(".py"),
    TXT(".txt");

    private final String suffix;
    private ResourceTypeEnum(String suffix){
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public static ResourceTypeEnum match(String name, ResourceTypeEnum defaultItem) {
        if (name != null){
            for (ResourceTypeEnum item:ResourceTypeEnum.values()){
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
