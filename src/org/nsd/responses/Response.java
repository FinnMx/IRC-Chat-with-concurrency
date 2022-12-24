package org.nsd.responses;
import org.json.simple.*;

public abstract class Response implements JSONAware {
    private String _class;

    public void set_class(String _class){
        this._class = _class;
    }

    public String get_class(){
        return _class;
    }

    public abstract Object toJSON();

    public String toJSONString() {
        return toJSON().toString();
    }
}
