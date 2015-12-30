package com.cqyw.smart.main.model;

/**
 * Created by Kairong on 2015/11/18.
 * mail:wangkrhust@gmail.com
 */
public enum RefreshEnum {
    NEWEAST(0),     // 从最新的开始刷
    NEW_10(1),      // 刷最新10条
    OLD_10(2),      // 刷旧10条
    NONE(3);        // 不刷新

    private int value;
    RefreshEnum(int value) {
        this.value = value;
    }

    public boolean newerThan(RefreshEnum var1) {
        if (value < var1.value) {
            return true;
        }

        return false;
    }
}
