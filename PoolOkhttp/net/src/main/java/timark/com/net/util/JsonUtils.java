package timark.com.net.util;

import com.alibaba.fastjson.JSON;

/**
 * Created by ZHOU-PC on 2017/11/7.
 */

public class JsonUtils {
    public static String obj2Json(Object o){
        return JSON.toJSONString(o);
    }

    public static<T> T json2Obj(String json, Class<T> clazz){
        return JSON.parseObject(json, clazz);
    }
}
