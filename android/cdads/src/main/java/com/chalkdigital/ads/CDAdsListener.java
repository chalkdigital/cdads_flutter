package com.chalkdigital.ads;

import android.location.Location;

/**
 * Created by arungupta on 27/12/16.
 */

public interface CDAdsListener {
    public void cdAdsLocationUpdated(Location location);
    public void cdAdsLocationServicesDidFailWithError(Error error);
}
