package com.cqyw.smart.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.helper.UserUpdateHelper;
import com.cqyw.smart.main.adapter.ProvinceListAdapter;
import com.cqyw.smart.main.adapter.UniversityListAdapter;
import com.cqyw.smart.main.model.ProvinceInfo;
import com.cqyw.smart.main.model.SchoolInfo;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Kairong on 2015/11/12.
 * mail:wangkrhust@gmail.com
 */
public class SelectEduInfoActivity extends JActionBarActivity {
    public static final String TAG = SelectEduInfoActivity.class.getSimpleName();
    private LinearLayout school_layout;
    private LinearLayout province_layout;
    private LinearLayout univeristy_layout;
    private LinearLayout grade_layout;
    private LinearLayout entryyear_layout;
    private LinearLayout confirm_layout;

    private LinearLayout[] layoutContent;
    private RelativeLayout actionBar;
    private TextView actionbarTitle;
    private LinearLayout next_step_ll;
    private LinearLayout university_detail_list_ll;

    // data
    private boolean hasProvInfoInited = false;

    private String province = null;

    private String univeristy = null;

    private String grade = null;

    private String entryyear = null;

    private int currentPage = 0;

    private int type_school = -1;// 0-本科生 1-研究生 2-博士生 3-中学生 4-小学生

    // constant
    private String[] ENTRYYEAR;
    private String[] TITLE_HINT = {"选择修行", "选择学校所在省份", "选择学校", "选择大学学历", "选择入学年份", "核实信息"};
    private String[] GRADE = {"本科生", "研究生", "博士生", "中学生", "小学生"};

    // view data
    private ProvinceListAdapter provInfoAdapter;
    private UniversityListAdapter univInfoAdapter;

    private List<ProvinceInfo> provinceInfos;
    private List<SchoolInfo> schoolInfos;

