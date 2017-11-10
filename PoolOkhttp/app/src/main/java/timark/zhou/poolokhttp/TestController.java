package timark.zhou.poolokhttp;

import timark.com.net.NetCallback;
import timark.com.net.NetManager;

/**
 * Created by ZHOU-PC on 2017/11/10.
 */

public class TestController {
    protected static final int NORMAL_LEVEL = 0;
    protected static final int CRITICAL_LEVEL = 1;

    public static void login(UserLoginRequest request, NetCallback<UserLoginResponseData> callback){
        NetManager.getInstance().httpPostByJson(NORMAL_LEVEL, UserServiceContans.LOGIN, request, callback);
    }

}
