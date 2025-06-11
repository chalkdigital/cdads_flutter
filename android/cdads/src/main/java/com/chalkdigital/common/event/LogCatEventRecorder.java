package com.chalkdigital.common.event;

import com.chalkdigital.common.logging.CDAdLog;

class LogCatEventRecorder implements EventRecorder {
    @Override
    public void record(final BaseEvent baseEvent) {
        CDAdLog.d(baseEvent.toString());
    }
}

