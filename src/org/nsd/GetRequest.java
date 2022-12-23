package org.nsd;

import org.json.simple.JSONObject;

public class GetRequest extends Request {
    private int after;

    public GetRequest(String userName, int time){
        set_class(GetRequest.class.getSimpleName());
        setIdentity(userName);
        this.after = time;
    }

    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("identity", getIdentity());
        obj.put("aftet", after);
        return obj;
    }
}
