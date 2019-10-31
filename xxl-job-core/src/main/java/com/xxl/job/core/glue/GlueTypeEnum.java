package com.xxl.job.core.glue;

/**
 * Created by xuxueli on 17/4/26.
 */
public enum GlueTypeEnum {

    BEAN("BEAN模式", false, null, null),
    GLUE_GROOVY("GLUE模式(Java)", false, null, null),
    SHELL("SHELL", true, "bash", ".sh"),
    PYTHON("PYTHON", true, "python", ".py"),
    GLUE_NODEJS("GLUE模式(Nodejs)", true, "node", ".js"),
    HIVE("HIVE", true, "hive",".sql"),
    PRESTO("PRESTO", false, null, null);

    private String desc;
    private boolean isScript;
    private String cmd;
    private String suffix;

    private GlueTypeEnum(String desc, boolean isScript, String cmd, String suffix) {
        this.desc = desc;
        this.isScript = isScript;
        this.cmd = cmd;
        this.suffix = suffix;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isScript() {
        return isScript;
    }

    public String getCmd() {
        return cmd;
    }

    public String getSuffix() {
        return suffix;
    }

    public static GlueTypeEnum match(String name){
        // .values()可以将枚举类转变为一个枚举类型的数组
        for (GlueTypeEnum item: GlueTypeEnum.values()) {
            if (item.name().equals(name)) { // .name()得到的是"GLUE_SHELL"这一个值
                return item;
            }
        }
        return null;
    }

}
