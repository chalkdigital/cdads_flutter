package com.chalkdigital.common;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;

import java.math.BigDecimal;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LocationService {
    public enum LocationAwareness {
        NORMAL, TRUNCATED, DISABLED;

        // These deprecated methods are only used to support the deprecated methods
        // CDAdView#setLocationAwareness, CDAdInterstitial#setLocationAwareness
        // and should not be used elsewhere. Unless interacting with those methods, use
        // the type CDAd.LocationAwareness

        @Deprecated
        public CDAdsUtils.LocationAwareness getNewLocationAwareness() {
            if (this == TRUNCATED) {
                return CDAdsUtils.LocationAwareness.TRUNCATED;
            } else if (this == DISABLED) {
                return CDAdsUtils.LocationAwareness.DISABLED;
            } else {
                return CDAdsUtils.LocationAwareness.NORMAL;
            }
        }

        @Deprecated
        public static LocationAwareness
                fromCDAdLocationAwareness(CDAdsUtils.LocationAwareness awareness) {
            if (awareness == CDAdsUtils.LocationAwareness.DISABLED) {
                return DISABLED;
            } else if (awareness == CDAdsUtils.LocationAwareness.TRUNCATED) {
                return TRUNCATED;
            } else {
                return NORMAL;
            }
        }
    }

    private static volatile LocationService sInstance;
    @VisibleForTesting @Nullable Location mLastKnownLocation;
    @VisibleForTesting long mLocationLastUpdatedMillis;

    private LocationService() {
    }

    @VisibleForTesting
    @NonNull
    static LocationService getInstance() {
        LocationService locationService = sInstance;
        if (locationService == null) {
            synchronized (LocationService.class) {
                locationService = sInstance;
                if (locationService == null) {
                    locationService = new LocationService();
                    sInstance = locationService;
                }
            }
        }
        return locationService;
    }

    public enum ValidLocationProvider {
        NETWORK(LocationManager.NETWORK_PROVIDER),
        GPS(LocationManager.GPS_PROVIDER);

        @NonNull final String name;

        ValidLocationProvider(@NonNull final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        private boolean hasRequiredPermissions(@NonNull final Context context) {
            switch (this) {
                case NETWORK:
                    return DeviceUtils.isPermissionGranted(context, ACCESS_FINE_LOCATION)
                            || DeviceUtils.isPermissionGranted(context, ACCESS_COARSE_LOCATION);
                case GPS:
                    return DeviceUtils.isPermissionGranted(context, ACCESS_FINE_LOCATION);
                default:
                    return false;
            }
        }
    }

    /**
     * Returns the last known location of the device using its GPS and network location providers.
     * This only checks Android location providers as often as
     * {@link CDAdsUtils#getMinimumLocationRefreshTime()} says to, in milliseconds.
     * <p>
     * May be {@code null} if:
     * <ul>
     * <li> Location permissions are not requested in the Android manifest file
     * <li> The location providers don't exist
     * <li> Location awareness is disabled in the parent CDAdView
     * </ul>
     */
    @Nullable
    public static Location getLastKnownLocation(@NonNull final Context context) {
        Preconditions.checkNotNull(context);
        final LocationService locationService = getInstance();

        if (isLocationFreshEnough(context)) {
            return locationService.mLastKnownLocation;
        }

        final Location gpsLocation = getLocationFromProvider(context, ValidLocationProvider.GPS);
        final Location networkLocation = getLocationFromProvider(context, ValidLocationProvider.NETWORK);
        final Location result = getMostRecentValidLocation(gpsLocation, networkLocation);

        locationService.mLastKnownLocation = result;
        locationService.mLocationLastUpdatedMillis = SystemClock.elapsedRealtime();
        return result;
    }

    @VisibleForTesting
    @Nullable
    static Location getLocationFromProvider(@NonNull final Context context,
            @NonNull final ValidLocationProvider provider) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(provider);

        if (!provider.hasRequiredPermissions(context)) {
            return null;
        }

        final LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            // noinspection ResourceType
            return locationManager.getLastKnownLocation(provider.toString());
        } catch (SecurityException e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Failed to retrieve location from " +
                    provider.toString() + " provider: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Failed to retrieve location: device has no " +
                    provider.toString() + " location provider.");
        } catch (NullPointerException e) {
                        Utils.logStackTrace(e); // This happens on 4.2.2 on a few Android TV devices
            CDAdLog.d("Failed to retrieve location: device has no " +
                    provider.toString() + " location provider.");
        }catch (Exception e){

        }

        return null;
    }

    @VisibleForTesting
    @Nullable
    static Location getMostRecentValidLocation(@Nullable final Location a, @Nullable final Location b) {
        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        // At this point, locations A and B are non-null, so return the more recent one
        return (a.getTime() > b.getTime()) ? a : b;
    }

    @VisibleForTesting
    static void truncateLocationLatLon(@Nullable final Location location,
            final int precision) {
        if (location == null || precision < 0) {
            return;
        }

        double lat = location.getLatitude();
        double truncatedLat = BigDecimal.valueOf(lat)
                .setScale(precision, BigDecimal.ROUND_HALF_DOWN)
                .doubleValue();
        location.setLatitude(truncatedLat);

        double lon = location.getLongitude();
        double truncatedLon = BigDecimal.valueOf(lon)
                .setScale(precision, BigDecimal.ROUND_HALF_DOWN)
                .doubleValue();
        location.setLongitude(truncatedLon);
    }

    private static boolean isLocationFreshEnough(Context context) {
        final LocationService locationService = LocationService.getInstance();
        if (locationService.mLastKnownLocation == null) {
            return false;
        }
        return SystemClock.elapsedRealtime() - locationService.mLocationLastUpdatedMillis <=
                CDAdLocationManager.getLocationClientInterval(context);
    }

    @Deprecated
    @VisibleForTesting
    public static void clearLastKnownLocation() {
        getInstance().mLastKnownLocation = null;
    }
}
