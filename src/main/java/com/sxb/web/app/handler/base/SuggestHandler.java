package com.sxb.web.app.handler.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sxb.web.app.handler.base.suggest.SuggestService;
import com.sxb.web.commons.util.RetUtil;


@Service("suggestHandler")
public class SuggestHandler {

    @Resource(name="bSSuggestService")
    private SuggestService suggestService;

    
    public Map<String,Object> indexSuggest() throws IOException {
        suggestService.indexSuggest();
        System.out.println("索引创建成功。");
        return RetUtil.getRetValue(true);
    }

    public Map<String,Object> searchSuggest(String key) throws Exception {
        if(StringUtils.hasLength(key) && key.length() >= 2){
            return suggestService.searchSuggest(key,null);
        }
        return RetUtil.getRetValue(true,new ArrayList<Object>());
    }
    
}
