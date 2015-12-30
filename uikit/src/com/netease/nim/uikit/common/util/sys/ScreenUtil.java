package com.netease.nim.uikit.common.util.sys;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;

import java.lang.reflect.Field;

public class ScreenUtil {
	private static final String TAG = "Joy.ScreenUtil";
	
	private static double RATIO = 0.85;
	
	public static int screenWidth;
	public static int screenHeight;
	public static int screenMin;// 宽高中，小的一边
	public static int screenMax;// 宽高中，较大的值

	public static JoyImageUtil.ImageType coverType;

	public static float density;
	public static float scaleDensity;
	public static float xdpi;
	public static float ydpi;
	public static int densityDpi;
	
	public static int dialogWidth;
	public static int statusbarheight;
	public static int navbarheight;

    static {
        init(NimUIKit.getContext());
    }
	
	public static int dip2px(float dipValue) {
		return (int) (dipValue * density + 0.5f);
	}

	public static int px2dip(float pxValue) {
		return (int) (pxValue / density + 0.5f);
	}

	public static int getDialogWidth() {
		dialogWidth = (int) (screenMin * RATIO);
		return dialogWidth;
	}

    public static void init(Context context) {
        if (null == context) {
            return;
        }
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        screenMin = (screenWidth > screenHeight) ? screenHeight : screenWidth;
        density = dm.density;
        scaleDensity = dm.scaledDensity;
        xdpi = dm.xdpi;
        ydpi = dm.ydpi;
        densityDpi = dm.densityDpi;

		initCoverWidth();
        Log.d(TAG, "screenWidth=" + screenWidth + " screenHeight=" + screenHeight + " density=" + density);
    }

	private static void initCoverWidth() {
		if (screenWidth <= 500) {
			coverType = JoyImageUtil.ImageType.V_480;
		} else if (screenWidth <= 600) {
			coverType = JoyImageUtil.ImageType.V_540;
		} else if (screenWidth <= 900) {
			coverType = JoyImageUtil.ImageType.V_720;
		} else {
			coverType = JoyImageUtil.ImageType.V_1080;
		}
	}

	public static int getDisplayHeight() {
		if(screenHeight == 0){
			GetInfo(NimUIKit.getContext());
		}
		return screenHeight;
	}

	public static void GetInfo(Context context) {
		if (null == context) {
			return;
		}
		DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		screenMin = (screenWidth > screenHeight) ? screenHeight : screenWidth;
		screenMax = (screenWidth < screenHeight) ? screenHeight : screenWidth;
		density = dm.density;
		scaleDensity = dm.scaledDensity;
		xdpi = dm.xdpi;
		ydpi = dm.ydpi;
		densityDpi = dm.densityDpi;
		statusbarheight = getStatusBarHeight(context);
		navbarheight = getNavBarHeight(context);
		Log.d(TAG, "screenWidth=" + screenWidth + " screenHeight=" + screenHeight + " density=" + density);
	}

	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = context.getResources().getDimensionPixelSize(x);
		} catch (Exception E) {
			E.printStackTrace();
		}
		return sbar;
	}

	public static int getNavBarHeight(Context context){
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}
}
