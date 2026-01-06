package gr.hua.dit.Street_Food_Go.service.external;

/**
 * Result object returned by the DeliveryTimeService.
 * Contains the calculated delivery time and distance.
 */
public class DeliveryTimeResult {

    private final int durationMinutes;
    private final double distanceKm;
    private final boolean success;
    private final String errorMessage;

    private DeliveryTimeResult(int durationMinutes, double distanceKm, boolean success, String errorMessage) {
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static DeliveryTimeResult success(int durationMinutes, double distanceKm) {
        return new DeliveryTimeResult(durationMinutes, distanceKm, true, null);
    }

    public static DeliveryTimeResult failure(String errorMessage) {
        return new DeliveryTimeResult(0, 0, false, errorMessage);
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (success) {
            return "DeliveryTimeResult{" +
                    "durationMinutes=" + durationMinutes +
                    ", distanceKm=" + distanceKm +
                    '}';
        } else {
            return "DeliveryTimeResult{error='" + errorMessage + "'}";
        }
    }
}
