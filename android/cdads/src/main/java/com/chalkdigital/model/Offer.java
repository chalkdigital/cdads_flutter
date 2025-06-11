package com.chalkdigital.model;

public class Offer {

    private String offerStartAt;
    private String offerEndAt;
    private String offerId;
    private String offerName;
    private String offerAction;
    private KeyValue[] offerTexts;
    private KeyValue[] offerLinks;

    public String getOfferStartAt() {
        return offerStartAt;
    }

    public void setOfferStartAt(String offerStartAt) {
        this.offerStartAt = offerStartAt;
    }

    public String getOfferEndAt() {
        return offerEndAt;
    }

    public void setOfferEndAt(String offerEndAt) {
        this.offerEndAt = offerEndAt;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public String getOfferAction() {
        return offerAction;
    }

    public void setOfferAction(String offerAction) {
        this.offerAction = offerAction;
    }

    public KeyValue[] getOfferTexts() {
        return offerTexts;
    }

    public void setOfferTexts(KeyValue[] offerTexts) {
        this.offerTexts = offerTexts;
    }

    public KeyValue[] getOfferLinks() {
        return offerLinks;
    }

    public void setOfferLinks(KeyValue[] offerLinks) {
        this.offerLinks = offerLinks;
    }
}
