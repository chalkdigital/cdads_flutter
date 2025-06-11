package com.chalkdigital.ads.factories;

import android.content.Context;
import android.support.annotation.NonNull;

import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.mraid.MraidController;
import com.chalkdigital.mraid.PlacementType;

public class MraidControllerFactory {
    protected static MraidControllerFactory instance = new MraidControllerFactory();

    @VisibleForTesting
    public static void setInstance(MraidControllerFactory factory) {
        instance = factory;
    }

    public static MraidController create(@NonNull final Context context,
                                         @NonNull final AdReport adReport,
                                         @NonNull final PlacementType placementType, final String clickAction) {
        return instance.internalCreate(context, adReport, placementType, clickAction);
    }

    protected MraidController internalCreate(@NonNull final Context context, 
            @NonNull final AdReport adReport,
            @NonNull final PlacementType placementType, final String clickAction) {
        return new MraidController(context, adReport, placementType, clickAction);
    }
}
