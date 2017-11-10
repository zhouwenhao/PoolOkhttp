package timark.com.net.netdata;

/**
 * Created by ZHOU-PC on 2017/11/6.
 */

public class BaseRequest {
    /**
     * 如果需要表单提交file   请用
     * Map<String, File> files
     * String为该File的key值
     */

    private String dk;
    private String token;

    public String getDk() {
        return dk;
    }

    public void setDk(String dk) {
        this.dk = dk;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
