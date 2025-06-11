package com.chalkdigital.common.logging;

import android.support.annotation.NonNull;
import android.util.Log;

import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CDAdLog {
    public static final String LOGGER_NAMESPACE = "com.cdads";

    private static final String LOGTAG = "CDAds v"+CDAdConstants.CDAdSdkVersion;
    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAMESPACE);
    private static final CDAdLogHandler LOG_HANDLER = new CDAdLogHandler();

    /**
     * Sets up the {@link Logger}, {@link Handler}, and prevents any parent Handlers from being
     * notified to avoid duplicated log messages.
     */
    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        LOG_HANDLER.setLevel(Level.FINE);

        LogManager.getLogManager().addLogger(LOGGER);
        addHandler(LOGGER, LOG_HANDLER);
    }

    private CDAdLog() {}

    public static void c(final String message) {
        CDAdLog.c(null, message, null);
    }

    public static void v(final String message) {
        CDAdLog.v(null, message, null);
    }

    public static void d(final String message) {
        CDAdLog.d(null, message, null);
    }

    public static void i(final String message) {
        CDAdLog.i(null, message, null);
    }

    public static void w(final String message) {
        CDAdLog.w(null, message, null);
    }

    public static void e(final String message) {
        CDAdLog.e(null, message, null);
    }

    public static void c(final String tag, final String message) {
        CDAdLog.c(tag, message, null);
    }

    public static void v(final String tag, final String message) {
        CDAdLog.v(tag, message, null);
    }

    public static void d(final String tag, final String message) {
        CDAdLog.d(tag, message, null);
    }

    public static void i(final String tag, final String message) {
        CDAdLog.i(tag, message, null);
    }

    public static void w(final String tag, final String message) {
        CDAdLog.w(tag, message, null);
    }

    public static void e(final String tag, final String message) {
        CDAdLog.e(tag, message, null);
    }

    public static void c(final String message, final Throwable throwable) {
        LOGGER.log(Level.FINEST, message, throwable);
    }

    public static void v(final String message, final Throwable throwable) {
        LOGGER.log(Level.FINE, message, throwable);
    }

    public static void d(final String message, final Throwable throwable) {
        LOGGER.log(Level.CONFIG, message, throwable);
    }

    public static void i(final String message, final Throwable throwable) {
        LOGGER.log(Level.INFO, message, throwable);
    }

    public static void w(final String message, final Throwable throwable) {
        LOGGER.log(Level.WARNING, message, throwable);
    }

    public static void e(final String message, final Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }

    public static void c(final String tag, final String message, final Throwable throwable) {
        LOGGER.log(Level.FINEST, ((tag!=null)?tag + ": ":"")+ message, throwable);
    }

    public static void v(final String tag, final String message, final Throwable throwable) {
        LOGGER.log(Level.FINE, ((tag!=null)?tag + ": ":"")+ message, throwable);
    }

    public static void d(final String tag, final String message, final Throwable throwable) {
        LOGGER.log(Level.CONFIG, ((tag!=null)?tag + ": ":"")+ message, throwable);
    }

    public static void i(final String tag, final String message, final Throwable throwable) {
        LOGGER.log(Level.INFO, ((tag!=null)?tag + ": ":"")+ message, throwable);
    }

    public static void w(final String tag, final String message, final Throwable throwable) {
        LOGGER.log(Level.WARNING, ((tag!=null)?tag + ": ":"")+ message, throwable);
    }

    public static void e(final String tag, final String message, final Throwable throwable) {
        LOGGER.log(Level.SEVERE, ((tag!=null)?tag + ": ":"")+ message, throwable);
    }

    @VisibleForTesting
    public static void setSdkHandlerLevel(@NonNull final Level level) {
        LOG_HANDLER.setLevel(level);
    }

    @VisibleForTesting
    public static Level getSdkHandlerLevel() {
        return LOG_HANDLER.getLevel();
    }

    /**
     * Adds a {@link Handler} to a {@link Logger} if they are not already associated.
     */
    private static void addHandler(@NonNull final Logger logger,
            @NonNull final Handler handler) {
        final Handler[] currentHandlers = logger.getHandlers();
        for (final Handler currentHandler : currentHandlers) {
            if (currentHandler.equals(handler)) {
                return;
            }
        }
        logger.addHandler(handler);
    }

    private static final class CDAdLogHandler extends Handler {
        private static final Map<Level, Integer> LEVEL_TO_LOG = new HashMap<Level, Integer>(7);

        /*
         * Mapping between Level.* and Log.*:
         * Level.FINEST  => Log.v
         * Level.FINER   => Log.v
         * Level.FINE    => Log.v
         * Level.CONFIG  => Log.d
         * Level.INFO    => Log.i
         * Level.WARNING => Log.w
         * Level.SEVERE  => Log.e
         */
        static {
            LEVEL_TO_LOG.put(Level.FINEST, Log.VERBOSE);
            LEVEL_TO_LOG.put(Level.FINER, Log.VERBOSE);
            LEVEL_TO_LOG.put(Level.FINE, Log.VERBOSE);
            LEVEL_TO_LOG.put(Level.CONFIG, Log.DEBUG);
            LEVEL_TO_LOG.put(Level.INFO, Log.INFO);
            LEVEL_TO_LOG.put(Level.WARNING, Log.WARN);
            LEVEL_TO_LOG.put(Level.SEVERE, Log.ERROR);
        }

        @Override
        @SuppressWarnings({"LogTagMismatch", "WrongConstant"})
        public void publish(final LogRecord logRecord) {
            if (isLoggable(logRecord)) {
                final int priority;
                if (LEVEL_TO_LOG.containsKey(logRecord.getLevel())) {
                    priority = LEVEL_TO_LOG.get(logRecord.getLevel());
                } else {
                    priority = Log.VERBOSE;
                }

                String message = logRecord.getMessage() + "\n";

                final Throwable error = logRecord.getThrown();
                if (error != null) {
                    message += Log.getStackTraceString(error);
                }

                Log.println(priority, LOGTAG, message);
            }
        }

        @Override public void close() {}

        @Override public void flush() {}
    }
}
