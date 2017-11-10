package timark.com.net.netconf;

/**
 * Created by ZHOU-PC on 2017/11/7.
 */

public class NetConfig {
    private static String ip = "";

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        if (!ip.endsWith("/")){
            NetConfig.ip = ip+"/";
        }else {
            NetConfig.ip = ip;
        }
    }
}
