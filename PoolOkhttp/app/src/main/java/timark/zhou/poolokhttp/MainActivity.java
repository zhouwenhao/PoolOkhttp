package timark.zhou.poolokhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import timark.com.net.NetCallback;
import timark.com.net.NetCode;
import timark.com.net.netdata.BaseResponse;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserLoginRequest request = new UserLoginRequest();
        request.setLoginName("test");
        request.setPassword("test");
        TestController.login(request, new NetCallback<UserLoginResponseData>() {
            @Override
            public void onResponse(BaseResponse response) {
                if (response.getErrCode() == NetCode.RESULT_OK){
                    UserLoginResponseData data = (UserLoginResponseData)response.getData();
                }else {
                    Toast.makeText(MainActivity.this, response.getErrMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
