package com.xxl.job.core.enums;


/**
 * @Description:
 * @author: mashencai@supcon.com
 * @date: 2018年09月12日 14:24
 */
public enum ScheduleCycleEnum {

    DAY("每日", new int[]{0}),
    WEEK("每周", new int[]{1, 2, 3, 4, 5, 6, 7}),
    MONTH("每月", new int[]{1, 2, 3, 4, 5, 6 ,7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28} );


    private String dateType;
    private int[] dayNum;

    ScheduleCycleEnum(String dateType, int[] dayNum){
        this.dateType = dateType;
        this.dayNum = dayNum;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public int[] getDayNum() {
        return dayNum;
    }

    public void setDayNum(int[] dayNum) {
        this.dayNum = dayNum;
    }

    public static ScheduleCycleEnum typeMatch(String name, ScheduleCycleEnum defaultItem) {
        if (name != null) {
            for (ScheduleCycleEnum item:ScheduleCycleEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }

    public static int itemMatch(int num, ScheduleCycleEnum scheduleCycleEnum) {
        if (num > 0) {
            for (int item : scheduleCycleEnum.getDayNum()) {
                if (item == num) {
                    return item;
                }
            }
        }
        return 0;
    }

}
