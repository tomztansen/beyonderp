package com.vaadinerp.config;

import ch.qos.logback.core.spi.FilterReply;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientAbortLogFilterTest {

    private final ClientAbortLogFilter filter = new ClientAbortLogFilter();

    private FilterReply decide(Throwable t, Object... params) {
        return filter.decide(null, null, null, "msg", params.length == 0 ? null : params, t);
    }

    @Test
    void deniesDirectClientAbort() {
        assertThat(decide(new ClientAbortException("aborted"))).isEqualTo(FilterReply.DENY);
    }

    @Test
    void deniesWhenClientAbortIsNestedInCauseChain() {
        Throwable wrapped = new RuntimeException("flush failed",
                new java.io.IOException("boom", new ClientAbortException("aborted")));
        assertThat(decide(wrapped)).isEqualTo(FilterReply.DENY);
    }

    @Test
    void deniesWhenThrowablePassedAsTrailingArgument() {
        assertThat(decide(null, "arg", new ClientAbortException("aborted"))).isEqualTo(FilterReply.DENY);
    }

    @Test
    void passesUnrelatedException() {
        assertThat(decide(new IllegalStateException("real error"))).isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    void passesWhenNoThrowable() {
        assertThat(decide(null)).isEqualTo(FilterReply.NEUTRAL);
    }
}
