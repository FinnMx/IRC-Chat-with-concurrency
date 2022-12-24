package org.nsd.responses;

import org.json.simple.JSONObject;

public class ErrorResponse extends Response{
    private String error;

    public ErrorResponse(String error){
        set_class(ErrorResponse.class.getSimpleName());
        this.error = error;
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("error", error);
        return obj;
    }
}
