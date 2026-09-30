package com.vaadinerp.scheduler;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.scheduling.support.CronExpression;

/**
 * Jembatan antara input jadwal visual dan cron 6 field Spring. Murni (tanpa Spring context,
 * tanpa UI) supaya bisa diuji langsung. Pola bergambar hanya dikenali bila cron-nya PERSIS
 * sama dengan yang dibangun ulang dari nilai visual; selain itu jatuh ke ADVANCED dengan cron
 * aslinya, jadi tidak ada nilai yang diam-diam berubah saat form dibuka lalu disimpan.
 */
public final class CronCodec {

    public enum Mode { EVERY_N_MINUTES, DAILY, MONTHLY, ADVANCED }

    public static final List<Integer> INTERVALS = List.of(1, 5, 10, 15, 30);

    /** dayOfMonth 0 = hari terakhir bulan. */
    public record Parsed(Mode mode, int intervalMinutes, int hour, int minute, Set<DayOfWeek> days,
            int dayOfMonth, String raw) {
    }

    private static final Pattern EVERY = Pattern.compile("^0 \\*/(\\d+) \\* \\* \\* \\*$");
    private static final Pattern DAILY = Pattern
            .compile("^0 (\\d{1,2}) (\\d{1,2}) \\* \\* (\\*|MON-FRI|[A-Z]{3}(?:,[A-Z]{3})*)$");
    private static final Pattern MONTHLY = Pattern.compile("^0 (\\d{1,2}) (\\d{1,2}) (\\d{1,2}|L) \\* \\*$");
    private static final List<DayOfWeek> WEEK = List.of(DayOfWeek.values());

    private CronCodec() {
    }

    public static String everyNMinutes(int n) {
        if (!INTERVALS.contains(n)) {
            throw new IllegalArgumentException("Interval must be one of " + INTERVALS);
        }
        return "0 */" + n + " * * * *";
    }

    public static String daily(int hour, int minute, Set<DayOfWeek> days) {
        checkTime(hour, minute);
        String dow;
        if (days == null || days.isEmpty() || days.size() == 7) {
            dow = "*";
        } else if (days.equals(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))) {
            dow = "MON-FRI";
        } else {
            dow = WEEK.stream().filter(days::contains).map(CronCodec::abbr).collect(Collectors.joining(","));
        }
        return "0 " + minute + " " + hour + " * * " + dow;
    }

    public static String monthly(int dayOfMonth, int hour, int minute) {
        checkTime(hour, minute);
        if (dayOfMonth < 0 || dayOfMonth > 28) {
            throw new IllegalArgumentException("Day of month must be 1-28, or 0 for the last day");
        }
        return "0 " + minute + " " + hour + " " + (dayOfMonth == 0 ? "L" : String.valueOf(dayOfMonth)) + " * *";
    }

    public static String build(Parsed p) {
        return switch (p.mode()) {
            case EVERY_N_MINUTES -> everyNMinutes(p.intervalMinutes());
            case DAILY -> daily(p.hour(), p.minute(), p.days());
            case MONTHLY -> monthly(p.dayOfMonth(), p.hour(), p.minute());
            case ADVANCED -> p.raw();
        };
    }

    public static Parsed parse(String cron) {
        String raw = cron == null ? "" : cron.trim();
        Parsed p = tryParse(raw);
        if (p != null && raw.equals(safeBuild(p))) {
            return p;
        }
        return new Parsed(Mode.ADVANCED, 0, 0, 0, Set.of(), 0, raw);
    }

    public static Optional<String> validate(String cron) {
        if (cron == null || cron.isBlank()) {
            return Optional.of("Schedule is required");
        }
        String c = cron.trim();
        if (c.split("\\s+").length != 6) {
            return Optional.of("Cron needs 6 fields: second minute hour day-of-month month day-of-week");
        }
        try {
            CronExpression.parse(c);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.of("Invalid cron: " + e.getMessage());
        }
    }

    public static String describe(String cron) {
        Parsed p = parse(cron);
        String time = String.format("%02d:%02d", p.hour(), p.minute());
        return switch (p.mode()) {
            case EVERY_N_MINUTES -> p.intervalMinutes() == 1 ? "Every minute"
                    : "Every " + p.intervalMinutes() + " minutes";
            case DAILY -> {
                if (p.days().isEmpty() || p.days().size() == 7) {
                    yield "Every day at " + time;
                }
                if (p.days().equals(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))) {
                    yield "Weekdays (Mon-Fri) at " + time;
                }
                yield WEEK.stream().filter(p.days()::contains)
                        .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .collect(Collectors.joining(", ")) + " at " + time;
            }
            case MONTHLY -> (p.dayOfMonth() == 0 ? "Last day of every month at " : "Day " + p.dayOfMonth()
                    + " of every month at ") + time;
            case ADVANCED -> "Custom schedule";
        };
    }

    public static List<ZonedDateTime> nextRuns(String cron, ZonedDateTime from, int count) {
        CronExpression ce = CronExpression.parse(cron.trim());
        List<ZonedDateTime> out = new ArrayList<>();
        ZonedDateTime t = from;
        for (int i = 0; i < count; i++) {
            t = ce.next(t);
            if (t == null) {
                break;
            }
            out.add(t);
        }
        return out;
    }

    private static Parsed tryParse(String raw) {
        try {
            Matcher m = EVERY.matcher(raw);
            if (m.matches()) {
                return new Parsed(Mode.EVERY_N_MINUTES, Integer.parseInt(m.group(1)), 0, 0, Set.of(), 0, raw);
            }
            m = DAILY.matcher(raw);
            if (m.matches()) {
                Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
                String dow = m.group(3);
                if ("MON-FRI".equals(dow)) {
                    days.addAll(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
                } else if (!"*".equals(dow)) {
                    for (String a : dow.split(",")) {
                        days.add(fromAbbr(a));
                    }
                }
                return new Parsed(Mode.DAILY, 0, Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)),
                        days, 0, raw);
            }
            m = MONTHLY.matcher(raw);
            if (m.matches()) {
                int dom = "L".equals(m.group(3)) ? 0 : Integer.parseInt(m.group(3));
                return new Parsed(Mode.MONTHLY, 0, Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)),
                        Set.of(), dom, raw);
            }
        } catch (RuntimeException e) {
            // angka/hari tidak valid -> bukan pola bergambar
        }
        return null;
    }

    private static String safeBuild(Parsed p) {
        try {
            return build(p);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void checkTime(int hour, int minute) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Time must be 00:00-23:59");
        }
    }

    private static String abbr(DayOfWeek d) {
        return d.name().substring(0, 3);
    }

    private static DayOfWeek fromAbbr(String a) {
        for (DayOfWeek d : WEEK) {
            if (abbr(d).equals(a)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Unknown day: " + a);
    }
}
