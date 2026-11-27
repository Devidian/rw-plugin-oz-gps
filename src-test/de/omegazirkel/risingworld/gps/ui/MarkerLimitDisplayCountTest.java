package de.omegazirkel.risingworld.gps.ui;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import de.omegazirkel.risingworld.gps.MarkerType;

public class MarkerLimitDisplayCountTest {
    @Test
    public void sixtyIdleTicksReadTwelveCountsAndPickUpOtherViewersChanges() {
        var display = new MarkerLimitDisplayCount();
        var reads = new AtomicInteger();
        var actual = new AtomicInteger(2);
        for (int tick = 0; tick < 60; tick++) {
            if (tick == 1) actual.set(3);
            int shown = display.read(MarkerType.GROUP, "builders", TimeUnit.SECONDS.toNanos(tick), () -> {
                reads.incrementAndGet();
                return actual.get();
            });
            assertEquals(tick < 5 ? 2 : 3, shown);
        }
        assertEquals(12, reads.get());
    }

    @Test
    public void tabGroupAndLocalRefreshAlwaysReadCurrentCount() {
        var display = new MarkerLimitDisplayCount();
        var reads = new AtomicInteger();
        assertEquals(1, display.read(MarkerType.PRIVATE, null, 0, reads::incrementAndGet));
        assertEquals(2, display.read(MarkerType.GROUP, "builders", 1, reads::incrementAndGet));
        assertEquals(3, display.read(MarkerType.GROUP, "visitors", 2, reads::incrementAndGet));
        display.invalidate();
        assertEquals(4, display.read(MarkerType.GROUP, "visitors", 3, reads::incrementAndGet));
    }
}
