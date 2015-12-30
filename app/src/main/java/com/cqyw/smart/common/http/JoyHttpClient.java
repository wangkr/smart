package com.cqyw.smart.common.http;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.JoyServers;
import com.cqyw.smart.login.protocol.LoginHttpClient;
import com.netease.nim.uikit.common.framework.NimTaskExecutor;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.netty.util.CharsetUtil;

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

    public class JoyHttpTask implements Runnable {

        private String url;
        private Map<String, String> headers;
        private JSONObject jsonBody;
        private JoyHttpCallback callback;

        public JoyHttpTask(String url, Map<String, String> headers, JSONObject jsonBody, JoyHttpCallback callback) {
            this.url = url;
            this.headers = headers;
            this.jsonBody = jsonBody;
            this.callback = callback;
        }

        @Override
        public void run() {
            String response = null;
            int errorCode = 0;
            try {
                response = post(url, headers, jsonBody);
            } catch (NimHttpException e) {
                errorCode = e.getHttpCode();
            } finally {
                final String res = response;
                final int code = errorCode;
                // do callback on ui thread
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onResponse(res, code, null);
                        }
                    }
                });
            }
        }
    }


    /**
     * ************************ Single instance **************************
     */
    private static JoyHttpClient instance;

    public synchronized static JoyHttpClient getInstance() {
        if (instance == null) {
            instance = new JoyHttpClient();
        }

        return instance;
    }

    private JoyHttpClient() {
        init();
    }

    /**
     * **************** Http Config & Thread pool & Http Client ******************
     */
    // 最大连接数
    public final static int MAX_CONNECTIONS = 10;

    // 获取连接的最大等待时间
    public final static int WAIT_TIMEOUT = 5 * 1000;

    // 每个路由最大连接数
    public final static int MAX_ROUTE_CONNECTIONS = 10;

    // 连接超时时间
    public final static int CONNECT_TIMEOUT = 5 * 1000;

    // 读取超时时间
    public final static int READ_TIMEOUT = 10 * 1000;

    private boolean inited = false;

    private HttpClient client;

    private ClientConnectionManager connManager;

    private NimTaskExecutor executor;

    private Handler uiHandler;

    public void init() {
        if (inited) {
            return;
        }

        // init thread pool
        executor = new NimTaskExecutor("NIM_HTTP_TASK_EXECUTOR", new NimTaskExecutor.Config(1, 3, 10 * 1000, true));

        // init HttpClient supporting multi thread access
        HttpParams httpParams = new BasicHttpParams();
        // 设置最大连接数
        ConnManagerParams.setMaxTotalConnections(httpParams, MAX_CONNECTIONS);
        // 设置获取连接的最大等待时间
        ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);
        // 设置每个路由最大连接数
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS));
        // 设置连接超时时间
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECT_TIMEOUT);
        // 设置读取超时时间
        HttpConnectionParams.setSoTimeout(httpParams, READ_TIMEOUT);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        SSLSocketFactory.getSocketFactory().setHostnameVerifier(new AllowAllHostnameVerifier());

        connManager = new ThreadSafeClientConnManager(httpParams, registry);
        client = new DefaultHttpClient(connManager, httpParams);

        uiHandler = new Handler(AppCache.getContext().getMainLooper());

        inited = true;
    }

    public void release() {
        if (executor != null) {
            executor.shutdown();
        }

        if (connManager != null) {
            connManager.shutdown();
        }

        client = null;
    }

    public void execute(String url, Map<String, String> headers, JSONObject jsonBody, JoyHttpCallback callback) {
        if (!inited) {
            return;
        }
        Log.d("JoyHttpClient", "init");
        executor.execute(new JoyHttpTask(url, headers, jsonBody, callback));
    }

    /**
     * **************************** useful method ************************
     */

    private String post(String url, Map<String, String> headers, JSONObject jsonBody) {
        HttpResponse response;
        HttpPost request;
        try {
            request = new HttpPost(url);

            // add request headers
            request.addHeader("charset", "utf-8");
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }

            String body = formBodyfromJson(jsonBody);

            // add body
            HttpEntity entity = null;
            if (body != null) {
                entity = new StringEntity(body, HTTP.UTF_8);
            }

            if (entity != null) {
                request.setEntity(entity);
            }

            // execute
            response = client.execute(request);

            // response
            StatusLine statusLine = response.getStatusLine();
            if (statusLine == null) {
                Log.e(TAG, "StatusLine is null");
                throw new NimHttpException();
            }
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                throw new NimHttpException(statusCode);
            }
            String entityString = EntityUtils.toString(response.getEntity(), "utf-8");
