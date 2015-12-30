package com.cqyw.smart.main.model;

/**
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public enum SnapNewTypeEnum {
    WARNING(0),
    ANSWER(1),
    COMMENT(2),
    LIKE(3);
    private int value;
    SnapNewTypeEnum(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SnapNewTypeEnum getType(int value) {
        switch (value)
        {
            case 0:
                return WARNING;
            case 1:
                return ANSWER;
            case 2:
                return COMMENT;
            case 3:
                return LIKE;
            default:
                return COMMENT;
        }
    }
}
