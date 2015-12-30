package com.netease.nim.uikit.joycustom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.netease.nim.uikit.R;


/**
 * 主界面照片选择弹框
 * Created by Kairong on 2015/11/29.
 * mail:wangkrhust@gmail.com
 */
public class JoyPictureOptionDialog extends Dialog {
    LinearLayout camera_ll, gallery_ll;
    View.OnClickListener camOptListener, galOptListener;
    public JoyPictureOptionDialog(Context context){
        super(context);
    }

    public JoyPictureOptionDialog(Context context, int theme){
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joy_layout_picture_optiondlg);
        init();
    }

    private void init() {
        camera_ll = (LinearLayout) findViewById(R.id.camera_btn_ll);
        gallery_ll = (LinearLayout) findViewById(R.id.gallery_btn_ll);
        camera_ll.setOnClickListener(camOptListener);
        gallery_ll.setOnClickListener(galOptListener);
    }

    public void setCamOptListener(final View.OnClickListener camOptListener) {
        this.camOptListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camOptListener.onClick(v);
                dismiss();
            }
        };
    }

    public void setGalOptListener(final View.OnClickListener galOptListener) {
        this.galOptListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galOptListener.onClick(v);
                dismiss();
            }
        };
    }
}
