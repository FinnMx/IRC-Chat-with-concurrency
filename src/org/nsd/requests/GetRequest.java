package org.nsd.requests;

import org.json.simple.JSONObject;

public class GetRequest extends Request {
    private String after;

    public GetRequest(String userName, String time){
        set_class(GetRequest.class.getSimpleName());
        setIdentity(userName);
        this.after = time;
    }

    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("identity", getIdentity());
        obj.put("after", after);
        return obj;
    }
}
