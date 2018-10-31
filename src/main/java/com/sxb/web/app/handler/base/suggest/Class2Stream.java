package com.sxb.web.app.handler.base.suggest;

import java.io.InputStream;

import org.ansj.dic.PathToStream;
import org.ansj.exception.LibraryException;

public class Class2Stream extends PathToStream{
    
    @Override
    public InputStream toStream(String path) {
        try {
            path = path.split("\\|")[1];
            path = "com/sxb/web/app/handler/base/suggest/" + path;
            
            InputStream resourceAsStream = null;
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            resourceAsStream = contextClassLoader.getResourceAsStream(path);
            if(resourceAsStream == null){
                resourceAsStream = Class2Stream.class.getResourceAsStream(path);
            }
            return resourceAsStream;
        } catch (Exception e) {
            throw new LibraryException("err to load by class " + path + " message : " + e.getMessage());
        }
    }

}
