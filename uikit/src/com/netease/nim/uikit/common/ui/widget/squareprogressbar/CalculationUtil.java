package com.netease.nim.uikit.common.ui.widget.squareprogressbar;

/**
 * Created by Kairong on 2015/12/13.
 * mail:wangkrhust@gmail.com
 */
import android.content.Context;
import android.util.TypedValue;

public class CalculationUtil {

    public static int convertDpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
