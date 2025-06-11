package com.chalkdigital.network.response;

import com.chalkdigital.common.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class Event {
    private EventData eventData;
    private int timeout;
    private String id;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(Object object) {
        if (object!=null){
            this.timeout = TypeParser.parseInteger(object, Constants.THIRTY_SECONDS_MILLIS);
        }else{
            this.timeout = Constants.THIRTY_SECONDS_MILLIS;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(Object object) {
        this.id = TypeParser.parseString(object, "");
    }

    public EventData getEventData() {
        return eventData;
    }

    public void setEventData(final Object object) {
        if (object !=null && object instanceof Map){
            Map<String, Object> eventData = (Map<String, Object>)object;
            if (eventData.size()>0)
                this.eventData = new EventData((Map<String, Object>) object);
            else this.eventData = null;
        }else this.eventData = null;
    }

    public Event(Map<String, Object> map) {
        if (map!=null && map.size()>0){
            setEventData(map.get("eventdata"));
            setTimeout(map.get("timeout"));
            setId(map.get("id"));
        }
    }

}
