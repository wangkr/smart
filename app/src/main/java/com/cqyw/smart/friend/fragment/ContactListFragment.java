package com.cqyw.smart.friend.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.contact.activity.LocalContactFriendActivity;
import com.cqyw.smart.friend.model.FriendTab;
import com.cqyw.smart.R;
import com.cqyw.smart.widget.roundedview.RoundedImageView;
import com.cqyw.smart.friend.activity.SystemMessageActivity;
import com.cqyw.smart.friend.helper.SystemMessageUnreadManager;
import com.cqyw.smart.friend.reminder.ReminderId;
import com.cqyw.smart.friend.reminder.ReminderItem;
import com.cqyw.smart.friend.reminder.ReminderManager;
import com.netease.nim.uikit.common.ui.dialog.CustomDialogViewHolder;
import com.netease.nim.uikit.contact.ContactsCustomization;
import com.netease.nim.uikit.contact.ContactsFragment;
import com.netease.nim.uikit.contact.core.item.AbsContactItem;
import com.netease.nim.uikit.contact.core.item.ItemTypes;
import com.netease.nim.uikit.contact.core.model.ContactDataAdapter;
import com.netease.nim.uikit.contact.core.viewholder.AbsContactViewHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * 集成通讯录列表
 * Created by huangjun on 2015/9/7
 */
public class ContactListFragment extends FriendTabFragment {

    private ContactsFragment fragment;

    public ContactListFragment() {
        this.setContainerId(FriendTab.CONTACT.fragmentId);
    }

    /**
     * ******************************** 功能项定制 ***********************************
     */
    public final static class FuncItem extends AbsContactItem {
        static final FuncItem PHONE_CONTACT = new FuncItem();
        static final FuncItem VERIFY = new FuncItem();

        @Override
        public int getItemType() {
            return ItemTypes.FUNC;
        }

        @Override
        public String belongsGroup() {
            return null;
        }

        public static final class FuncViewHolder extends AbsContactViewHolder<FuncItem> {
            private RoundedImageView image;
            private TextView funcName;
            private TextView unreadNum;
//            // added in v1.0.4
//            private ImageView funcSettings;

//            private View.OnClickListener onPhoneSettingListener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            };

            @Override
            public View inflate(LayoutInflater inflater) {
                View view = inflater.inflate(R.layout.func_contacts_item, null);
                this.image = (RoundedImageView) view.findViewById(R.id.img_head);
                this.funcName = (TextView) view.findViewById(R.id.tv_func_name);
                this.unreadNum = (TextView) view.findViewById(R.id.tab_new_msg_label);
                return view;
            }

            @Override
            public void refresh(ContactDataAdapter contactAdapter, int position, FuncItem item) {
                if (item == PHONE_CONTACT) {
                    funcName.setText(R.string.phone_contact);
                    image.setImageResource(R.drawable.phone_contact_icn);
                    image.setScaleType(ScaleType.FIT_XY);
                } else if (item == VERIFY) {
                    funcName.setText(R.string.verify_reminder);
                    image.setImageResource(R.drawable.system_notification_icn);
                    image.setScaleType(ScaleType.FIT_XY);
                    int unreadCount = SystemMessageUnreadManager.getInstance().getSysMsgUnreadCount();
                    updateUnreadNum(unreadCount);

                    ReminderManager.getInstance().registerUnreadNumChangedCallback(new ReminderManager.UnreadNumChangedCallback() {
                        @Override
                        public void onUnreadNumChanged(ReminderItem item) {
                            if (item.getId() != ReminderId.CONTACT) {
                                return;
                            }
                            updateUnreadNum(item.getUnread());
                        }
                    });
                }

                if (item != VERIFY) {
                    image.setScaleType(ScaleType.FIT_XY);
                    unreadNum.setVisibility(View.GONE);
                }
            }

            private void updateUnreadNum(int unreadCount) {
                // 2.*版本viewholder复用问题
                if (unreadCount > 0 && funcName.getText().toString().equals("验证提醒")) {
                    unreadNum.setVisibility(View.VISIBLE);
                    unreadNum.setText("" + unreadCount);
                } else {
                    unreadNum.setVisibility(View.GONE);
                }
            }
        }

        static List<AbsContactItem> provide() {
            List<AbsContactItem> items = new ArrayList<AbsContactItem>();
            items.add(PHONE_CONTACT);
            items.add(VERIFY);
            return items;
        }

        static void handle(Context context, AbsContactItem item) {
            if (item == PHONE_CONTACT) {
                // 启动通讯录匹配
                LocalContactFriendActivity.start(context);
            } else if (item == VERIFY) {
                SystemMessageActivity.start(context);
            }
        }
    }

    /**
     * ******************************** 生命周期 ***********************************
     */

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    protected void onInit() {
        // 集成通讯录页面
        addContactFragment();
    }

    // 将通讯录列表fragment动态集成进来。 开发者也可以使用在xml中配置的方式静态集成。
    private void addContactFragment() {
        fragment = new ContactsFragment();
        fragment.setContainerId(R.id.contact_fragment);

        JActionBarActivity activity = (JActionBarActivity) getActivity();

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        fragment = (ContactsFragment) activity.addFragment(fragment);

        // 功能项定制
        fragment.setContactsCustomization(new ContactsCustomization() {
            @Override
            public Class<? extends AbsContactViewHolder<? extends AbsContactItem>> onGetFuncViewHolderClass() {
                return FuncItem.FuncViewHolder.class;
            }

            @Override
            public List<AbsContactItem> onGetFuncItems() {
                return FuncItem.provide();
            }

            @Override
            public void onFuncItemClick(AbsContactItem item) {
                FuncItem.handle(getActivity(), item);
            }

        });
    }

    @Override
    public void onCurrentTabClicked() {
        if (fragment != null) {
            fragment.scrollToTop();
        }
    }
}
