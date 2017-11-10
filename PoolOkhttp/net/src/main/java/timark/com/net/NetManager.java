package timark.com.net;


import com.parkingwang.okhttp3.LogInterceptor.LogInterceptor;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timark.com.net.netconf.NetConfig;
import timark.com.net.netconf.NetPool;
import timark.com.net.netdata.BaseRequest;
import timark.com.net.netdata.BaseResponse;
import timark.com.net.util.JsonUtils;

/**
 * Created by ZHOU-PC on 2017/11/6.
 */

public class NetManager {
    private static final int NET_POOL_NUM = 10;

    private static NetManager mInstance;
    private OkHttpClient mOkHttpClient;
    private NetPool mNetPool;

    private static int readTimeOut = 3000;
    private static int writeTimeOut = 3000;
    private static int connectTimeOut = 3000;

    private NetManager(int readTimeOut, int writeTimeOut, int connectTimeOut){
        mOkHttpClient = new OkHttpClient.Builder().readTimeout(readTimeOut, TimeUnit.SECONDS).writeTimeout(writeTimeOut, TimeUnit.SECONDS).connectTimeout(connectTimeOut, TimeUnit.SECONDS).addInterceptor(new LogInterceptor()).build();
        mNetPool = NetPool.getInstance();
    }

    public synchronized static NetManager init(int readTimeOut, int writeTimeOut, int connectTimeOut){
        if (mInstance == null){
            NetPool.init(NET_POOL_NUM);
            mInstance = new NetManager(readTimeOut, writeTimeOut, connectTimeOut);
        }
        return mInstance;
    }

    public synchronized static NetManager init(){
        if (mInstance == null){
            NetPool.init(NET_POOL_NUM);
            mInstance = new NetManager(readTimeOut, writeTimeOut, connectTimeOut);
        }
        return mInstance;
    }

    public static void setIp(String ip){
        NetConfig.setIp(ip);
    }

    public static NetManager getInstance(){
        return mInstance;
    }

    public<T extends BaseRequest> void httpPostByJson(int level, String url, T baseRequest, NetCallback callback){
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, JsonUtils.obj2Json(baseRequest));
        sendAsync(level, url, body, callback);
    }

    public<T extends BaseRequest, W extends BaseResponse> W httpPostByJson(String url, T baseRequest, Class<W> wClass){
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, JsonUtils.obj2Json(baseRequest));
        return sendExcute(url, body, wClass);
    }

    public<T extends BaseRequest> void httpPostByForm(int level, String url, T baseRequest, NetCallback callback){
        RequestBody body = request2Form(baseRequest);
        sendAsync(level, url, body, callback);
    }

    public<T extends BaseRequest, W extends BaseResponse> W  httpPostByForm(String url, T baseRequest, Class<W> wClass){
        RequestBody body = request2Form(baseRequest);
        return sendExcute(url, body, wClass);
    }

    public<T extends BaseRequest> void httpPostByFile(int level, String url, T baseRequest, NetCallback callback){
        RequestBody body = request2FormFile(baseRequest);
        sendAsync(level, url, body, callback);
    }

    public<T extends BaseRequest, W extends BaseResponse> W  httpPostByFile(String url, T baseRequest, Class<W> wClass){
        RequestBody body = request2FormFile(baseRequest);
        return sendExcute(url, body, wClass);
    }

    private void sendAsync(int level, String url, RequestBody body, NetCallback callback){
        Request request = new Request.Builder().url(getUrl(url)).post(body).build();
        Call call = mOkHttpClient.newCall(request);
        excute(level, call, callback);
    }

    private <T extends BaseResponse> T sendExcute(String url, RequestBody body, Class<T> tClass){
        Request request = new Request.Builder().url(getUrl(url)).post(body).build();
        Call call = mOkHttpClient.newCall(request);
        try {
            Response res = call.execute();
            return JsonUtils.json2Obj(res.body().string(), tClass);
        }catch (IOException e) {
            return null;
        }
    }

    private <T extends BaseRequest> RequestBody request2Form(T baseRequest){
        FormBody.Builder form = new FormBody.Builder();
        JSONObject hostObject = null;
        try {
            hostObject = new JSONObject(JsonUtils.obj2Json(baseRequest));
            Iterator<String> sIterator = hostObject.keys();
            while(sIterator.hasNext()){
                String key = sIterator.next();
                if (key.toLowerCase().equals("files")) {
                    continue;
                }
                String value = hostObject.getString(key);
                form.add(key, value);
            }
        }catch (Exception e){

        }
        return form.build();
    }

    private<T extends BaseRequest> RequestBody request2FormFile(T baseRequest){
        MultipartBody.Builder fileBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        JSONObject hostObject = null;
        try {
            hostObject = new JSONObject(JsonUtils.obj2Json(baseRequest));
            Iterator<String> sIterator = hostObject.keys();
            while(sIterator.hasNext()){
                String key = sIterator.next();
                if (key.toLowerCase().equals("files")) {
                    continue;
                }
                String value = hostObject.getString(key);
                fileBuilder.addFormDataPart(key, value);
            }
            HashMap<String, File> fileMap = getRequestFiles(baseRequest);
            for (String fileKey : fileMap.keySet()) {
                File file = fileMap.get(fileKey);
                fileBuilder.addFormDataPart(fileKey, file.getName(), RequestBody.create(MediaType.parse("image/png"), file));
            }
        }catch (Exception e){

        }
        return fileBuilder.build();
    }

    private void excute(int level, Call call, NetCallback callback){
        if (level == 0) {
            mNetPool.normalExcute(call, callback);
        }else {
            mNetPool.criticalExcute(call, callback);
        }
    }

    private String getUrl(String uri){
        if (uri.startsWith("http")){
            return uri;
        }
        if (uri.startsWith("/")){
            uri = uri.substring(1, uri.length());
        }
        return NetConfig.getIp() + uri;
    }

    private static<T extends BaseRequest> HashMap<String, File> getRequestFiles(T request){
        HashMap<String, File> fileMap = new HashMap<>();
        Field[] fs = request.getClass().getDeclaredFields();
        for(int i = 0 ; i < fs.length; i++){
            try {
                Field f = fs[i];
                f.setAccessible(true); //设置些属性是可以访问的
                Object val = f.get(request);//得到此属性的值
                String type = f.getType().toString();//得到此属性的类型
                if (type.endsWith("Map")) {
                    try {
                        fileMap = (HashMap<String, File>) val;
                    }catch (Exception e){

                    }
                    break;
                }
            }catch (IllegalAccessException e){

            }
        }
        return fileMap;
    }
}
