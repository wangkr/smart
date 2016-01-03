package com.cqyw.smart.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.widget.Toast;


import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kairong on 2015/9/30.
 * mail:wangkrhust@gmail.com
 */
public class Utils {

    public static void showLongToast(Context context, String pMsg) {
        if ( null != context) {
            Toast.makeText(context, pMsg, Toast.LENGTH_LONG).show();
        }
    }

    public static void showShortToast(Context context, String pMsg) {
        if ( null != context) {
            Toast.makeText(context, pMsg, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 验证手机号
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(17[^4,\\D])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static ColorStateList getColorStateList(Context context, int resId){
        XmlResourceParser xrp = context.getResources().getXml(resId);
        ColorStateList csl = null;
        try{
            csl = ColorStateList.createFromXml(context.getResources(), xrp);
        } catch (XmlPullParserException|IOException e){
            e.printStackTrace();
            return null;
        }
        return csl;
    }

    /**
     * List 深度拷贝：执行序列化和反序列化
     * @param src
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List deepCopy(List src) throws IOException, ClassNotFoundException{
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in =new ObjectInputStream(byteIn);
        List dest = (List)in.readObject();
        return dest;
    }

}
