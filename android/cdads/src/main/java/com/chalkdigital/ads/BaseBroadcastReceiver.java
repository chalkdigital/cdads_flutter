package com.chalkdigital.ads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.Preconditions;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    private final long mBroadcastIdentifier;
    @Nullable private Context mContext;

    public BaseBroadcastReceiver(final long broadcastIdentifier) {
        mBroadcastIdentifier = broadcastIdentifier;
    }

    public static void broadcastAction(@NonNull final Context context, final long broadcastIdentifier,
                                       @NonNull final String action) {
        broadcastAction(context, broadcastIdentifier, action, null);
    }

    public static void broadcastAction(@NonNull final Context context, final long broadcastIdentifier,
            @NonNull final String action, @NonNull final String info) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(action, "action cannot be null");
        Intent intent = new Intent(action);
        intent.putExtra(DataKeys.BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        if (info!=null)
            intent.putExtra(DataKeys.BROADCAST_INFO_KEY, info);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    @NonNull
    public abstract IntentFilter getIntentFilter();

    public void register(final @NonNull BroadcastReceiver broadcastReceiver, Context context) {
        mContext = context;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver,
                getIntentFilter());
    }

    public void unregister(final @Nullable BroadcastReceiver broadcastReceiver) {
        if (mContext != null && broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
            mContext = null;
        }
    }

    /**
     * Only consume this broadcast if the identifier on the received Intent and this broadcast
     * match up. This allows us to target broadcasts to the ad that spawned them. We include
     * this here because there is no appropriate IntentFilter condition that can recreate this
     * behavior.
     */
    public boolean shouldConsumeBroadcast(@NonNull final Intent intent) {
        Preconditions.checkNotNull(intent, "intent cannot be null");
        final long receivedIdentifier = intent.getLongExtra(DataKeys.BROADCAST_IDENTIFIER_KEY, -1);
        return mBroadcastIdentifier == receivedIdentifier;
    }
}
