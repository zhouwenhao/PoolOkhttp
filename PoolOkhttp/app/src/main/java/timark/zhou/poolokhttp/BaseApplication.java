package timark.zhou.poolokhttp;

import android.app.Application;

import timark.com.net.NetManager;

/**
 * Created by ZHOU-PC on 2017/11/10.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        NetManager.init();
        NetManager.setIp("http://192.168.40.67:8080/");
    }
}
