package com.vaadinerp.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

/**
 * Drops log events whose throwable chain contains a {@link org.apache.catalina.connector.ClientAbortException}.
 *
 * <p>The Stimulsoft web viewer servlet logs these at ERROR every time the browser cancels
 * a request mid-write (scroll, zoom, navigate away, reload the viewer iframe). It is benign
 * — the client just went away — and comes from third-party code we cannot patch, so it is
 * suppressed here at the logging layer. Mirrors {@code GlobalErrorHandler.isClientAbort},
 * which already ignores the same exception on the Vaadin path. Only client-abort events are
 * denied; every other log passes through untouched.
 */
public class ClientAbortLogFilter extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level,
                              String format, Object[] params, Throwable t) {
        if (isClientAbort(t) || isClientAbort(lastThrowable(params))) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }

    /** Walk the full cause chain; guard against cycles; null-safe. */
    private static boolean isClientAbort(Throwable t) {
        for (Throwable c = t; c != null; c = (c.getCause() == c) ? null : c.getCause()) {
            if (c instanceof org.apache.catalina.connector.ClientAbortException) return true;
        }
        return false;
    }

    /** Some callers pass the throwable as the trailing log argument rather than a dedicated slot. */
    private static Throwable lastThrowable(Object[] params) {
        if (params != null && params.length > 0 && params[params.length - 1] instanceof Throwable th) {
            return th;
        }
        return null;
    }
}
