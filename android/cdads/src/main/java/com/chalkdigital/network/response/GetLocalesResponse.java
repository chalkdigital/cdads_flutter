package com.chalkdigital.network.response;

import com.chalkdigital.model.CDLocale;

public class GetLocalesResponse extends SSBBaseResponse {

    private CDLocale[] locales;

    public CDLocale[] getLocales() {
        return locales;
    }

    public void setLocales(CDLocale[] locales) {
        this.locales = locales;
    }
}
