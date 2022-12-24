package org.nsd.requests;
import org.json.simple.*;

public abstract class Request implements JSONAware {
    private String _class;
    private String identity;

    public void set_class(String _class){
        this._class = _class;
    }

    public void setIdentity(String identity){
        this.identity = identity;
    }

    public String get_class(){
        return _class;
    }

    public String getIdentity(){
        return identity;
    }

    public abstract Object toJSON();

    public String toJSONString() {
        return toJSON().toString();
    }

}
