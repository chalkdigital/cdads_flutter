package com.chalkdigital.common;

import java.util.HashMap;

public class CDAdRequestError extends Error {

    private HashMap<String, Object > params;

    /**
     * Create CDAdRequestError with params
     * @param params new CDAdRequestError instance
     */
    public CDAdRequestError(HashMap<String, Object> params) {
        this.params = params;
    }


    /**
     * Get Localized message
     * @return localized description of error
     */
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

}
