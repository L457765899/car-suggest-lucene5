package com.sxb.web.app.controller.base;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.sxb.web.app.handler.base.SuggestHandler;

@Controller
@RequestMapping("/app/base/suggest")
public class SuggestController {
    
    @Autowired
    private SuggestHandler suggestHandler;
    
    /**
     * 搜索条件联想，提供给demo
     * @param key
     * @return
     * @throws Exception
     */
    @RequestMapping("/searchSuggestWeb")
    @ResponseBody
    public String searchSuggestWeb(String key) throws Exception{
        return new Gson().toJson(suggestHandler.searchSuggest(key));
    }
    
    /**
     * 寻销车-联想数据索引
     * @return
     * @throws IOException
     */
    @RequestMapping("/indexSuggest")
    @ResponseBody
    public String indexSuggest() throws IOException{
        return new Gson().toJson(suggestHandler.indexSuggest());
    }
    
}
