package timark.zhou.poolokhttp;


/**
 * Created by Administrator on 2017/11/7.
 */

public class UserLoginRequest extends ModBaseRequest {

    private String loginName;
    private String password;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
