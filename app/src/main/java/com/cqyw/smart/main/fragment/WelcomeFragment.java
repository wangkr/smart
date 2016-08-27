package com.cqyw.smart.main.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cqyw.smart.R;
import com.cqyw.smart.login.LoginActivity;

/**
 * Created by Kairong on 2015/11/10.
 * mail:wangkrhust@gmail.com
 */
public class WelcomeFragment extends Fragment {
//    public static int[] welcome_fragment_bgs = {R.drawable.welcome1, R.drawable.welcome2, R.drawable.welcome3, R.drawable.welcome4};

    private static final String KEY_CONTENT = "WelcomeFragment:index";

    private int index = 0;

    private LinearLayout linearLayout;

    public static WelcomeFragment newInstance(int index) {
        WelcomeFragment fragment = new WelcomeFragment();
        fragment.index = index;

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            index = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
//        view.findViewById(R.id.welcome_fragment_bg).setBackgroundResource(welcome_fragment_bgs[index % 4]);
//        if (index % 4 == 3) {
//            view.findViewById(R.id.start_joy_experience).setVisibility(View.VISIBLE);
//            view.findViewById(R.id.start_joy_experience).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    LoginActivity.start(getContext());
//                }
//            });
//        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, index);
    }
}
