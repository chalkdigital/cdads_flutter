package com.chalkdigital.model;

import java.util.ArrayList;

public class Advertisement {

    private String adStartAt;
    private String adEndAt;
    private String adId;
    private String adName;
    private String adAction;
    private String title;
    private String body;
    private String html;
    private String lp;
    private String size;
    private String image;
    private ArrayList<KeyValue> adTexts;
    private ArrayList<KeyValue> adLinks;

    public String getAdStartAt() {
        return adStartAt;
    }

    public void setAdStartAt(String adStartAt) {
        this.adStartAt = adStartAt;
    }

    public String getAdEndAt() {
        return adEndAt;
    }

    public void setAdEndAt(String adEndAt) {
        this.adEndAt = adEndAt;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public String getAdAction() {
        return adAction;
    }

    public void setAdAction(String adAction) {
        this.adAction = adAction;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getLp() {
        return lp;
    }

    public void setLp(String lp) {
        this.lp = lp;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<KeyValue> getAdTexts() {
        return adTexts;
    }

    public void setAdTexts(ArrayList<KeyValue> adTexts) {
        this.adTexts = adTexts;
    }

    public ArrayList<KeyValue> getAdLinks() {
        return adLinks;
    }

    public void setAdLinks(ArrayList<KeyValue> adLinks) {
        this.adLinks = adLinks;
    }
}
