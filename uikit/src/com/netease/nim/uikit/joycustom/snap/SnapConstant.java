package com.netease.nim.uikit.joycustom.snap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.joycustom.upyun.UpYun;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;


import java.io.File;

/**
 * Created by Kairong on 2015/11/3.
 * mail:wangkrhust@gmail.com
 */
public class SnapConstant {

    // snap content message keys
    public static final String KEY_COVER_NAME = "cover_name";
    public static final String KEY_SNAP_CONTENT = "snap_content";
    public static final String DEFAULT_COVER_FOLDER = "default";

    public enum SnapCover {
        OTHERS(0),
        SINGLE(1),
        ERCIYUAN(2),
        CUTE(3),
        WENYI(4);
        public int index;
        SnapCover(int index){
            this.index = index;
        }
    }

    private static final String[] snap_cover_title = new String[] { "其他", "单身", "二次元", "萌", "文艺"};

    private static final String[][] snap_cover_server_name = {
            {"ea0eea5b461ee264b2aa42843b8faeba.jpg","f783f849148cb01f458c6b5cade48138.jpg","d445f35b809940a118080e166914738f.jpg",
                    "3c00903098297a5fef510c21aa332ad2.jpg","f13a3a70b5c6c1301426496bf17d682b.jpg","f403320900b134e2af0b6ba3ba74aab6.jpg"},
            {"9e698816a1acedf4b4b6d6155b30aa21.jpg","cef4014097ed87393aefc6c32d3514b7.jpg","72b8ee70d525882cd0a8e61a545ccdd4.jpg",
                    "c3f4882711d27ff54466dcc70e8f465d.jpg","5e45575eae2e2e8d8301931dc63d5e0e.jpg","f34b452a46e88a31f81a4b8d07364675.jpg"},
            {"77bf0c9565978057bd8e7abbe677461a.jpg","72860c469242e975e32d6fc5d07b02cf.jpg","4e09766877786d378c889347a99e4257.jpg",
                    "2ad30500d27b18a155547fea19e8f14a.jpg","b32012c8506c7facb8885af8df4209db.jpg","bf8b6ac8e88ae9ba1197f39a1cca36ed.jpg"},
            {"32dd15667550fb55d6d3f6c1217d6ee4.jpg","25ca3a3b8a59e33823803e3af1660640.jpg","d4ac7032d844f3639540660f8f67f71a.jpg",
                    "86c6590bf78265a2726afe7255f775e6.jpg","23437239d2b5bf9b3fbda34697b15537.jpg","a62a88c93360774223f8b06268000326.jpg"},
            {"5282fcb044f951f11d3136318bc11e6d.jpg","44f7f938b861ba7f6fb60856c37a14bc.jpg","58145d8fe22a7c9bdc2a2e1487ba88de.jpg",
                    "f513820b7a79f83caad6764717732cc7.jpg","42007cadc3b50f7cf576098e9c47a763.jpg","7872bfecba604553ffc4d9df4dbc2a53.jpg"}};

    private static final int[] snap_coverIds_others = {R.drawable.cover_others1, R.drawable.cover_others2, R.drawable.cover_others3,
            R.drawable.cover_others4, R.drawable.cover_others5, R.drawable.cover_others6};
    private static final int[] snap_coverIds_cute = {R.drawable.cover_cute1, R.drawable.cover_cute2, R.drawable.cover_cute3,
            R.drawable.cover_cute4, R.drawable.cover_cute5, R.drawable.cover_cute6};
    private static final int[] snap_coverIds_erciyuan = {R.drawable.cover_erciyuan1, R.drawable.cover_erciyuan2, R.drawable.cover_erciyuan3,
            R.drawable.cover_erciyuan4, R.drawable.cover_erciyuan5, R.drawable.cover_erciyuan6};
    private static final int[] snap_coverIds_single = {R.drawable.cover_single1, R.drawable.cover_single2, R.drawable.cover_single3,
            R.drawable.cover_single4, R.drawable.cover_single5, R.drawable.cover_single6};
    private static final int[] snap_coverIds_wenyi = {R.drawable.cover_wenyi1, R.drawable.cover_wenyi2, R.drawable.cover_wenyi3,
            R.drawable.cover_wenyi4, R.drawable.cover_wenyi5, R.drawable.cover_wenyi6};

