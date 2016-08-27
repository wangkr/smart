package com.cqyw.smart.contact.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.localcontact.LocalContact;
import com.cqyw.smart.contact.localcontact.LocalContactAdapter;
import com.cqyw.smart.contact.localcontact.LocalContactService;
import com.cqyw.smart.contact.localcontact.LocalContactViewHolder;
import com.cqyw.smart.util.TrimTellNumber;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 本地通讯录
 * Created by Kairong on 2015/11/28.
 * mail:wangkrhust@gmail.com
 */
public class LocalContactFriendActivity extends JActionBarActivity implements TAdapterDelegate{
    private static final int REQUEST_ACCESS_CONTACTS = 100;
    // data
    private Map<String, String> infos;
    private LinkedList<LocalContact> items;
    private LocalContactAdapter adapter;
    private LocalContactService localContactService;
    // view
    private ListView listView;
    private SwitchButton settingSwitch;

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, LocalContactFriendActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppContext.isAndroid6() && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_ACCESS_CONTACTS);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "请允许读取通讯录", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (AppSharedPreference.getLocalcontactSwitch()) {
                DialogMaker.showProgressDialog(LocalContactFriendActivity.this, "正在匹配好友...");
                handler.postDelayed(runnable, 100);
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    refreshListView();
                    DialogMaker.dismissProgressDialog();
                    break;
                case 1:
                    DialogMaker.dismissProgressDialog();
                    break;
                default:
                    break;

            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    infos = getContactInfo();
                    // 去除已经匹配了的
                    for (LocalContact contact:items) {
                        infos.remove(contact.getPhone());
                    }
                    StringBuilder phonesString = new StringBuilder();
                    for (String key:infos.keySet()) {
                        phonesString.append(key).append(",");
                    }
                    if (phonesString.length() > 0) {
                        JoyCommClient.getInstance().localcontactMatch(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(),
                                phonesString.substring(0, phonesString.length() - 1), new ICommProtocol.CommCallback<List<LocalContact>>() {
                                    @Override
                                    public void onSuccess(List<LocalContact> localContactList) {
                                        for(LocalContact contact:localContactList) {
                                            contact.setContactname(infos.get(contact.getPhone()));
                                            items.addFirst(contact);
                                        }
                                        // 倒序插入存储以便再次读取时顺序不变
                                        Collections.reverse(localContactList);
                                        // 保存到本地数据库
                                        localContactService.saveLocalContactList(localContactList);
                                        handler.sendEmptyMessage(0);
                                    }

                                    @Override
                                    public void onFailed(String code, String errorMsg) {
                                        handler.sendEmptyMessage(1);
                                    }
                                });
                    }
                }
            }).start();
        }
    };

    private void refreshListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void initStyle() {
        super.initStyle();
        setTitle(R.string.phone_contact);
        setContentView(R.layout.activity_local_contact);
    }

    @Override
    protected void initData() {
        localContactService = new LocalContactService(this);
    }

    @Override
    protected void initView() {
        settingSwitch = (SwitchButton)findViewById(R.id.local_contact_switch);
        settingSwitch.setCheck(AppSharedPreference.getLocalcontactSwitch());
        settingSwitch.setOnChangedListener(new SwitchButton.OnChangedListener() {
            @Override
            public void OnChanged(View v, boolean checkState) {
                if (AppSharedPreference.getLocalcontactSwitch() != checkState) {
                    JoyCommClient.getInstance().setMatch(AppCache.getJoyId(), AppCache.getJoyToken(), checkState,new ICommProtocol.CommCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean match) {
                            settingSwitch.setCheck(match);
                            AppSharedPreference.setLocalcontactSwitch(match);
                            if (match) {
                                DialogMaker.showProgressDialog(LocalContactFriendActivity.this, "正在匹配好友...");
                                handler.postDelayed(runnable, 100);
                            } else {
                                items.clear();
                                localContactService.deleteAll();
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailed(String code, String errorMsg) {
                            settingSwitch.setCheck(AppSharedPreference.getLocalcontactSwitch());
                            Utils.showShortToast(LocalContactFriendActivity.this, "设置失败:"+errorMsg);
                        }
                    });
                }
            }
        });
        initListView();
    }

    private void initListView() {
        items = new LinkedList<>();
        adapter = new LocalContactAdapter(this, items, this);
        adapter.setOnClickEvent(new LocalContactAdapter.OnClickEvent() {
            @Override
            public void onAvatarClick(LocalContact localContact) {
                if (TextUtils.equals(localContact.getId(), AppCache.getJoyId())) {
                    UserProfileSettingActivity.start(LocalContactFriendActivity.this, AppCache.getJoyId());
                } else {
                    UserProfileActivity.start(LocalContactFriendActivity.this, localContact.getId());
                }
            }
        });
        listView = (ListView)findViewById(R.id.local_contact_listview);
        listView.setAdapter(adapter);

        // 加载本地保存的数据
        if (AppSharedPreference.getLocalcontactSwitch()) {
            items.addAll(localContactService.findAll());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return LocalContactViewHolder.class;
    }


    @Override
    public boolean enabled(int position) {
        return false;
    }


    private Map<String, String> getContactInfo() {
        Map<String, String> contactInfos = new HashMap<>();
        //读取手机本地的电话
        ContentResolver cr = getContentResolver();
        //取得电话本中开始一项的光标，必须先moveToNext()
        Cursor cursor =cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        if (cursor == null) {
            return contactInfos;
        }
        while(cursor.moveToNext()){
            //取得联系人的名字索引
            int nameIndex  =cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            String name = cursor.getString(nameIndex);

            //取得联系人的ID索引值
            String contactId =cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //查询该位联系人的电话号码，类似的可以查询email，photo
            Cursor phone =cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = "
                            + contactId, null, null);//第一个参数是确定查询电话号，第三个参数是查询具体某个人的过滤值
            //一个人可能有几个号码
            while(phone.moveToNext()){
                String phoneNumber =phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactInfos.put(TrimTellNumber.trimTelNum(phoneNumber), name);
            }
            phone.close();
        }
        cursor.close();

        return contactInfos;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_CONTACTS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (AppSharedPreference.getLocalcontactSwitch()) {
                DialogMaker.showProgressDialog(LocalContactFriendActivity.this, "正在匹配好友...");
                handler.postDelayed(runnable, 100);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
