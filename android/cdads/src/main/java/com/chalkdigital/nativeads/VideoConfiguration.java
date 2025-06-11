package com.chalkdigital.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;

import com.chalkdigital.common.DataKeys;

import java.util.HashMap;

public class VideoConfiguration{


    public enum CDAdImpressionType{
        CDAdImpressionTypeALL(0),
        CDAdImpressionTypeLinear(1),
        CDAdImpressionTypeNonLinear(2);

        private  Integer value;
        private CDAdImpressionType(Integer value){
            this.value = value;
        }

        public static Integer getValue(VideoConfiguration.CDAdImpressionType cdAdImpressionType){
            return cdAdImpressionType.value;
        }
    }

    private Integer minDuration;
    private Integer maxDuration;
    private Integer startDelay;
    private Integer linearity;
    private Integer skip;
    private Integer skipMin;
    private Integer skipAfter;
    private Integer minBitrate;
    private Integer maxBitrate;

    public final static class Builder{
        private Integer minDuration;
        private Integer maxDuration;
        private Integer startDelay;
        private CDAdImpressionType linearity;
        private Boolean skip;
        private Integer skipMin;
        private Integer skipAfter;
        private Integer minBitrate;
        private Integer maxBitrate;

        public Builder() {
            super();
            minDuration = 1;
            maxDuration = 36000;
            startDelay = 0;
            linearity = CDAdImpressionType.CDAdImpressionTypeALL;
            skip = false;
            skipMin = 10;
            skipAfter = 10;
            minBitrate = 0;
            maxBitrate = 10000;


        }

        /**
         * Set minimum duration in seconds
         * @param minDuration integer value seconds, default value is 1
         */
        public void setMinDuration(final Integer minDuration) {
            this.minDuration = minDuration;
        }

        /**
         * Set maximum duration in seconds
         * @param maxDuration integer value seconds, default value is 36000
         */
        public void setMaxDuration(final Integer maxDuration) {
            this.maxDuration = maxDuration;
        }

        /**
         * Set start delay duration in seconds
         * @param startDelay integer value seconds, default value is 0
         */
        public void setStartDelay(final Integer startDelay) {
            this.startDelay = startDelay;
        }

        /**
         * Set video linearity
         * @param linearity set from enum CDAdImpressionType for all, linear  or non linear
         */
        public void setLinearity(final CDAdImpressionType linearity) {
            this.linearity = linearity;
        }

        /**
         * Set skip mode
         * @param skip boolean value to enable skip. Pass true to enable skip, its default value is false.
         */
        public void setSkip(final Boolean skip) {
            this.skip = skip;
        }

        /**
         * Set minimum duration in seconds after which skip would be enabled
         * @param skipMin integer value seconds, default value is 10
         */
        public void setSkipMin(final Integer skipMin) {
            this.skipMin = skipMin;
        }

        /**
         * Set skip duration after which skip would be enabled
         * @param skipAfter integer value seconds, default value is 10
         */
        public void setSkipAfter(final Integer skipAfter) {
            this.skipAfter = skipAfter;
        }

        /**
         * Set minimum bitrate of this video ad
         * @param minBitrate integer value seconds, default value is 0
         */
        public void setMinBitrate(final Integer minBitrate) {
            this.minBitrate = minBitrate;
        }

        /**
         * Set maximim bitrate for this video ad
         * @param maxBitrate integer value seconds, default value is 10000
         */
        public void setMaxBitrate(final Integer maxBitrate) {
            this.maxBitrate = maxBitrate;
        }

        @NonNull
        public final VideoConfiguration build(Context context) {
            return new VideoConfiguration(this, context);
        }
    }


    private VideoConfiguration(@NonNull Builder builder, Context context) {
        minDuration = builder.minDuration;
        maxDuration = builder.maxDuration;
        startDelay = builder.startDelay;
        linearity = builder.linearity.value;
        skip = (builder.skip == null || builder.skip)?1:0;
        skipMin = builder.skipMin;
        skipAfter = builder.skipAfter;
        minBitrate = builder.minBitrate;
        maxBitrate = builder.maxBitrate;
    }

    public HashMap<String, Object> getParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DataKeys.MIN_BITRATE, minBitrate);
        params.put(DataKeys.MIN_DURATION, minDuration);
        params.put(DataKeys.SKIP_MIN, skipMin);
        params.put(DataKeys.START_DELAY, startDelay);
        params.put(DataKeys.LINEARITY, linearity);
        params.put(DataKeys.SKIP, skip);
        params.put(DataKeys.SKIP_AFTER, skipAfter);
        params.put(DataKeys.MAX_BITRATE, maxBitrate);
        params.put(DataKeys.MAX_DURATION, maxDuration);
        return params;

    }

    /**
     * Get minimum duration
     * @return integer value in seconds
     */
    public Integer getMinDuration() {
        return minDuration;
    }

    /**
     * Get maximum duration
     * @return integer value in seconds
     */
    public Integer getMaxDuration() {
        return maxDuration;
    }

    /**
     * Get video start delay
     * @return integer value in seconds
     */
    public Integer getStartDelay() {
        return startDelay;
    }

    /**
     * Get linearity
     * @return integer value 0 for all, 1 for linear and 2 for non linear
     */
    public Integer getLinearity() {
        return linearity;
    }

    /**
     * Get skip
     * @return integer value 1 for skip enabled, 0 for skip disabled
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Get skip minimum
     * @return integer value in seconds
     */
    public Integer getSkipMin() {
        return skipMin;
    }

    /**
     * Get skip after
     * @return integer value in seconds
     */
    public Integer getSkipAfter() {
        return skipAfter;
    }

    /**
     * Get minimum bitrate required for video ad
     * @return integer value
     */
    public Integer getMinBitrate() {
        return minBitrate;
    }

    /**
     * Get maximum bitrate
     * @return integer value for maximum bitrate required for video ad
     */
    public Integer getMaxBitrate() {
        return maxBitrate;
    }
}