    /**
     * 获取本地封面id
     * @param cover
     * @return
     */
    public static int[] getSnapCoverIds(SnapCover cover){
        switch (cover){
            case OTHERS:
                return snap_coverIds_others;
            case CUTE:
                return snap_coverIds_cute;
            case ERCIYUAN:
                return snap_coverIds_erciyuan;
            case SINGLE:
                return snap_coverIds_single;
            case WENYI:
                return snap_coverIds_wenyi;
            default:
                return snap_coverIds_cute;
        }
    }

    /**
     * 获取封面类别号1
     * @param cover 封面
     * @return
     */
    public static String getSnapCoverCategory(SnapCover cover){
        return snap_cover_title[cover.index % snap_cover_title.length];
    }

    /**
     * 获取封面类别号2
     * @param coverId 封面id
     * @return
     */
    public static String getSnapCoverCategory(int coverId){
        return snap_cover_title[coverId % snap_cover_title.length];
    }

    public static int getCount(){
        return snap_cover_title.length;
    }

    /**
     * 获取服务器端默认封面名字
     * @param coverId 封面id
     * @param coverIndex 封面下标号
     * @return
     */
    public static String getServerDefaultCoverName(int coverId, int coverIndex){
        if (coverId >= getCount() || coverIndex >= 6){
            return StringUtil.Empty;
        }

        return DEFAULT_COVER_FOLDER + UpYun.SEPARATOR + snap_cover_server_name[coverId][coverIndex];
    }

    public static String getCoverAbsUrlFromJson(String json, JoyImageUtil.ImageType type){
        return JoyImageUtil.getCoverAbsUrl(parseCoverName(json), type);
    }

    public static String parseCoverName(String json){
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString(KEY_COVER_NAME);
    }

    public static String parseContent(String json){
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString(KEY_SNAP_CONTENT);
    }

//    /**
//     * 下载消息中的附件包括阅后即焚附件-封面
//     * @param message 消息
//     */
//    public static void downloadIMMessageAttachment(final IMMessage message){
//
//        String extension = ((FileAttachment) message.getAttachment()).getExtension();
//        if (!TextUtils.isEmpty(extension)){
//            String coverName = parseCoverName(extension);
//            if (TextUtils.isEmpty(coverName)) {
//                return;
//            }
//
//            message.setAttachStatus(AttachStatusEnum.transferring);
//            x.image().loadFile(UpYunImageUtil.getCoverAbsUrl(coverName, UpYunImageUtil.ImageType.V_300ad), ImageOptions.DEFAULT, new Callback.CommonCallback<File>() {
//                @Override
//                public void onSuccess(File result) {
//                    message.setAttachStatus(AttachStatusEnum.transferred);
//                }
//
//                @Override
//                public void onError(Throwable ex, boolean isOnCallback) {
//                    message.setAttachStatus(AttachStatusEnum.fail);
//                }
//
//                @Override
//                public void onCancelled(CancelledException cex) {
//                    message.setAttachStatus(AttachStatusEnum.fail);
//                }
//
//                @Override
//                public void onFinished() {
//
//                }
//            });
//        } else {
//            NIMClient.getService(MsgService.class).downloadAttachment(message, true);
//        }
//    }


    /**
     * 下载消息中的阅后即焚附件
     * @param upFilePath 文件名
     */
    public static File downloadSmartImage(String upFilePath){
        File tempFile = new File(StorageUtil.getWritePath(FileUtil.getFileNameFromPath(upFilePath), StorageType.TYPE_TEMP));
        if (JoyImageUtil.getJoyyunSmart().readFile(upFilePath, tempFile)){
            return tempFile;
        } else {
            return null;
        }
    }
}
