package com.sxb.web.app.handler.base.suggest;

import java.io.IOException;
import java.util.Map;

import com.sxb.web.app.handler.base.suggest.AbstractSuggestServiceImpl.Area;

public interface SuggestService {

    void indexSuggest() throws IOException ;

    Map<String, Object> searchSuggest(String key,Area area) throws Exception ;

}
