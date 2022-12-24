package org.nsd.requests;

import org.json.simple.JSONObject;

public class OpenRequest extends Request {

    public OpenRequest(String channel){
        set_class(OpenRequest.class.getSimpleName());
        setIdentity(channel);
    }

    public Object toJSON(){
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("identity", getIdentity());

        return obj;
    }

}
