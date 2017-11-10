package timark.com.net.netdata;

/**
 * Created by ZHOU-PC on 2017/11/6.
 */

public class BaseResponse {
    private int errCode;
    private String errMsg;
    private BaseInnerData data;

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public BaseInnerData getData() {
        return data;
    }

    public void setData(BaseInnerData data) {
        this.data = data;
    }
}
