package com.chalkdigital.network.response;

import com.chalkdigital.model.Offer;

public class GetBeaconOffers extends SSBBaseResponse {

    private Offer[] offers;

    public Offer[] getOffers() {
        return offers;
    }

    public void setOffers(Offer[] offers) {
        this.offers = offers;
    }
}
