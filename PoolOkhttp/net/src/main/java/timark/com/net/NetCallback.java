package timark.com.net;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import timark.com.net.netdata.BaseInnerData;
import timark.com.net.netdata.BaseResponse;

/**
 * Created by ZHOU-PC on 2017/11/6.
 */

public abstract class NetCallback<T extends BaseInnerData> {

    public abstract void onResponse(BaseResponse response);

    public Class getGenericType(){
        Type genType = this.getClass().getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)){
            return Object.class;
        }else {
            Type[] params = ((ParameterizedType)genType).getActualTypeArguments();
            return !(params[0] instanceof Class) ? Object.class:(Class)params[0];
        }
    }
}
