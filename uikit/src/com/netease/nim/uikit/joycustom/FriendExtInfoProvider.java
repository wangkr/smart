package com.netease.nim.uikit.joycustom;

import android.content.Context;

/**
 * 好友扩展信息接口
 * Created by Kairong on 2015/12/28.
 * mail:wangkrhust@gmail.com
 */
public interface FriendExtInfoProvider {

    /**
     * 从服务器拉取备注等信息
     */
    public void fetchNoteInfofromRemote();

    /**
     * 添加备注
     * @param context
     * @param friend_id
     * @param note
     */
    public boolean addNote(Context context, String friend_id, String friend_nick, String note, String university, String jo);

    /**
     * 删除好友备注
     * 删除好友时调用
     * @param context
     * @param friend_id
     */
    public boolean deleteNote(Context context, String friend_id);

    /**
     * 更新昵称
     * @param context
     * @param friend_id
     * @param friend_nick
     */
    public boolean updateNick(Context context, String friend_id, String friend_nick);

    /**
     * 更新备注
     * @param context
     * @param friend_id
     * @param note
     * @return 更新状态
     */
    public boolean updateNote(Context context, String friend_id, String note);

    /**
     * 获取好友备注,从缓存获取
     * @param friend_id
     * @return
     */
    public String getNote(String friend_id);

    /**
     * 获取好友昵称
     * @param friend_id
     * @return
     */
    public String getNick(String friend_id);

    /**
     * 获取学校
     * @param friend_id
     * @return
     */
    public String getUniversity(String friend_id);

    /**
     * 获取好友Jo值
     * @param friend_id
     * @return
     */
    public String getJo(String friend_id);

    /**
     * 用户名称信息
     */
    class UserNameInfo{
        public String nick;
        public String note;
        public String university;
        public String jo;
        public UserNameInfo(String nick, String note,String university, String jo){
            this.nick = nick;
            this.note = note;
            this.university = university;
            this.jo = jo;
        }
    }
}
