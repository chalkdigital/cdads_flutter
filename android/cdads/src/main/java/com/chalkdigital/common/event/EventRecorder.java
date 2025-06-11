package com.chalkdigital.common.event;

/**
 * This interface represents a backend to which CDAd client events are logged.
 */
public interface EventRecorder {
    void record(BaseEvent baseEvent);
}
