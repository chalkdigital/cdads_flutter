package com.chalkdigital.common.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;

public class EventDispatcher {
    private final Iterable<EventRecorder> mEventRecorders;
    private final Looper mLooper;
    private final Handler mMessageHandler;
    private final Handler.Callback mHandlerCallback;

    @VisibleForTesting
    EventDispatcher(Iterable<EventRecorder> recorders, Looper looper) {
        mEventRecorders = recorders;
        mLooper = looper;
        mHandlerCallback = new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                if (msg.obj instanceof BaseEvent) {
                    for (final EventRecorder recorder : mEventRecorders) {
                        recorder.record((BaseEvent) msg.obj);
                    }
                } else {
                    CDAdLog.d("EventDispatcher received non-BaseEvent message type.");
                }
                return true;
            }
        };
        mMessageHandler = new Handler(mLooper, mHandlerCallback);
    }

    public void dispatch(BaseEvent event) {
        Message.obtain(mMessageHandler, 0, event).sendToTarget();
    }

    @VisibleForTesting
    Iterable<EventRecorder> getEventRecorders() {
        return mEventRecorders;
    }

    @VisibleForTesting
    Handler.Callback getHandlerCallback() {
        return mHandlerCallback;
    }
}
