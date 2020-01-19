package com.ztesoft.config.compare.utils;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 构造前台需要的响应结果
 */
public class ResponseUtil {
    private ResponseUtil() {
    }

    /**
     * 组装相应信息给前台，用于渲染表格
     *
     * @param list
     * @return
     */
    public static Map<String, Object> renderTableResponse(List list) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 200);
        map.put("message", "");
        map.put("total", list.size());
        map.put("item", list);
        return map;
    }

    public static Map<String, Object> renderResponse(Integer code, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("resultCode", code);
        map.put("message", msg);
        return map;
    }
}
