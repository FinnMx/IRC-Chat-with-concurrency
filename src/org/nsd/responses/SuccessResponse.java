package org.nsd.responses;

import org.json.simple.JSONObject;

public class SuccessResponse extends Response {

    public SuccessResponse(){
        set_class(SuccessResponse.class.getSimpleName());
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        return obj;
    }
}