//            LogUtil.d(TAG, "url = "+ url + "result entity= "+entityString);
//             判断token是否过期：是，返回结果；否则，重新登录获取token
            checkJoyTokenValid(entityString);
            return entityString;
        } catch (Exception e) {
            if (e instanceof NimHttpException) {
                throw (NimHttpException) e;
            }
            Log.e(TAG, "Post data error", e);
            if (e instanceof UnknownHostException) {
                throw new NimHttpException(408);
            }
            throw new NimHttpException(e);
        }
    }
    private void checkJoyTokenValid(final String entityString) {
        JSONObject entityJson;
        String code;
        try {
            entityJson = JSONObject.parseObject(entityString);
            if (entityJson.size() > 0) {
                code = entityJson.getString(JoyHttpProtocol.REQUEST_KEY_CODE);
            } else {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "checkJoyToken failed");
            return;
        }
        try {
            if (TextUtils.equals(code, "999403") || TextUtils.equals(code, "999401")) {
                // 本地服务器登录重新获取token
                LogUtil.d(TAG, "checkJoyToken start...");
                String url = JoyServers.joyServer() + LoginHttpClient.API_NAME_LOGIN;
                String password = AppSharedPreference.getUserMD5Passwd();

                Map<String, String> headers = new HashMap<>(1);
                headers.put(LoginHttpClient.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(LoginHttpClient.REQUEST_KEY_PHONE, AppSharedPreference.getUserAccount());
                jsonObject.put(LoginHttpClient.REQUEST_KEY_PASSWORD, password);
                String bodyString = formBodyfromJson(jsonObject);

                HttpResponse response;
                HttpPost request;
                request = new HttpPost(url);

                // add request headers
                request.addHeader("charset", "utf-8");
                if (headers != null) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        request.addHeader(header.getKey(), header.getValue());
                    }
                }

                // add body
                HttpEntity entity = null;
                if (bodyString != null) {
                    entity = new StringEntity(bodyString);
                }

                if (entity != null) {
                    request.setEntity(entity);
                }

                // execute
                response = client.execute(request);

                // response
                StatusLine statusLine = response.getStatusLine();
                if (statusLine == null) {
                    Log.e(TAG, "StatusLine is null");
                    throw new NimHttpException();
                }
                int statusCode = statusLine.getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    throw new NimHttpException(statusCode);
                }
                String newTokenJson = EntityUtils.toString(response.getEntity());
                LogUtil.d(TAG, newTokenJson);
                JSONObject jsonObject1 = JSONObject.parseObject(newTokenJson);
                if (TextUtils.equals(jsonObject1.getString(JoyHttpProtocol.REQUEST_KEY_CODE).substring(4,6), JoyHttpProtocol.STATUS_CODE_SUCCESS)) {
                    JSONObject dataObj = jsonObject1.getJSONObject(JoyHttpProtocol.REQUEST_KEY_DATA);
                    String JoyToken = dataObj.getString(JoyHttpProtocol.RESULT_KEY_TOKEN);
                    if (!TextUtils.isEmpty(JoyToken)) {
                        AppSharedPreference.saveJoyToken(JoyToken);
                        AppCache.setJoyToken(JoyToken);
                        LogUtil.d(TAG, "update JoyToken success");
                    }
                } else {
                    LogUtil.d(TAG, "checkJoyToken failed " + jsonObject1.getString(JoyHttpProtocol.RESULT_KEY_MSG));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "checkJoyToken failed "+e.getMessage());
        } finally {
            LogUtil.d(TAG, "checkJoyToken done");
        }
    }

    private String formBodyfromJson(JSONObject jsonBody) {
        StringBuilder builder = new StringBuilder();
        Set<String> keyset = jsonBody.keySet();
        Iterator<String> it = keyset.iterator();
        while (it.hasNext()) {
            String key = it.next();
            builder.append(key).append("=").append(jsonBody.get(key)).append("&");
        }
        String bodyString = builder.toString();
        return bodyString.length() > 0 ? bodyString.substring(0, bodyString.length()-1) : "";
    }

}
