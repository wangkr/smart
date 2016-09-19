package com.cqyw.smart.widget.popwindow;

import android.content.Context;

import com.cqyw.smart.main.model.PublicSnapMessage;

/**
 * Created by Kairong on 2016/9/18.
 * mail:wangkrhust@gmail.com
 */
public class ImageShower {
    public static ShowOriginImageDialog showOriginImageDialog;

    public static ShowOriginImageDialog showImage(Context context, PublicSnapMessage message) {
        if (showOriginImageDialog == null) {
            showOriginImageDialog = new ShowOriginImageDialog(context);
        } else if (showOriginImageDialog.getContext() != context) {
            dismissShowImageDialog();
            showOriginImageDialog = new ShowOriginImageDialog(context);
        }

        showOriginImageDialog.setMessage(message);
        showOriginImageDialog.show();

        return showOriginImageDialog;
    }


    public static void dismissShowImageDialog() {
        if (null == showOriginImageDialog) {
            return;
        }
        if (showOriginImageDialog.isShowing()) {
            try {
                showOriginImageDialog.dismiss();
                showOriginImageDialog = null;
            } catch (Exception e) {
                // maybe we catch IllegalArgumentException here.
            }

        }

    }
}
