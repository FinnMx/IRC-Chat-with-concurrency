package org.nsd.requests;

import org.json.simple.JSONObject;

public class UnsubscribeRequest extends Request {
    private String channel;

    public UnsubscribeRequest(String userName, String channel){
        set_class(UnsubscribeRequest.class.getSimpleName());
        setIdentity(userName);
        this.channel = channel;
    }

    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("identity", getIdentity());
        obj.put("channel", channel);
        return obj;
    }
}
