package com.cqyw.smart.common.http;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.JoyServers;
import com.cqyw.smart.login.protocol.LoginHttpClient;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Kairong on 2015/10/27.
 * mail:wangkrhust@gmail.com
 */
public class JoyHttpClient {

    private static final String TAG = "JoyHttpClient";

    /**
     * *********************** Http Task & Callback *************************
     */
    public interface JoyHttpCallback {
        void onResponse(String response, int code, String errorMsg);
    }


    /**
     * ************************ Single instance **************************
     */
    private static JoyHttpClient instance;

    public static JoyHttpClient getInstance() {
        if (instance == null) {
            synchronized (JoyHttpClient.class) {
                if(instance == null) {
                    instance = new JoyHttpClient();
                }
            }
        }

        return instance;
    }

    private JoyHttpClient() {
        init();
    }

    private class InnerCallback implements Callback {
        final JoyHttpCallback callback;
        final boolean checkToken;
        final boolean onUIcallback;

        public InnerCallback(JoyHttpCallback callback, boolean checkToken, boolean onUIcallback) {
            this.callback = callback;
            this.checkToken = checkToken;
            this.onUIcallback = onUIcallback;
        }

        @Override
        public void onFailure(Call call, final IOException e) {
            if (!call.isCanceled())call.cancel();

            if (callback != null) {
                if (onUIcallback) {
                    postOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(e.getMessage(), -1, e.getMessage());
                        }
                    });
                } else {
                    callback.onResponse(e.getMessage(), -1, e.getMessage());
                }
            }
        }

        @Override
        public void onResponse(final Call call, final Response response) throws IOException {

            if (callback == null) return;

            final String body = response.body().string();

            if (response.isSuccessful()) {
                // 检测token是否过期：是，则自动重置
                boolean result = true;
                if (checkToken) {
                    result = checkJoyTokenValid(body);
                }
                if (result) {
                    if (onUIcallback) {
                        postOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse(body, 0, null);
                            }
                        });
                    } else {
                        callback.onResponse(body, 0, null);
                    }
                }
                else // 检查token中，重新发送刚才的请求
                {
                    enqueue(call.request(), new Callback()
                    {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            if (onUIcallback) {
                                postOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResponse(e.getMessage(), -1, e.getMessage());
                                    }
                                });
                            } else {
                                callback.onResponse(e.getMessage(), -1, e.getMessage());
                            }
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException
                        {
                            final String body = response.body().string();
                            if (response.isSuccessful()) {
                                // 检测token是否过期：是，则自动重置
                                if (checkJoyTokenValid(body)) {
                                    if (onUIcallback) {
                                        postOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResponse(body, 0, null);
                                            }
                                        });
                                    } else {
                                        callback.onResponse(body, 0, null);
                                    }
                                } else {
                                    if (onUIcallback) {
                                        postOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResponse("", -1, "token检查失败");
                                            }
                                        });
                                    } else {
                                        callback.onResponse("", -1, "token检查失败");
                                    }
                                }
                            } else {
                                if (onUIcallback) {
                                    postOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onResponse(body, -1, response.message());
                                        }
                                    });
                                } else {
                                    callback.onResponse(body, -1, response.message());
                                }
                            }
                        }
                    });
                }
            } else {
                if (onUIcallback) {
                    postOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(body, -1, response.message());
                        }
                    });
                } else {
                    callback.onResponse(body, -1, response.message());
                }
            }
        }
    }

    /**
     * **************** Http Config & Thread pool & Http Client ******************
     */

    // 获取连接的最大等待时间
    public final static int WAIT_TIMEOUT = 5 * 1000;

    // 连接超时时间
    public final static int CONNECT_TIMEOUT = 5 * 1000;

    // 读取超时时间
    public final static int READ_TIMEOUT = 10 * 1000;

    private boolean inited = false;

    private OkHttpClient okHttpClient;

    private Handler uiHandler;

    public void init() {
        if(inited) {
            return;
        }
        okHttpClient = new OkHttpClient();
        okHttpClient.newBuilder().
                readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS).
                connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS).
                writeTimeout(WAIT_TIMEOUT, TimeUnit.MILLISECONDS).
                build();
        uiHandler = new Handler(AppCache.getContext().getMainLooper());
        inited = true;
    }

    public void release() {
        okHttpClient.dispatcher().cancelAll();
        okHttpClient.dispatcher().executorService().shutdownNow();
        okHttpClient = null;
        instance = null;
        inited = false;
    }

    public void cancellAll(){
        okHttpClient.dispatcher().cancelAll();
    }

    private void postOnUiThread(Runnable runnable) {
        uiHandler.post(runnable);
    }

    private void enqueue(Request request, Callback callback) {
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void execute(String url, Map<String, String> headers, JSONObject jsonBody, final JoyHttpCallback callback, final boolean checkToken, final boolean onUIcallback) {
        if (!inited) {
            return;
        }

        Request request = new Request.Builder()
                .headers(Headers.of(headers))
                .url(url)
                .post(formBodyfromJson(jsonBody))
                .build();

        enqueue(request,new InnerCallback(callback, checkToken, onUIcallback));
    }

    public void downLoadFileAsyn(String url, Callback callback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public boolean downLoadFileSync(String url, String path){
        final Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    File file = new File(path);
                    if (file.exists())
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    //如果下载文件成功，第一个参数为文件的绝对路径
                    return true;
                } catch (IOException e) {
                    return false;
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
        } catch (IOException e){
            return false;
        }
        return false;
    }

    private boolean checkJoyTokenValid(final String entityString) {
        LogUtil.d(TAG, entityString);
        JSONObject entityJson;
        String code;
        try {
            entityJson = JSONObject.parseObject(entityString);
            if (entityJson.size() > 0) {
                code = entityJson.getString(JoyHttpProtocol.REQUEST_KEY_CODE);
            } else {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }

        if (TextUtils.equals(code, "999403") || TextUtils.equals(code, "999401")) {
            // 本地服务器登录重新获取token
            String url = JoyServers.joyServer() + LoginHttpClient.API_NAME_LOGIN;
            String password = AppSharedPreference.getUserMD5Passwd();

            Request request = new Request.Builder().
                    url(url).
                    header(LoginHttpClient.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8").
                    post(new FormBody.Builder().
                            add(LoginHttpClient.REQUEST_KEY_PHONE, AppSharedPreference.getUserAccount()).
                            add(LoginHttpClient.REQUEST_KEY_PASSWORD, password).build()).
                    build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String newTokenJson = response.body().string();
                        JSONObject jsonObject1 = JSONObject.parseObject(newTokenJson);
                        if (TextUtils.equals(jsonObject1.getString(JoyHttpProtocol.REQUEST_KEY_CODE).substring(4, 6),
                                JoyHttpProtocol.STATUS_CODE_SUCCESS)) {
                            JSONObject dataObj = jsonObject1.getJSONObject(JoyHttpProtocol.REQUEST_KEY_DATA);
                            String JoyToken = dataObj.getString(JoyHttpProtocol.RESULT_KEY_TOKEN);
                            if (!TextUtils.isEmpty(JoyToken)) {
                                AppSharedPreference.saveJoyToken(JoyToken);
                                AppCache.setJoyToken(JoyToken);
                            }
                        }
                    }
                }
            });
            return false;
        }
        return true;
    }

    @NonNull
    private FormBody formBodyfromJson(JSONObject jsonBody) {
        FormBody.Builder builder = new FormBody.Builder();
        Set<String> keyset = jsonBody.keySet();
        Iterator<String> it = keyset.iterator();
        while (it.hasNext()) {
            String key = it.next();
            builder.add(key, jsonBody.getString(key));
        }
        return builder.build();
    }

}
