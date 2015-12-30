package com.cqyw.smart.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.widget.touchgallery.GalleryWidget.GalleryViewPager;
import com.cqyw.smart.widget.touchgallery.GalleryWidget.UrlPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kairong on 2015/12/27.
 * mail:wangkrhust@gmail.com
 */
public class PictureViewerActivity extends JActionBarActivity implements GalleryViewPager.OnItemClickListener{
    private static final String EXTRA_URL_LIST = "urlList";
    private static final String EXTRA_OFFSET = "offset";
    private GalleryViewPager viewPager;
    private UrlPagerAdapter pagerAdapter;
    private List<String> urlList;
    private int offset = 0;
    public static void start(Context context, ArrayList<String> urls, int offset) {
        Intent intent = new Intent();
        intent.setClass(context, PictureViewerActivity.class);
        intent.putStringArrayListExtra(EXTRA_URL_LIST, urls);
        intent.putExtra(EXTRA_OFFSET, offset);
        context.startActivity(intent);
    }
    @Override
    protected void initStyle() {
        super.initStyle();
        setContentView(R.layout.activity_picture_viewer);
    }

    @Override
    protected void initData() {
        urlList = getIntent().getStringArrayListExtra(EXTRA_URL_LIST);
        offset = getIntent().getIntExtra(EXTRA_OFFSET, 0);
    }

    @Override
    protected void initView() {
        pagerAdapter = new UrlPagerAdapter(this, urlList);
        viewPager = findView(R.id.picture_viewer);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setOnItemClickListener(this);
        viewPager.setCurrentItem(offset);
    }

    @Override
    public void onItemClicked(View view, int position) {
        super.onBackPressed();
    }
}
