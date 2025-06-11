package com.chalkdigital.common.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.logging.CDAdLog;

/**
 * Immutable data class with client event data.
 */
public class Event extends BaseEvent {
    private Event(@NonNull Builder builder) {
        super(builder);
    }

    public static class Builder extends BaseEvent.Builder {
        public Builder(@NonNull Name name, @NonNull Category category, double samplingRate) {
            super(ScribeCategory.EXCHANGE_CLIENT_EVENT, name, category, samplingRate);
        }

        @NonNull
        @Override
        public Event build() {
            return new Event(this);
        }
    }

    /**
     * Creates a BaseEvent from the specified event and the metadata surrounding the event.
     *
     * @param name         Event name: See {@link Name} for
     *                     constants.
     * @param category     Category: See {@link Category} for
     *                     constants.
     * @param samplingRate The percentage of events to sample. See {@link SamplingRate}
     *                     for constants.
     * @param eventDetails Data object containing the remaining meta data around this event.
     * @return An {@link BaseEvent} with all the parts combined, or {@code null} if there is no
     * metadata available.
     */
    @Nullable
    public static BaseEvent createEventFromDetails(@NonNull final Name name,
            @NonNull final Category category,
            @NonNull final SamplingRate samplingRate,
            @Nullable EventDetails eventDetails) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(category);
        Preconditions.checkNotNull(samplingRate);

        if (eventDetails == null) {
            CDAdLog.d("Unable to log event due to no details present");
            return null;
        }

        return new Builder(name,
                category,
                samplingRate.getSamplingRate())
                .withAdUnitId(eventDetails.getAdUnitId())
                .withAdCreativeId(eventDetails.getDspCreativeId())
                .withAdType(eventDetails.getAdType())
                .withAdNetworkType(eventDetails.getAdNetworkType())
                .withAdWidthPx(eventDetails.getAdWidthPx())
                .withAdHeightPx(eventDetails.getAdHeightPx())
                .withGeoLat(eventDetails.getGeoLatitude())
                .withGeoLon(eventDetails.getGeoLongitude())
                .withGeoAccuracy(eventDetails.getGeoAccuracy())
                .withPerformanceDurationMs(eventDetails.getPerformanceDurationMs())
                .withRequestId(eventDetails.getRequestId())
                .withRequestStatusCode(eventDetails.getRequestStatusCode())
                .withRequestUri(eventDetails.getRequestUri())
                .build();
    }
}
