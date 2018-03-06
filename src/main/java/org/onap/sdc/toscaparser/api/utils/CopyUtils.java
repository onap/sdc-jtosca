package org.onap.sdc.toscaparser.api.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CopyUtils {

    @SuppressWarnings("unchecked")
	public static Object copyLhmOrAl(Object src) {
    	if(src instanceof LinkedHashMap) {
    		LinkedHashMap<String,Object> dst = new LinkedHashMap<String,Object>();
    		for(Map.Entry<String,Object> me: ((LinkedHashMap<String,Object>)src).entrySet()) {
    			dst.put(me.getKey(),me.getValue());	
    		}
    		return dst;
    	}
    	else if(src instanceof ArrayList) {
    		ArrayList<Object> dst = new ArrayList<Object>();
    		for(Object o: (ArrayList<Object>)src) {
    			dst.add(o);
    		}
    		return dst;
    	}
    	else {
    		return null;
    	}
    }
}
