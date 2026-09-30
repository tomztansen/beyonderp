package com.vaadinerp.scheduler;

import com.vaadinerp.scheduler.JobDispatcher.Dispatch;
import com.vaadinerp.scheduler.JobDispatcher.Job;
import com.vaadinerp.scheduler.JobDispatcher.Outcome;
import com.vaadinerp.service.ScriptExecutorService;
import jakarta.annotation.PreDestroy;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Ticker job Groovy terjadwal. Satu query kecil per menit; job yang jatuh tempo diantre ke
 * {@link JobDispatcher}. Hanya untuk SATU instance aplikasi. sys_scheduled_job diakses lewat
 * JdbcTemplate (bukan entity JPA) supaya aplikasi tetap start bila tabelnya belum dibuat.
 */
@Service
public class ScheduledJobService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobService.class);
    static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final long WATCHDOG_MS = 5 * 60_000L;
    private static final long DB_ERROR_LOG_INTERVAL_MS = 60 * 60_000L;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ScriptExecutorService scripts;
    private final JobDispatcher dispatcher;
    private final boolean enabled;

    /** Diisi saat start: job yang jadwalnya terlewat sebelum ini tidak dikejar. */
    private volatile ZonedDateTime lastTick = ZonedDateTime.now(ZONE);
    private long lastDbErrorLogMs;

    public ScheduledJobService(JdbcTemplate jdbc, PlatformTransactionManager txManager,
            ScriptExecutorService scripts,
            @Value("${app.scheduler.threads:3}") int threads,
            @Value("${app.scheduler.enabled:true}") boolean enabled) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(txManager);
        this.scripts = scripts;
        this.enabled = enabled;
        this.dispatcher = new JobDispatcher(threads, 50, this::execute, this::record, System::currentTimeMillis);
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Jakarta")
    public void tick() {
        if (!enabled) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime from = lastTick;
        lastTick = now;
        dispatcher.expire(WATCHDOG_MS);

        List<Map<String, Object>> rows;
        try {
            rows = jdbc.queryForList("SELECT id, job_code, schedule, script, next_run_at "
                    + "FROM public.sys_scheduled_job WHERE enabled");
        } catch (DataAccessException e) {
            long nowMs = System.currentTimeMillis();
            if (nowMs - lastDbErrorLogMs > DB_ERROR_LOG_INTERVAL_MS) {
                lastDbErrorLogMs = nowMs;
                log.warn("Scheduler tidak bisa membaca sys_scheduled_job (dicatat sekali per jam): {}",
                        e.getMostSpecificCause().getMessage());
            }
            return;
        }
        for (Map<String, Object> row : rows) {
            try {
                handle(row, from, now);
            } catch (Exception e) {
                log.warn("Scheduler: gagal memproses job {}: {}", row.get("job_code"), e.toString());
            }
        }
    }

    private void handle(Map<String, Object> row, ZonedDateTime from, ZonedDateTime now) {
        long id = ((Number) row.get("id")).longValue();
        String code = (String) row.get("job_code");
        CronExpression cron;
        try {
            cron = CronExpression.parse(String.valueOf(row.get("schedule")).trim());
        } catch (IllegalArgumentException e) {
            // Hanya menulis bila pesannya berubah, supaya tidak ada UPDATE tiap menit.
            String msg = "Invalid schedule: " + e.getMessage();
            jdbc.update("UPDATE public.sys_scheduled_job SET last_status = 'FAILED', last_error = ? "
                    + "WHERE id = ? AND last_error IS DISTINCT FROM ?", msg, id, msg);
            return;
        }
        ZonedDateTime next = cron.next(now);
        Timestamp nextTs = next != null ? Timestamp.valueOf(next.toLocalDateTime()) : null;
        if (!Objects.equals(nextTs, row.get("next_run_at"))) {
            jdbc.update("UPDATE public.sys_scheduled_job SET next_run_at = ? WHERE id = ?", nextTs, id);
        }
        if (DueRule.isDue(cron, from, now)) {
            Job job = new Job(id, code, (String) row.get("script"));
            Dispatch d = dispatcher.dispatch(job);
            if (d != Dispatch.QUEUED) {
                record(job, Outcome.skipped(d == Dispatch.ALREADY_RUNNING
                        ? "Skipped: previous run still in progress" : "Skipped: job queue is full"));
            }
        }
    }

    /**
     * Dipanggil tombol Run now. Melewati jalur yang sama dengan ticker (guard, pool, transaksi),
     * tidak menggeser lastTick maupun next_run_at.
     */
    public Dispatch runNow(String jobCode) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, job_code, script FROM public.sys_scheduled_job WHERE job_code = ?", jobCode);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Job \"" + jobCode + "\" not found. Save the job first.");
        }
        Map<String, Object> r = rows.get(0);
        return dispatcher.dispatch(new Job(((Number) r.get("id")).longValue(), (String) r.get("job_code"),
                (String) r.get("script")));
    }

    /** Satu transaksi per job: sukses -> commit, exception apa pun -> rollback seluruh tulisan job. */
    private Outcome execute(Job job) {
        long t0 = System.currentTimeMillis();
        try {
            tx.executeWithoutResult(status -> {
                jdbc.execute("SET LOCAL statement_timeout = '" + ScriptExecutorService.JOB_TIMEOUT_SECONDS + "s'");
                scripts.executeScheduledJobScript(job.code(), job.script(), log);
            });
            return Outcome.ok(System.currentTimeMillis() - t0);
        } catch (Throwable t) {
            return Outcome.failed(t, System.currentTimeMillis() - t0);
        }
    }

    /**
     * Tidak menyentuh version/updatedt/updateby: kolom itu milik form, dan mengubahnya membuat user
     * yang sedang mengedit job mendapat "Data conflict" hanya karena job berjalan di latar.
     */
    private void record(Job job, Outcome out) {
        if ("SKIPPED".equals(out.status())) {
            jdbc.update("UPDATE public.sys_scheduled_job SET last_status = 'SKIPPED', last_error = ? WHERE id = ?",
                    out.error(), job.id());
            return;
        }
        jdbc.update("UPDATE public.sys_scheduled_job SET last_run_at = ?, last_status = ?, "
                + "last_duration_ms = ?, last_error = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now(ZONE)), out.status(), out.durationMs(), out.error(), job.id());
    }

    @PreDestroy
    void shutdown() {
        dispatcher.shutdown();
    }
}
