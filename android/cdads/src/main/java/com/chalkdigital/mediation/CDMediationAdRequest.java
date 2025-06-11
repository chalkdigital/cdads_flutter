package com.chalkdigital.mediation;

import android.location.Location;

import java.util.Date;
import java.util.Set;

public interface CDMediationAdRequest {

    String getAge();

    String getGender();

    String getEducation();

    String getLanguage();

    int income();

    String getKeywords();

    Location getLocation();

    boolean isTesting();
}