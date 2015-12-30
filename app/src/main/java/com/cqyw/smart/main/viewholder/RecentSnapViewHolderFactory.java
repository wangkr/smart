package com.cqyw.smart.main.viewholder;

import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.model.SnapNewTypeEnum;

import java.util.HashMap;

/**
 * Created by Kairong on 2015/11/20.
 * mail:wangkrhust@gmail.com
 */
public class RecentSnapViewHolderFactory {
    private static HashMap<String, Class<? extends RecentViewHolderBase>> viewHolders = new HashMap<>();

    static {
        // built in
        register(RecentAnswerViewHolder.class.getSimpleName(), RecentAnswerViewHolder.class);
        register(RecentCommentViewHolder.class.getSimpleName(), RecentCommentViewHolder.class);
        register(RecentLikeViewHolder.class.getSimpleName(), RecentLikeViewHolder.class);
        register(RecentWarningViewHolder.class.getSimpleName(), RecentWarningViewHolder.class);
    }

    public static void register(String tag, Class<? extends RecentViewHolderBase> viewHolder) {
        viewHolders.put(tag, viewHolder);
    }

    public static Class<? extends RecentViewHolderBase> getViewHolderByType(RecentSnapNews message) {
        if (message != null) {
            if (message.getType() == SnapNewTypeEnum.ANSWER) {
                return viewHolders.get("RecentAnswerViewHolder");
            } else if (message.getType() == SnapNewTypeEnum.COMMENT) {
                return viewHolders.get("RecentCommentViewHolder");
            } else if (message.getType() == SnapNewTypeEnum.LIKE) {
                return viewHolders.get("RecentLikeViewHolder");
            } else if (message.getType() == SnapNewTypeEnum.WARNING) {
                return viewHolders.get("RecentWarningViewHolder");
            }
        }
        return viewHolders.get("RecentCommentViewHolder");
    }

    public static int getViewTypeCount() {
        // plus text and unknown
        return viewHolders.size();
    }
}
