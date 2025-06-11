package com.chalkdigital.network.response;

import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class PublisherExtension {
    private ConfigurationParameters global;
    private ConfigurationParameters bid;
    private Object mediationpriority;
    private String base_sdk_event_url;

    public PublisherExtension(Map<String, Object> map) {
        setMediationpriority(map.get("mediationpriority"));
        setBase_sdk_event_url(map.get("base_sdk_event_url"));
        setGlobal(map.get("global"));
        setBid(map.get("bid"));
    }

    public ConfigurationParameters getGlobal() {
        return global;
    }

    public void setGlobal(final Object global) {
        if (global instanceof Map)
            this.global = new ConfigurationParameters((Map<String, Object>) global);
        else this.global = null;
    }

    public ConfigurationParameters getBid() {
        return bid;
    }

    public void setBid(final Object bid) {
        if (bid instanceof Map) {
            LinkedTreeMap linkedTreeMap = (LinkedTreeMap)bid;
            if (mediationpriority!=null)
                linkedTreeMap.put("mediationpriority", mediationpriority);
            if (base_sdk_event_url!=null)
                linkedTreeMap.put("base_sdk_event_url", base_sdk_event_url);
            this.bid = new ConfigurationParameters(linkedTreeMap);
        }
        else this.bid = null;
    }

    public void setMediationpriority(Object mediationpriority) {
        this.mediationpriority = mediationpriority;
    }

    public void setBase_sdk_event_url(Object base_sdk_event_url) {
        this.base_sdk_event_url = TypeParser.parseString(base_sdk_event_url, "");
    }
}
