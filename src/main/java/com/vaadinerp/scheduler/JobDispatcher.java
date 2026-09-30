package com.vaadinerp.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pelaksana job: pool tetap + antrean terbatas + guard "job ini sedang jalan/antre" + watchdog.
 * Tidak tahu apa-apa soal database atau Spring; pekerjaan sebenarnya dan pencatatan hasil
 * disuntik lewat {@code body} dan {@code recorder}, sehingga isolasi error dan kebersihan
 * state bisa diuji tanpa context.
 */
public final class JobDispatcher {

    private static final Logger log = LoggerFactory.getLogger(JobDispatcher.class);

    public record Job(long id, String code, String script) {
    }

    public record Outcome(String status, String error, long durationMs) {
        private static final int MAX_ERROR = 1000;

        public static Outcome ok(long ms) {
            return new Outcome("OK", null, ms);
        }

        public static Outcome skipped(String reason) {
            return new Outcome("SKIPPED", cut(reason), 0);
        }

        public static Outcome failed(String message, long ms) {
            return new Outcome("FAILED", cut(message), ms);
        }

        /** Pesan penyebab asli (Groovy membungkus exception berlapis), bukan pembungkusnya. */
        public static Outcome failed(Throwable t, long ms) {
            Throwable root = t;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String msg = root.getMessage();
            return failed(msg != null && !msg.isBlank() ? msg : root.getClass().getSimpleName(), ms);
        }

        private static String cut(String s) {
            return s != null && s.length() > MAX_ERROR ? s.substring(0, MAX_ERROR) : s;
        }
    }

    public enum Dispatch { QUEUED, ALREADY_RUNNING, QUEUE_FULL }

    private static final class Run {
        final Job job;
        volatile long startedAt; // 0 = masih antre, belum dimulai
        final AtomicBoolean settled = new AtomicBoolean();

        Run(Job job) {
            this.job = job;
        }
    }

    private final Map<Long, Run> running = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor pool;
    private final Function<Job, Outcome> body;
    private final BiConsumer<Job, Outcome> recorder;
    private final LongSupplier clockMs;

    public JobDispatcher(int threads, int queueCapacity, Function<Job, Outcome> body,
            BiConsumer<Job, Outcome> recorder, LongSupplier clockMs) {
        this.body = body;
        this.recorder = recorder;
        this.clockMs = clockMs;
        AtomicInteger seq = new AtomicInteger();
        this.pool = new ThreadPoolExecutor(threads, threads, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity), r -> {
                    Thread t = new Thread(r, "scheduled-job-" + seq.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                });
        this.pool.allowCoreThreadTimeOut(true);
    }

    public Dispatch dispatch(Job job) {
        Run run = new Run(job);
        if (running.putIfAbsent(job.id(), run) != null) {
            return Dispatch.ALREADY_RUNNING;
        }
        try {
            pool.execute(() -> execute(run));
        } catch (RejectedExecutionException e) {
            running.remove(job.id(), run);
            return Dispatch.QUEUE_FULL;
        }
        return Dispatch.QUEUED;
    }

    private void execute(Run run) {
        run.startedAt = clockMs.getAsLong();
        Outcome out;
        try {
            out = body.apply(run.job);
        } catch (Throwable t) {
            out = Outcome.failed(t, clockMs.getAsLong() - run.startedAt);
        }
        try {
            // Siapa yang lebih dulu (selesai atau watchdog) yang mencatat; yang kalah diam.
            if (run.settled.compareAndSet(false, true)) {
                recorder.accept(run.job, out);
            }
        } catch (Throwable t) {
            log.warn("Gagal mencatat hasil job {}: {}", run.job.code(), t.toString());
        } finally {
            running.remove(run.job.id(), run);
        }
    }

    /**
     * Tandai FAILED job yang sudah berjalan lebih lama dari {@code maxRunMs} dan lepas slotnya.
     * Job yang masih antre (belum dimulai) tidak dihitung.
     */
    public List<Long> expire(long maxRunMs) {
        List<Long> expired = new ArrayList<>();
        long now = clockMs.getAsLong();
        for (Run run : running.values()) {
            if (run.startedAt > 0 && now - run.startedAt > maxRunMs && run.settled.compareAndSet(false, true)) {
                running.remove(run.job.id(), run);
                expired.add(run.job.id());
                try {
                    recorder.accept(run.job, Outcome.failed("Timed out after " + (maxRunMs / 60_000) + " minutes",
                            now - run.startedAt));
                } catch (Throwable t) {
                    log.warn("Gagal mencatat timeout job {}: {}", run.job.code(), t.toString());
                }
            }
        }
        return expired;
    }

    public int runningCount() {
        return running.size();
    }

    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
