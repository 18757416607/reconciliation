package com.reconciliation.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/2.
 * JOSN工具类
 */
public class JsonUtil {

    /**
     * json字符串转换Map对象
     * @param str
     * @return
     * @throws IOException
     */
    public static Map jsonToMap(String str) throws IOException {
        return new ObjectMapper().readValue(str,Map.class);
    }

}
