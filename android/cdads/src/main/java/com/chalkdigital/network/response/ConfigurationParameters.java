package com.chalkdigital.network.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationParameters  {

    private Float ver;
    private Boolean closable;
    private Boolean skipable;
    private String skipOffset;
    private Boolean border;
    private Float borderWidth;
    private Integer stretch;
    private Integer clickaction;
    private String borderColor;
    private String vpaidSourceUrl;
    private String orientation;
    private String adPrefix;
    private String adSuffix;
    private ArrayList<Event> mediationpriority;
    private String base_sdk_event_url;

    public ConfigurationParameters() {
        closable = true;
        stretch = 1;
    }

    public ConfigurationParameters(Map<String, Object> map) {
        setVer(map.get("ver"));
        setEvents(map.get("mediationpriority"));
        setBorderColor(map.get("borderColor"));
        setClosable(map.get("closable"));
        setBorder(map.get("border"));
        setBorderWidth(map.get("borderWidth"));
        setStretch(map.get("stretch"));
        setClickaction(map.get("clickaction"));
        setSkipOffset(map.get("skipOffset"));
        setSkipable(map.get("skipable"));
        setAdPrefix(map.get("adPrefix"));
        setAdSuffix(map.get("adSuffix"));
        setvpaidSourceUrl(map.get("vpaidSourceUrl"));
        setOrientation(map.get("orientation"));
        setBaseSdkEventUrl(map.get("base_sdk_event_url"));

    }

    public Float getVer() {
        return ver;
    }

    public void setVer(final Object ver) {
        this.ver = TypeParser.parseFloat(ver, 1.0f);
    }

    public ArrayList<Event> getEvents() {
        if (mediationpriority == null)
            mediationpriority = new ArrayList<Event>();
        return mediationpriority;
    }

    public void setEvents(final Object object) {
        if (object != null && object instanceof ArrayList){
            ArrayList objects = (ArrayList) object;
            int size = objects.size();
            ArrayList<Event> events = new ArrayList<Event>(size);
            for (int i = 0; i< size; i++){
                Object o = objects.get(i);
                if (o instanceof Map){
                    Event event = new Event((Map<String, Object>) o);
                    events.add(event);
                }else events.add(null);;

            }
            this.mediationpriority = events;
        }
    }

    public String getBorderColor() {
//        if (borderColor!=null && !borderColor.equals("") && borderColor.length()>6)
//
//        else return "000000";
        return borderColor;
    }

    public void setBorderColor(final Object color) {
        this.borderColor = TypeParser.parseString(color, "000000");
    }

    public Boolean getClosable() {
        return closable;
    }

    public Boolean getBorder() {
        return border;
    }

    public Float getBorderWidth() {
//        if (borderWidth>0.0f)
//
//        else return 1.0f;
        return borderWidth;
    }

    public Integer getStretch() {
        return stretch;
    }

    public void setClosable(final Object closable) {
        this.closable = TypeParser.parseBoolean(closable, false);
    }

    public void setBorder(final Object showBorder) {
        this.border = TypeParser.parseBoolean(showBorder, false);
    }

    public void setBorderWidth(final Object borderWidth) {
        this.borderWidth = TypeParser.parseFloat(borderWidth, 1.0f);
    }

    public void setStretch(final Object stretch) {
        this.stretch = TypeParser.parseInteger(stretch, 1);
    }

    public Integer getClickaction() {
        return clickaction;
    }

    public void setClickaction(final Object clickaction) {
        this.clickaction = TypeParser.parseInteger(clickaction, 0);
    }

    public Boolean getSkipable() {
        return skipable;
    }

    public void setSkipable(final Object skipable) {
        this.skipable = TypeParser.parseBoolean(skipable, true);
    }

    public String getSkipOffset() {
        return skipOffset;
    }

    public void setSkipOffset(final Object skipOffset) {
        this.skipOffset = TypeParser.parseString(skipOffset, "10%");
    }

    public String getvpaidSourceUrl() {
        return vpaidSourceUrl;
    }

    public void setvpaidSourceUrl(final Object vpaidSourceUrl) {
        this.vpaidSourceUrl = TypeParser.parseString(vpaidSourceUrl, "");
    }

    public void setAdPrefix(final Object  adPrefix) {
        this.adPrefix = TypeParser.parseString(adPrefix, "");
    }

    public void setAdSuffix(final Object  adSuffix) {
        this.adSuffix = TypeParser.parseString(adSuffix, "");
    }

    public String getAdPrefix() {
        return adPrefix;
    }

    public String getAdSuffix() {
        return adSuffix;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(final Object orientation) {
        this.orientation = TypeParser.parseString(orientation, null);
    }

    public void setMediationpriority(ArrayList<Event> priority) {
        this.mediationpriority = priority;
    }

    public String getBase_sdk_event_url() {
        return base_sdk_event_url;
    }

    public void setBaseSdkEventUrl(Object object){
        this.base_sdk_event_url = TypeParser.parseString(object, "");
    }

    public void setBase_sdk_event_url(String base_sdk_event_url) {
        this.base_sdk_event_url = base_sdk_event_url;
    }
}
