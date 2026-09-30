package com.vaadinerp.scheduler;

import java.time.ZonedDateTime;
import org.springframework.scheduling.support.CronExpression;

/** Aturan jatuh tempo: ada titik jadwal di (lastTick, now]. Job yang terlewat sebelum start tidak dikejar. */
public final class DueRule {
    private DueRule() {
    }

    public static boolean isDue(CronExpression cron, ZonedDateTime lastTick, ZonedDateTime now) {
        ZonedDateTime next = cron.next(lastTick);
        return next != null && !next.isAfter(now);
    }
}
