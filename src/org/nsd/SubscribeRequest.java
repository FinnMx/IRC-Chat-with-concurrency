package org.nsd;

import org.json.simple.JSONObject;

public class SubscribeRequest extends Request{
    private String channel;

    public SubscribeRequest(String userName, String channel){
        set_class(SubscribeRequest.class.getSimpleName());
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
