package de.omegazirkel.risingworld.gps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.omegazirkel.risingworld.tools.bridge.WalletBridge.WalletCallResult;
import net.risingworld.api.utils.Vector3f;

public class MarkerPurchaseTest {
    private static final WalletCallResult SUCCESS = new WalletCallResult(true, "");
    private static final WalletCallResult UNKNOWN = new WalletCallResult(false, "");
    private static final MarkerPurchase.Status REVIEW = MarkerPurchase.Status.REVIEW_REQUIRED;

    @Test
    public void failedInsertRefundsConfirmedDebitOnceAndKeepsExistingMarker() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            GPSDatabase database = GPSDatabase.createForTesting(connection);
            database.saveMarker(marker());
            rejectMarkers(connection);
            Marker rejected = marker();
            List<String> debit = new ArrayList<>();
            List<String> refund = new ArrayList<>();
            var result = MarkerPurchase.create(database, rejected, 20, "COIN", id -> {
                debit.add(id);
                return SUCCESS;
            }, id -> {
                refund.add(id);
                return SUCCESS;
            });
            assertEquals(MarkerPurchase.Status.SAVE_REFUNDED, result.status());
            assertEquals(1, debit.size());
            assertEquals(debit, refund);
            assertNull(rejected.getId());
            assertEquals("1", value(connection, "SELECT COUNT(*) FROM marker"));
            assertEquals("0", value(connection, "SELECT COUNT(*) FROM gps_marker_purchase"));
        }
    }

    @Test
    public void unconfirmedDebitSurvivesReopenAndBlocksAutomaticReplay() throws Exception {
        var file = Files.createTempFile("gps-recovery-", ".sqlite");
        try {
            try (var connection = DriverManager.getConnection("jdbc:sqlite:" + file)) {
                var database = GPSDatabase.createForTesting(connection);
                assertEquals(REVIEW, MarkerPurchase.create(database, marker(), 20, "COIN", id -> UNKNOWN,
                        id -> unexpected()).status());
                assertEquals("DEBIT_ATTEMPTED", value(connection, "SELECT stage FROM gps_marker_purchase"));
            }
            try (var connection = DriverManager.getConnection("jdbc:sqlite:" + file)) {
                var reopened = GPSDatabase.createForTesting(connection);
                assertEquals(REVIEW, MarkerPurchase.create(reopened, marker(), 20, "COIN", id -> unexpected(),
                        id -> unexpected()).status());
                assertEquals("0", value(connection, "SELECT COUNT(*) FROM marker"));
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void unconfirmedRefundRemainsPendingAndPreventsAnotherDebit() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            var database = GPSDatabase.createForTesting(connection);
            rejectMarkers(connection);
            assertEquals(REVIEW, MarkerPurchase.create(database, marker(), 20, "COIN", id -> SUCCESS,
                    id -> UNKNOWN).status());
            assertEquals("REFUND_ATTEMPTED", value(connection, "SELECT stage FROM gps_marker_purchase"));
            assertEquals(REVIEW, MarkerPurchase.create(database, marker(), 20, "COIN", id -> unexpected(),
                    id -> unexpected()).status());
        }
    }

    @Test
    public void failedSqliteFinalizationRollsBackMarkerAndPreservesConfirmedDebit() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            var database = GPSDatabase.createForTesting(connection);
            try (var statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TRIGGER reject_purchase BEFORE DELETE ON gps_marker_purchase
                        BEGIN SELECT RAISE(FAIL, 'injected finalization failure'); END
                        """);
            }
            Marker marker = marker();
            assertEquals(REVIEW, MarkerPurchase.create(database, marker, 20, "COIN", id -> SUCCESS,
                    id -> unexpected()).status());
            assertNull(marker.getId());
            assertEquals("0", value(connection, "SELECT COUNT(*) FROM marker"));
            assertEquals("DEBIT_CONFIRMED", value(connection, "SELECT stage FROM gps_marker_purchase"));
        }
    }

    @Test
    public void successfulPurchaseCommitsMarkerAndClearsPendingState() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            var database = GPSDatabase.createForTesting(connection);
            Marker marker = marker();
            assertEquals(MarkerPurchase.Status.CREATED,
                    MarkerPurchase.create(database, marker, 20, "COIN", id -> SUCCESS, id -> unexpected()).status());
            assertNotNull(marker.getId());
            assertEquals("1", value(connection, "SELECT COUNT(*) FROM marker"));
            assertEquals("0", value(connection, "SELECT COUNT(*) FROM gps_marker_purchase"));
        }
    }

    private static void rejectMarkers(Connection connection) throws Exception {
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE TRIGGER reject_marker BEFORE INSERT ON marker BEGIN SELECT RAISE(FAIL, 'injected'); END");
        }
    }

    private static String value(Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            if (!rows.next()) throw new AssertionError("Expected result for " + sql);
            return rows.getString(1);
        }
    }

    private static WalletCallResult unexpected() {
        fail("Unexpected Wallet operation");
        return UNKNOWN;
    }

    private static Marker marker() {
        return new Marker(42, MarkerType.PRIVATE, null, new Vector3f(1, 2, 3), "marker", "marker", 0, 0);
    }
}
