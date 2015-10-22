package com.leanhippo.root.trackmylocation.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by speriasami on 7/9/2015.
 */
public class RequestParameterPackage {

    private String uri;
    private String method = "GET";
    private Map<String, String> params = new HashMap<String, String>();
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setParam(String key, String value){
        params.put(key, value);
    }

    public String getEncodedParams() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for(String key : params.keySet()){
            String value = params.get(key);
            if(sb.length() > 0){
                sb.append("&");
            }
            sb.append(key + "=" + URLEncoder.encode(value, "UTF-8"));
        }
        return sb.toString();
    }


}
