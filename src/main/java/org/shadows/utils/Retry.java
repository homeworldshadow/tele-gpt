package org.shadows.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Retry utils
 *
 * @author bayura-ea
 */
@Slf4j
public final class Retry {

    private Retry() {
    }

    public static void with(Supplier<?> s, int count, Duration timeout) {
        try {
            s.get();
        } catch (Exception e) {
            if (count > 0) {
                log.warn("Left retries {} on exception: {}", count, e.toString());
                try {
                    Thread.sleep(timeout.toMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
                with(s, --count, timeout);
                return;
            }
            log.error("Retries exceeded with error: " + e, e);
        }
    }
}
