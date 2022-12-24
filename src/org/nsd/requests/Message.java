package org.nsd.requests;

import org.json.simple.JSONObject;

public class Message extends Request {
    private String from;
    private int when;
    private String body;

    public Message(String userFrom, String message){
        set_class(Message.class.getSimpleName());
        this.from = userFrom;
        this.when = 0;
        this.body = message;

    }

    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("from", from);
        obj.put("when", when);
        obj.put("body", body);

        return obj;
    }
}
