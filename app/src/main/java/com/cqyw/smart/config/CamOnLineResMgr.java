package com.cqyw.smart.config;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.common.http.JoyHttpClient;
import com.cqyw.smart.main.service.OnlineResDBService;
import com.netease.nim.uikit.common.media.picker.joycamera.ICamOnLineResMgr;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.file.ZipUtils;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Kairong on 2016/8/7.
 * mail:wangkrhust@gmail.com
 */
public class CamOnLineResMgr implements ICamOnLineResMgr {
    private static final String COVER_EXT = "cover";
    public static final String AR_EXT = "FBX";
    private static final String ICON_EXT = "icon";

    private static List<CamOnLineRes> coverItems = new CopyOnWriteArrayList<>();
    private static List<CamOnLineRes> arItems = new CopyOnWriteArrayList<>();
    private static Map<Integer, Integer> usedTimes = Collections.synchronizedMap(new HashMap<Integer, Integer>());
    private static HashSet<Integer> localIds = new HashSet<>();
    private static boolean inited = false;
    private static CamOnLineResMgr INSTANCE;

    private CamOnLineResMgr(){
        onlineResDBService = new OnlineResDBService(AppContext.getContext());
    }

    public static CamOnLineResMgr getInstance(){
        if (INSTANCE == null) {
            synchronized (CamOnLineResMgr.class) {
                if(INSTANCE == null) {
                    INSTANCE = new CamOnLineResMgr();
                }
            }
        }

        return INSTANCE;
    }
    private OnlineResDBService onlineResDBService;
    public static void moveFile(String assetPath, String sdcardPath, AssetManager am) throws IOException{
        FileOutputStream out = new FileOutputStream(sdcardPath);
        InputStream in = am.open(assetPath);
        byte[] buffer = new byte[1024];
        int lengh = in.read(buffer);

        while(lengh > 0){
            out.write(buffer, 0, lengh);
            lengh = in.read(buffer);
        }

        out.flush();
        in.close();
        out.close();
    }

    private void unZipCoverAssets(List<CamOnLineRes> colrCover) throws IOException{
        AssetManager am = AppContext.getContext().getAssets();
        String[] resPaths = am.list("camres/cover");
        String sdcard_dir = StorageUtil.getDirectoryByDirType(StorageType.TYPE_FILE) + root_dir + default_sub_dir;
        File file = new File(sdcard_dir);
        TreeMap<Integer, List<String>> map = new TreeMap<>();
        if (file.exists()){
            for (String path : resPaths){
                List<String> filePaths = ZipUtils.unZipFromAssets(am, "camres/cover/" +path, sdcard_dir + "cover", true);
                int id = Integer.valueOf(FileUtil.getFileNameNoEx(path));
                map.put(id, filePaths);
            }
        } else {
            throw new FileNotFoundException();
        }

        int index = 0;
        for(int id : map.keySet()) {
            CamOnLineRes colr = new CamOnLineRes();
            colr.setId(id);
            colr.setType(CamOnLineRes.Type.COVER);
            colr.setStatus(CamOnLineRes.Status.DEFAULT);
            colr.setLocalIndex(index++);
            for(String path : map.get(id)){
                if (COVER_EXT.equals(FileUtil.getExtensionName(path))){
                    colr.setCachePath(path);
                } else if (ICON_EXT.equals(FileUtil.getExtensionName(path))) {
                    colr.setIconCachePath(path);
                }
            }

            colrCover.add(colr);
        }

        map.clear();

    }

    private void unZipARAssets(List<CamOnLineRes> colrAR) throws IOException{
        AssetManager am = AppContext.getContext().getAssets();
        String[] resPaths = am.list("camres/ar");
        String sdcard_dir = StorageUtil.getDirectoryByDirType(StorageType.TYPE_FILE) + root_dir + default_sub_dir;
        File file = new File(sdcard_dir);
        TreeMap<Integer, List<String>> map = new TreeMap<>();
        if (file.exists()){
            for (String path : resPaths){
                List<String> filePaths = ZipUtils.unZipFromAssets(am, "camres/ar/" +path, sdcard_dir + "ar", true);
                int id = Integer.valueOf(FileUtil.getFileNameNoEx(path));
                map.put(id, filePaths);
            }
        } else {
            throw new FileNotFoundException();
        }

        int index = 0;
        for(int id : map.keySet()) {
            CamOnLineRes colr = new CamOnLineRes();
            colr.setId(id);
            colr.setType(CamOnLineRes.Type.AR);
            colr.setStatus(CamOnLineRes.Status.DEFAULT);
            colr.setLocalIndex(index++);
            for(String path : map.get(id)){
                if (AR_EXT.equals(FileUtil.getExtensionName(path))){
                    colr.setCachePath(path);
                } else if (ICON_EXT.equals(FileUtil.getExtensionName(path))) {
                    colr.setIconCachePath(path);
                }
            }

            colrAR.add(colr);
        }

        map.clear();

    }

