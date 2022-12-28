package org.nsd.requests;

import org.json.simple.JSONObject;

public class DefaultRequest extends Request{

    public DefaultRequest(String type){
        set_class(type);
    }

    @Override
    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        return obj;
    }
}
