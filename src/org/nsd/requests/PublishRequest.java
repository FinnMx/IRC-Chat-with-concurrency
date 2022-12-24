package org.nsd.requests;

import org.json.simple.JSONObject;

public class PublishRequest extends Request {
    private Message message;

    public PublishRequest(String channel, Message message){
        set_class(PublishRequest.class.getSimpleName());
        setIdentity(channel);
        this.message = message;
    }

    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("identity", getIdentity());
        obj.put("message", message.toJSONString());

        return obj;
    }
}