    public static void start(Context context, int requestCode){
        Intent intent = new Intent(context, SelectEduInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        province_layout.setVisibility(View.VISIBLE);
        actionbarTitle.setText(TITLE_HINT[currentPage]);
    }

    private void generateEntryYear(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int startYear = (month >= 7 ? year : year-1);
        ENTRYYEAR = new String[6];
        for(int i = 0;i < 5;i++)
        {
            ENTRYYEAR[i] = startYear + "年";
            startYear--;
        }
        ENTRYYEAR[5] = "其他";
    }

    @Override
    protected void initStyle() {
        super.initStyle();
        setContentView(R.layout.activity_select_eduinfo);

        initActionBar();
        enableActionbarNextStep(false);
    }

    private void initActionBar(){
        actionBar = (RelativeLayout)findView(R.id.select_eduinfo_actionbar);
        ImageView back = (ImageView)actionBar.findViewById(R.id.centerBack);
        actionbarTitle = (TextView) actionBar.findViewById(R.id.leftTitle);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == 0) {
                    SelectEduInfoActivity.this.finish();
                    return;
                }
                switchContent(false);
            }
        });

        next_step_ll = (LinearLayout)actionBar.findViewById(R.id.rightTitle);
        TextView nextText = new TextView(this);
        nextText.setTextColor(Color.WHITE);
        nextText.setTextSize(18);
        nextText.setText("下一步");
        next_step_ll.addView(nextText);

        next_step_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((type_school < 3 && TextUtils.isEmpty(univeristy)) || TextUtils.isEmpty(grade) || TextUtils.isEmpty(entryyear)) {
                    Utils.showLongToast(SelectEduInfoActivity.this, "请完善所有信息");
                    return;
                }

                EasyAlertDialogHelper.createOkCancelDiolag(SelectEduInfoActivity.this,
                        "重要提示", "学籍信息确认后无法修改,请谨慎填写!", "确认", "修改", false,
                        new EasyAlertDialogHelper.OnDialogActionListener() {
                            @Override
                            public void doCancelAction() {
                            }

                            @Override
                            public void doOkAction() {
                                if (!NetworkUtil.isNetAvailable(SelectEduInfoActivity.this)) {
                                    Utils.showLongToast(SelectEduInfoActivity.this, getResources().getString(R.string.network_is_not_available));
                                    return;
                                }
                                UserUpdateHelper.updateEduInfo(univeristy, entryyear, grade, new RequestCallbackWrapper<Void>() {
                                    @Override
                                    public void onResult(int code, Void aVoid, Throwable throwable) {
                                        if (code == ResponseCode.RES_SUCCESS) {
                                            Utils.showLongToast(SelectEduInfoActivity.this, "信息保存成功");
                                            AppSharedPreference.setEduinfoEdited(true);
                                            AppCache.setStatus(2);
                                            setResult(RESULT_OK);
                                            SelectEduInfoActivity.this.finish();
                                        } else {
                                            LogUtil.d("SelectEduInfoActivity", "学籍信息保存失败:" + code);
                                        }
                                    }
                                });
                            }
                        }).show();
            }
        });

    }


    private void enableActionbarNextStep(boolean enable){
        next_step_ll.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initView() {
        school_layout = findView(R.id.select_school_type_layout);
        province_layout = findView(R.id.select_province_layout);
        univeristy_layout = findView(R.id.select_university_layout);
        grade_layout = findView(R.id.select_grade_layout);
        entryyear_layout = findView(R.id.select_entryyear_layout);
        confirm_layout = findView(R.id.confirm_info_layout);

        layoutContent = new LinearLayout[6];
        layoutContent[0] = school_layout;
        layoutContent[1] = province_layout;
        layoutContent[2] = univeristy_layout;
        layoutContent[3] = grade_layout;
        layoutContent[4] = entryyear_layout;
        layoutContent[5] = confirm_layout;

        initSchoolLayout();
        initProvinceLayout();
        initUniversityLayout();
        initGradeLayout();
        initEntryyearLayout();
    }

    private ICommProtocol.CommCallback<List<ProvinceInfo>> provInfoCallback = new ICommProtocol.CommCallback<List<ProvinceInfo>>() {
        @Override
        public void onSuccess(List<ProvinceInfo> provInfos) {
            DialogMaker.dismissProgressDialog();
            provinceInfos.clear();
            provinceInfos.addAll(provInfos);
            provInfoAdapter.notifyDataSetChanged();
            hasProvInfoInited = true;
        }

        @Override
        public void onFailed(String code, String errorMsg) {
            DialogMaker.dismissProgressDialog();
            Utils.showLongToast(SelectEduInfoActivity.this, "加载信息失败:"+errorMsg);
        }
    };

    private ICommProtocol.CommCallback<List<SchoolInfo>> schoolInfoCallback = new ICommProtocol.CommCallback<List<SchoolInfo>>() {
        @Override
        public void onSuccess(List<SchoolInfo> schlInfos) {
            DialogMaker.dismissProgressDialog();
            schoolInfos.clear();
            schoolInfos.addAll(schlInfos);
            univInfoAdapter.notifyDataSetChanged();
            switchContent(true);
        }

        @Override
        public void onFailed(String code, String errorMsg) {
            DialogMaker.dismissProgressDialog();
            Utils.showLongToast(SelectEduInfoActivity.this, "加载信息失败:"+errorMsg);
        }
    };

    private void initSchoolLayout() {
        TextView type_university_tv = (TextView) school_layout.findViewById(R.id.type_university_tv);
        TextView type_middle_school_tv = (TextView) school_layout.findViewById(R.id.type_middle_school_tv);
        TextView type_primary_school_tv = (TextView) school_layout.findViewById(R.id.type_primary_school_tv);

        // 初始化监听
        View.OnClickListener onTypeClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                switch (v.getId())
                {
                    case R.id.type_university_tv:
                        type_school = 0;
                        // 获取省份信息
                        if (!hasProvInfoInited)
                        {
                            DialogMaker.showProgressDialog(SelectEduInfoActivity.this, "");
                            JoyCommClient.getInstance().getProvinceInfo(AppCache.getJoyId(), AppCache.getJoyToken(), provInfoCallback);
                        }
                        switchContent(true);
                        break;
                    case R.id.type_middle_school_tv:
                        type_school = 3;
                        grade = GRADE[3];
                        univeristy = "    ";
                        switchContent(true);
                        break;
                    case R.id.type_primary_school_tv:
                        type_school = 4;
                        grade = GRADE[4];
                        univeristy = "    ";
                        switchContent(true);
                        break;
                }
            }
        };
        type_middle_school_tv.setOnClickListener(onTypeClick);
        type_primary_school_tv.setOnClickListener(onTypeClick);
        type_university_tv.setOnClickListener(onTypeClick);
    }

    private void initProvinceLayout()
    {
        final ListView listView = (ListView)province_layout.findViewById(R.id.province_list);
        provinceInfos = new ArrayList<>();
        provInfoAdapter = new ProvinceListAdapter(this, provinceInfos);
        listView.setAdapter(provInfoAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                province = provinceInfos.get(position).getProvince_name();
                // 加载学校信息
                DialogMaker.showProgressDialog(SelectEduInfoActivity.this, "");
                JoyCommClient.getInstance().getSchoolInfo(AppCache.getJoyId(), AppCache.getJoyToken(), provinceInfos.get(position).getProvince_id(),schoolInfoCallback);
            }
        });

        province_layout.setVisibility(View.GONE);
    }

    private void initEntryyearLayout()
    {
        generateEntryYear();
        final TextView[] entry_iv = new TextView[6];
        entry_iv[5] = (TextView)entryyear_layout.findViewById(R.id.entry_other);
        entry_iv[4] = (TextView)entryyear_layout.findViewById(R.id.entry5);
        entry_iv[3] = (TextView)entryyear_layout.findViewById(R.id.entry4);
        entry_iv[2] = (TextView)entryyear_layout.findViewById(R.id.entry3);
        entry_iv[1] = (TextView)entryyear_layout.findViewById(R.id.entry2);
        entry_iv[0] = (TextView)entryyear_layout.findViewById(R.id.entry1);

        for(int i = 0;i < 6;i++)
        {
            entry_iv[i].setText(ENTRYYEAR[i]);
        }

        // 初始化监听
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 6; i++ ){
                    if (entry_iv[i].getId() == v.getId()){
                        if(i < 5)
                        entryyear = ENTRYYEAR[i].substring(0, 4)+"级";
                        else
                        entryyear = ENTRYYEAR[i];
                        switchContent(true);
                        break;
                    }
                }
            }
        };

        // 设置监听
        for (TextView v:entry_iv) {
            v.setOnClickListener(onClickListener);
        }
        entryyear_layout.setVisibility(View.GONE);
    }

    private void initGradeLayout(){
        final TextView[] grade_tv = new TextView[3];
        grade_tv[0] = (TextView)grade_layout.findViewById(R.id.undergraduate);
        grade_tv[1] = (TextView)grade_layout.findViewById(R.id.graduate);
        grade_tv[2] = (TextView)grade_layout.findViewById(R.id.phd);
        View.OnClickListener onClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 3; i++ ){
                    if (grade_tv[i].getId() == v.getId()){
                        grade = GRADE[i];
                        type_school = i;
                        switchContent(true);
                        break;
                    }
                }
            }
        };
        // 设置监听
        for (TextView v:grade_tv)
        {
            v.setOnClickListener(onClickListener);
        }
        grade_layout.setVisibility(View.GONE);
    }

    private void initUniversityLayout()
    {
        final ListView listView = (ListView)univeristy_layout.findViewById(R.id.university_list);
        schoolInfos = new ArrayList<>();
        univInfoAdapter = new UniversityListAdapter(this, schoolInfos);
        listView.setAdapter(univInfoAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                univeristy = schoolInfos.get(position).getSchool_name();
                switchContent(true);
            }
        });

        univeristy_layout.setVisibility(View.GONE);
    }

    private void initConfirmLayout(){
        TextView _university = (TextView)confirm_layout.findViewById(R.id.info_university);
        TextView _grade = (TextView)confirm_layout.findViewById(R.id.info_grade);
        TextView _entryyear = (TextView)confirm_layout.findViewById(R.id.info_entryyear);

        if(type_school >= 3){
            _university.setVisibility(View.GONE);
        } else {
            _university.setVisibility(View.VISIBLE);
            _university.setText(univeristy);
        }
        _grade.setText(grade);
        _entryyear.setText(entryyear);
        confirm_layout.setVisibility(View.GONE);
    }

    private void switchContent(boolean forward){
        if (forward){
            if (currentPage >= 0 && currentPage < 5) {
                layoutContent[currentPage].setVisibility(View.GONE);
                if (currentPage == 0 && type_school >= 3) {
                    while(++currentPage < 4) layoutContent[currentPage].setVisibility(View.GONE);
                } else {
                    currentPage++;
                }
                if (currentPage == 5){
                    enableActionbarNextStep(true);
                    initConfirmLayout();
                }
                layoutContent[currentPage].setVisibility(View.VISIBLE);
                actionbarTitle.setText(TITLE_HINT[currentPage]);
            }
        } else {
            if (currentPage <= 5 && currentPage > 0) {
                layoutContent[currentPage].setVisibility(View.GONE);
                if (currentPage == 5) {
                    enableActionbarNextStep(false);
                }

                if (currentPage == 4 && type_school >= 3){
                    currentPage -= 4;
                } else {
                    currentPage--;
                }

                layoutContent[currentPage].setVisibility(View.VISIBLE);
                actionbarTitle.setText(TITLE_HINT[currentPage]);
            }
        }
        LogUtil.d(TAG, "currentPage="+currentPage);
    }

}
