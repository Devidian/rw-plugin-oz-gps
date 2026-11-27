package de.omegazirkel.risingworld.gps.ui;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

import de.omegazirkel.risingworld.gps.MarkerType;

/** Bounds display queries while still observing writes made from other overlays. */
final class MarkerLimitDisplayCount {
    private static final long REFRESH_NANOS = TimeUnit.SECONDS.toNanos(5);

    private MarkerType type;
    private String group;
    private long readAt;
    private int count;

    int read(MarkerType type, String group, long now, IntSupplier query) {
        if (this.type != type || !Objects.equals(this.group, group) || now - readAt >= REFRESH_NANOS) {
            count = query.getAsInt();
            this.type = type;
            this.group = group;
            readAt = now;
        }
        return count;
    }

    void invalidate() {
        type = null;
    }
}
