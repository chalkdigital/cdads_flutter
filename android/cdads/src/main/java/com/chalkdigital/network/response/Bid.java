package com.chalkdigital.network.response;

import java.util.Map;

public class Bid {
    private String adm;
    private float w;
    private  float h;
    private Integer api;
    private PublisherExtension ext;

    public Bid(Map<String, Object> map) {
        setAdm(map.get("adm"));
        setApi(map.get("api"));
        setH(map.get("h"));
        setW(map.get("w"));
        setExt(map.get("ext"));
    }

    public PublisherExtension getExt() {
        return ext;
    }

    public void setExt(final Object ext) {
        if (ext!=null && ext instanceof Map)
            this.ext = new PublisherExtension((Map<String, Object>) ext);
        else this.ext = null;
    }

    public String getAdm() {
        return adm;
    }

    public void setAdm(Object adm) {
        this.adm = TypeParser.parseString(adm, "");
    }

    public float getW() {
        return w;
    }

    public void setW(Object w) {
        this.w = TypeParser.parseFloat(w, 0.0f);
    }

    public float getH() {
        return h;
    }

    public void setH(Object h) {
        this.h = TypeParser.parseFloat(h, 0.0f);
    }

    public Integer getApi() {
        return api;
    }

    public void setApi(Object api) {
        this.api = TypeParser.parseInteger(api, -1);
    }
}
