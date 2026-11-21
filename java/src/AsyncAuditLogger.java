package src;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Asynchronous Audit Logger demonstrating Java Multithreading.
 * Runs on a dedicated daemon thread to write operational logs to disk
 * without blocking console user interactions.
 * Syllabus Reference: Java Programming Unit 3 (Multithreaded Programming).
 */
public class AsyncAuditLogger implements Runnable {

    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static volatile boolean running = true;
    private static Thread loggerThread;

    static {
        AsyncAuditLogger logger = new AsyncAuditLogger();
        loggerThread = new Thread(logger, "AuditLoggerThread");
        loggerThread.setDaemon(true);
        loggerThread.start();
    }

    public static void log(String action, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = String.format("[%s] [ACTION: %-25s] %s", timestamp, action, details);
        logQueue.offer(entry);
    }

    @Override
    public void run() {
        while (running || !logQueue.isEmpty()) {
            try {
                String entry = logQueue.take();
                try (PrintWriter pw = new PrintWriter(new FileWriter("audit_trail.log", true))) {
                    pw.println(entry);
                } catch (Exception ignored) {
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void stopLogger() {
        running = false;
        if (loggerThread != null) {
            loggerThread.interrupt();
        }
    }
}
