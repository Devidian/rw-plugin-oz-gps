package de.omegazirkel.risingworld.gps;

import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

import de.omegazirkel.risingworld.GPS;
import de.omegazirkel.risingworld.tools.bridge.WalletBridge.WalletCallResult;

/** Coordinates a paid marker write without replaying an unconfirmed legacy Wallet operation. */
final class MarkerPurchase {
    enum Status { CREATED, DEBIT_REJECTED, SAVE_REFUNDED, REVIEW_REQUIRED }

    record Result(Status status, String message) {}

    private MarkerPurchase() {}

    static Result create(GPSDatabase database, Marker marker, long amount, String currency,
            Function<String, WalletCallResult> debit, Function<String, WalletCallResult> refund) {
        String operation = UUID.randomUUID().toString();
        try {
            if (!database.beginMarkerPurchase(operation, marker, amount, currency)) {
                return new Result(Status.REVIEW_REQUIRED, "");
            }
            WalletCallResult charge = debit.apply(operation);
            if (!charge.success()) {
                if ("Wallet balance is too low for this withdrawal.".equals(charge.message())
                        || "Currency is not registered.".equals(charge.message())) {
                    database.finishMarkerPurchase(operation);
                    return new Result(Status.DEBIT_REJECTED, charge.message());
                }
                return new Result(Status.REVIEW_REQUIRED, charge.message());
            }
            database.recordMarkerPurchaseStage(operation, "DEBIT_CONFIRMED");
            if (database.commitPurchasedMarker(operation, marker)) {
                return new Result(Status.CREATED, "");
            }
            // commitPurchasedMarker returns false only after a confirmed SQLite rollback.
            database.recordMarkerPurchaseStage(operation, "REFUND_ATTEMPTED");
            if (!refund.apply(operation).success()) {
                return new Result(Status.REVIEW_REQUIRED, "");
            }
            database.finishMarkerPurchase(operation);
            return new Result(Status.SAVE_REFUNDED, "");
        } catch (SQLException | RuntimeException ex) {
            GPS.logger().error("GPS marker purchase " + operation + " needs review: " + ex.getMessage());
            return new Result(Status.REVIEW_REQUIRED, "");
        }
    }
}
