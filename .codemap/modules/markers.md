# Marker access, persistence and map export
<!-- codemap-id: rw-plugin-oz-gps/markers -->

## Responsibility

Marker access, persistence and map export. GPS owns marker records and authorization. The export route exposes a filtered view.

## Source and tests

- [Paid marker creation](../../src/de/omegazirkel/risingworld/gps/MarkerPurchase.java) journals Wallet intent. [Recovery tests](../../src-test/de/omegazirkel/risingworld/gps/MarkerPurchaseTest.java) cover restart, failed writes and compensation.
- [`GPSRuntime`](../../src/de/omegazirkel/risingworld/GPSRuntime.java)
- [`GPSDatabase`](../../src/de/omegazirkel/risingworld/gps/GPSDatabase.java)
- [`MarkerPermissions`](../../src/de/omegazirkel/risingworld/gps/MarkerPermissions.java)
- [`GpsMarkerExportService`](../../src/de/omegazirkel/risingworld/gps/exports/GpsMarkerExportService.java)
- [`MarkerPermissionsTest`](../../src-test/de/omegazirkel/risingworld/gps/MarkerPermissionsTest.java)
- [`MarkerPersistenceFailureTest`](../../src-test/de/omegazirkel/risingworld/gps/MarkerPersistenceFailureTest.java) covers failed inserts and missing update targets.

See the [repository index](../README.md) for lifecycle, build and cross-repository dependencies.
