package com.chalkdigital.spark;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.chalkdigital.ads.RepeatingHandlerRunnable;
import com.chalkdigital.common.Preconditions;

/**
 * A runnable that is used to update a {@link SparkVideoViewCountdownRunnable}'s countdown display according
 * to rules contained in the {@link SparkVideoViewController}
 */
public class SparkVideoViewCountdownRunnable extends RepeatingHandlerRunnable {



        @NonNull
        private final SparkVideoViewController mVideoViewController;

        public SparkVideoViewCountdownRunnable(@NonNull SparkVideoViewController videoViewController,
                                              @NonNull Handler handler) {
            super(handler);
            Preconditions.checkNotNull(handler);
            Preconditions.checkNotNull(videoViewController);

            mVideoViewController = videoViewController;
        }

        @Override
        public void doWork() {
//            mVideoViewController.updateCountdown();
//
//            if (mVideoViewController.shouldBeInteractable()) {
//                mVideoViewController.makeVideoInteractable();
//            }
        }


}