    public static boolean checkFileExist(String path){
        if(TextUtils.isEmpty(path)) return false;
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    @Override
    public void uploadUsedtimes() {
        if (usedTimes.size() == 0) return;

        JoyCommClient.getInstance().uploadResUsedtimes(AppCache.getJoyId(), AppCache.getJoyToken(), usedTimes, new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                usedTimes.clear();
                onlineResDBService.deleteAllResUsedtimes();
            }

            @Override
            public void onFailed(String code, String errorMsg) {

            }
        });
    }

    @Override
    public void addUsedtimes(CamOnLineRes res) {
        if (usedTimes.containsKey(res.getId())){
            Integer times = usedTimes.get(res.getId());
            usedTimes.put(res.getId(), times+1);
        } else {
            usedTimes.put(res.getId(), 1);
        }
    }

    @Override
    public void saveSingleRes(CamOnLineRes res) {
        onlineResDBService.saveSingleRes(res);
    }

    @Override
    public void saveAll() {
        onlineResDBService.saveAllOnlineRes(arItems);
        onlineResDBService.saveAllOnlineRes(coverItems);
        onlineResDBService.saveResUsedtimes(usedTimes);
    }

    private void checkExist(List<CamOnLineRes> onLineRess, CamOnLineRes.Type type) throws IOException{
        AssetManager am = AppContext.getContext().getAssets();
        String folder = (type == CamOnLineRes.Type.AR ? "ar/" : "cover/");
        String sdcard_dir = StorageUtil.getDirectoryByDirType(StorageType.TYPE_FILE) + root_dir + default_sub_dir;
        for (CamOnLineRes colr: onLineRess){
            if (colr.getStatus() == CamOnLineRes.Status.COMPLETE) {
                boolean ise = checkFileExist(colr.getCachePath());

                ise &= checkFileExist(colr.getIconCachePath());

                if (!ise) {
                    colr.setStatus(CamOnLineRes.Status.NONE);
                }
            } else if (colr.getStatus() == CamOnLineRes.Status.DEFAULT) {
                boolean ise = checkFileExist(colr.getCachePath());

                ise &= checkFileExist(colr.getIconCachePath());

                if (!ise) {
                    String[] resPaths = am.list("camres/"+ folder + colr.getId());
                    for(String p : resPaths){
                        moveFile("camres/" + folder + colr.getId() + "/" + p, sdcard_dir + folder + colr.getId() + "/" + p, am);
                        if (AR_EXT.equals(FileUtil.getExtensionName(p)) || COVER_EXT.equals(FileUtil.getExtensionName(p))){
                            colr.setCachePath(sdcard_dir + folder + colr.getId() + "/" + p);
                        } else if (ICON_EXT.equals(FileUtil.getExtensionName(p))) {
                            colr.setIconCachePath(sdcard_dir + folder + colr.getId() + "/" + p);
                        }
//                        else {
//                            colr.getParamCachePaths().add(sdcard_dir + folder + colr.getId() + "/" + p);
//                        }
                    }
                }
            }
        }

        // 更新本地缓存
        onlineResDBService.saveAllOnlineRes(onLineRess);
    }

    @Override
    public void checkLocalResesExist(List<CamOnLineRes> res, CamOnLineRes.Type type, Callback callback) {
        try {
            // 检查文件是否存在
            checkExist(res, type);
            callback.onSuccess(res);
        } catch (IOException e) {
            callback.onFailed("文件错误");
        }
    }



    @Override
    public void initLocalReses(Callback<Void> callback) {
        if (inited) {
            if (callback != null)
            callback.onSuccess(null);
            return;
        }

        // 加载本地缓存
        List<CamOnLineRes> colrAR = onlineResDBService.findAllOnlineRes(localIds, CamOnLineRes.Type.AR);
        List<CamOnLineRes> colrCover = onlineResDBService.findAllOnlineRes(localIds, CamOnLineRes.Type.COVER);

        // 移动默认
        if (colrAR.size() == 0) {
            try {
                unZipARAssets(colrAR);
            } catch (IOException e){
                e.printStackTrace();
                if (callback != null)
                callback.onFailed("error:打开文件错误");
                return;
            }
        }

        if (colrCover.size() == 0) {
            try {
                unZipCoverAssets(colrCover);
            } catch (IOException e){
                e.printStackTrace();
                if (callback != null)
                callback.onFailed("error:打开文件错误");
                return;
            }
        }

        arItems.addAll(colrAR);
        coverItems.addAll(colrCover);

        usedTimes.putAll(onlineResDBService.findAllResUsedtimes());

        if (callback != null)
        callback.onSuccess(null);
        inited = true;
    }

    @Override
    public void pullOnlineReses(final Callback<Integer> callback) {
        JoyCommClient.getInstance().getOnlineRes(AppCache.getJoyId(), AppCache.getJoyToken(), new ICommProtocol.CommCallback<Map<String,List<CamOnLineRes>>>() {
            @Override
            public void onSuccess(Map<String,List<CamOnLineRes>> camOnLineRes) {
                Integer updateType = 0;
                int maxIndex = arItems.size();
                for(CamOnLineRes colr : camOnLineRes.get("ar")) {
                    if (!localIds.contains(colr.getId())) {
                        localIds.add(colr.getId());
                        colr.setLocalIndex(maxIndex++);
                        arItems.add(colr);
                        updateType |= CamOnLineRes.Type.AR.v();
                    }
                }

                maxIndex = coverItems.size();
                for(CamOnLineRes colr : camOnLineRes.get("cover")) {
                    if (!localIds.contains(colr.getId())) {
                        localIds.add(colr.getId());
                        colr.setLocalIndex(maxIndex++);
                        coverItems.add(colr);
                        updateType |= CamOnLineRes.Type.COVER.v();
                    }
                }

                callback.onSuccess(updateType);
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                callback.onFailed(errorMsg);
            }
        });
    }

    @Override
    public void downloadSingleObj(String url, final String path, final Callback callback) {
        JoyHttpClient.getInstance().downLoadFileAsyn(url, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) call.cancel();
                callback.onFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;
                    try {
                        is = response.body().byteStream();
                        File file = new File(path);
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                        fos.flush();
                        callback.onSuccess(null);
                    } catch (IOException e) {
                        callback.onFailed(e.getMessage());
                    } finally {
                        try {
                            if (is != null) is.close();
                        } catch (IOException e) {

                        }
                        try {
                            if (fos != null) fos.close();
                        } catch (IOException e) {

                        }
                    }
                }
            }
        });
    }

    @Override
    public void downloadSingleRes(CamOnLineRes res, Callback<CamOnLineRes> callback) {
        boolean result = true;
        boolean result2 = true;

        // 创建子目录
        File file = new File(StorageUtil.getDirectoryByDirType(StorageType.TYPE_FILE) + root_dir +
                (res.getType() == CamOnLineRes.Type.AR ? ar_sub_dir : cover_sub_dir) + res.getId());
        if (!file.exists()) {
            file.mkdir();
        }

        if (!checkFileExist(res.getCachePath())){
            result = JoyHttpClient.getInstance().downLoadFileSync(res.getUrl(), makeCachePath(res, res.getUrl()));

            // 主资源下载完成
            if (result && res.getType() == CamOnLineRes.Type.AR) { // AR资源需要解压
                String unzipDir = StorageUtil.getDirectoryByDirType(StorageType.TYPE_FILE) + root_dir + ar_sub_dir + res.getId();
                // 解压文件
                try {
                    File zipFile = new File(makeCachePath(res, res.getUrl()));
                    List<String> paths = ZipUtils.upZipFile(zipFile, unzipDir);
                    // 删除压缩文件以节省空间
                    zipFile.delete();
                    for (String p : paths) {
                        if (AR_EXT.equals(FileUtil.getExtensionName(p)))
                        {
                            res.setCachePath(p);
                        }
//                        else {
//                            res.getParamCachePaths().add(p);
//                        }
                    }
                } catch (IOException e) {
                    result = false;
                }
            }
            else if(result) {
                res.setCachePath(makeCachePath(res, res.getUrl()));
            }
        }

        if (!checkFileExist(res.getIconCachePath())){
            result2 = JoyHttpClient.getInstance().downLoadFileSync(res.getIconUrl(), makeCachePath(res, res.getIconUrl()));
            // 缩略图下载完成
            if (result2) {
                res.setIconCachePath(makeCachePath(res, res.getIconUrl()));
            }
        }

        if (result & result2) {
            res.setStatus(CamOnLineRes.Status.COMPLETE);
            callback.onSuccess(res);
        } else if (result | result2) {
            res.setStatus(CamOnLineRes.Status.PART);
            callback.onSuccess(res);
        } else {
            callback.onFailed("");
        }
    }

    public static String makeCachePath(CamOnLineRes res, String url){
        return StorageUtil.getDirectoryByDirType(StorageType.TYPE_FILE) + root_dir +
                (res.getType() == CamOnLineRes.Type.AR ? ar_sub_dir : cover_sub_dir) + res.getId() + "/" + FileUtil.getFileNameFromPath(url);
    }

    @Override
    public List<CamOnLineRes> getARItems() {
        return arItems;
    }

    @Override
    public List<CamOnLineRes> getCoverItems() {
        return coverItems;
    }

    @Override
    public Map<Integer, Integer> getUsedTimes() {
        return usedTimes;
    }

    @Override
    public CamOnLineRes getItem(CamOnLineRes.Type type, int position) {
        if (type == CamOnLineRes.Type.AR && position < arItems.size()) {
            return arItems.get(position);
        }

        if (type == CamOnLineRes.Type.COVER && position < coverItems.size()) {
            return coverItems.get(position);
        }

        return null;
    }

}
