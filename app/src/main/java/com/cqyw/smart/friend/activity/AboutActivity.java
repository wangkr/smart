package com.cqyw.smart.friend.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;

public class AboutActivity extends JActionBarActivity {
	
	private TextView version;
	private TextView appName;
	private TextView introduce;

	public static void start(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, AboutActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initStyle() {
		super.initStyle();
		setContentView(R.layout.acitivity_about);
		setTitle(R.string.about);
	}

	@Override
	protected void initData() {
	}

	@Override
	protected void initView() {
		findViews();
	}

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
		version = findView(R.id.about_app_version);
		appName = findView(R.id.about_app_name);
		introduce = findView(R.id.about_app_introduce);
	}

}
