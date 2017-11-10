package timark.com.net.netconf;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import timark.com.net.NetCallback;
import timark.com.net.NetCode;
import timark.com.net.netdata.BaseInnerData;
import timark.com.net.netdata.BaseResponse;
import timark.com.net.util.JsonUtils;

/**
 * Created by ZHOU-PC on 2017/11/6.
 */

public class NetPool {
    private static NetPool mInstance;

    private int maxCallSize;
    private List<CallClass> mCallQueue = new ArrayList<>();       //一般优先级 正在并发请求池
    private List<CallClass> mCallWaitQueue = new ArrayList<>();       //一般优先级 等待并发请求池

    private Handler mMainHandler;

    private NetPool(){

    }

    private NetPool(int maxSize){
        maxCallSize = maxSize;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public synchronized static void init(int maxSize){
        if (mInstance == null){
            mInstance = new NetPool(maxSize);
        }
    }

    public static NetPool getInstance(){
        return mInstance;
    }

    public void normalExcute(Call call, NetCallback callback){
        CallClass item = new CallClass(call, callback);
        if (mCallQueue.size() < maxCallSize){
            mCallQueue.add(item);
            try {
                call.enqueue(new PoolCallBack(item));
            }catch (Exception e) {
                mCallQueue.remove(call);
            }
        }else {
            mCallWaitQueue.add(item);
        }
    }

    public void criticalExcute(Call call, final NetCallback callback){
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendResponse(netError(), callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                sendResponse(str2Response(response.body().string(), callback.getGenericType()), callback);
            }
        });
    }

    private void removeAndAdd(CallClass item){
        mCallQueue.remove(item);
    }

    private void aginEnqueue(){
        if (mCallQueue.size() < maxCallSize && !mCallWaitQueue.isEmpty()){
            CallClass item = mCallWaitQueue.get(0);
            mCallQueue.add(item);
            mCallWaitQueue.remove(item);
            item.call.enqueue(new PoolCallBack(item));
        }
    }

    private void sendResponse(final BaseResponse baseResponse, final NetCallback callback){
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(baseResponse);
            }
        });
    }

    private <T> BaseResponse str2Response(String str, Class<T> clazz){
        BaseResponse baseResponse = new BaseResponse();
        JSONObject jObj = JSON.parseObject(str);
        if (jObj.containsKey("errCode") && jObj.containsKey("errMsg")){
            baseResponse.setErrCode(jObj.getInteger("errCode"));
            baseResponse.setErrMsg(jObj.getString("errMsg"));
        }else {
            baseResponse.setErrCode(NetCode.FORMAT_EEROR);
            baseResponse.setErrMsg("返回数据格式错误");
        }
        if (jObj.containsKey("data")){
            String dataStr = jObj.getString("data");
            BaseInnerData data = (BaseInnerData) JsonUtils.json2Obj(dataStr, clazz);
            baseResponse.setData(data);
        }
        return baseResponse;
    }

    private BaseResponse netError(){
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setErrCode(NetCode.NET_ERROR);
        baseResponse.setErrMsg("网络连接失败");
        return baseResponse;
    }

    class PoolCallBack implements Callback{

        private CallClass item;

        public PoolCallBack(CallClass c){
            item = c;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            removeAndAdd(item);
            aginEnqueue();

            sendResponse(netError(), item.callback);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            removeAndAdd(item);
            aginEnqueue();

            sendResponse(str2Response(response.body().string(), item.callback.getGenericType()), item.callback);
        }
    }

    private class CallClass{
        public Call call;
        public NetCallback callback;

        public CallClass(Call c, NetCallback n){
            call = c;
            callback = n;
        }
    }
}
