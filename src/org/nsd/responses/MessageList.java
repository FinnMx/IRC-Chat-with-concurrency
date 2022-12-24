package org.nsd.responses;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.nsd.requests.Message;

public class MessageList extends Response{
    private JSONArray messageList;

    public MessageList(JSONArray messsages){
        set_class(MessageList.class.getSimpleName());
        this.messageList = messsages;
    }

    public Object toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("_class", get_class());
        obj.put("messages", messageList);
        return obj;
    }
}
