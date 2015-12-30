package com.cqyw.smart.widget.popwindow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.cqyw.smart.R;

/**
 * Created by Kairong on 2015/12/29.
 * mail:wangkrhust@gmail.com
 */
public class HintDialog extends Dialog {
    public HintDialog(Context context) {
        super(context);
    }

    public HintDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_hint);
        Button button = (Button)findViewById(R.id.i_konw_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
