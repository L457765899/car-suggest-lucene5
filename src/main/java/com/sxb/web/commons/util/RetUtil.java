package com.sxb.web.commons.util;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RetUtil {
	
	public static final int ReturnCodeValue_Normal = 200;


    /**
     * 生成自定义的tokenId 作为用户的唯一标识tokenId (32位)
     ***/
    public static String formTokenId() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    public static Map<String, Object> getRetValue(boolean b, Object datas, String msg, int code) {
        Map<String, Object> retMp = new HashMap<String, Object>();
        retMp.put("success", b);
        retMp.put("errMsg", msg);
        retMp.put("code", code);
        retMp.put("datas", datas);
        return retMp;
    }

    public static Map<String, Object> getRetValue(boolean b, Object datas) {
        Map<String, Object> retMp = new HashMap<String, Object>();
        retMp.put("success", b);
        retMp.put("errMsg", "");
        retMp.put("code", ReturnCodeValue_Normal);
        retMp.put("datas", datas);
        return retMp;
    }

    public static Map<String, Object> getRetValue(boolean b, Object datas, String msg) {
        Map<String, Object> retMp = new HashMap<String, Object>();
        retMp.put("success", b);
        retMp.put("errMsg", msg);
        retMp.put("code", ReturnCodeValue_Normal);
        retMp.put("datas", datas);
        return retMp;
    }


    public static Map<String, Object> getRetValue(Object datas) {
        Map<String, Object> retMp = new HashMap<String, Object>();
        retMp.put("success", true);
        retMp.put("errMsg", "");
        retMp.put("code", ReturnCodeValue_Normal);
        retMp.put("datas", datas);
        return retMp;
    }


    public static Map<String, Object> getRetValue(boolean b) {
        Map<String, Object> retMp = new HashMap<String, Object>();
        retMp.put("success", b);
        retMp.put("errMsg", "");
        retMp.put("code", ReturnCodeValue_Normal);
        retMp.put("datas", new HashMap<String, Object>());
        return retMp;
    }


    public static Map<String, Object> getRetValue(boolean b, String msg) {
        Map<String, Object> retMp = new HashMap<String, Object>();
        retMp.put("success", b);
        retMp.put("errMsg", msg);
        retMp.put("code", ReturnCodeValue_Normal);
        retMp.put("datas", new HashMap<String, Object>());
        return retMp;
    }

    public static Map<String, Object> getRetValue(boolean b, String msg, int code) {
        Map<String, Object> retMp = new HashMap<String, Object>();
        retMp.put("success", b);
        retMp.put("errMsg", msg);
        retMp.put("code", code);
        retMp.put("datas", new HashMap<String, Object>());
        return retMp;
    }

    public static Reader getReader(String fileName){
    	ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    	String path = contextClassLoader.getResource("com/sxb/web/app/handler/base/suggest/").getPath() + fileName;
		try {
			FileInputStream in = new FileInputStream(path);
			Reader reader = new InputStreamReader(in, "UTF-8");
			return reader;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}
