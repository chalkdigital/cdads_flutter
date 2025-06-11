package com.chalkdigital.model;

public class CDLocale {

    private String localeId;
    private String localeName;
    private String localeStrings;
    private String localeDefault;

    public String getLocaleId() {
        return localeId;
    }

    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    public String getLocaleName() {
        return localeName;
    }

    public void setLocaleName(String localeName) {
        this.localeName = localeName;
    }

    public String getLocaleStrings() {
        return localeStrings;
    }

    public void setLocaleStrings(String localeStrings) {
        this.localeStrings = localeStrings;
    }

    public String getLocaleDefault() {
        return localeDefault;
    }

    public void setLocaleDefault(String localeDefault) {
        this.localeDefault = localeDefault;
    }
}
