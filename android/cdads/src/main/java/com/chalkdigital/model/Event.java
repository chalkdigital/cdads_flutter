package com.chalkdigital.model;

public class Event {
    private EventData eventData;
    private int timeout;
    private String id;

    public EventData getEventData() {
        return eventData;
    }

    public void setEventData(EventData eventData) {
        eventData = eventData;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
