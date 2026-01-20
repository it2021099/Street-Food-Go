package gr.hua.dit.Street_Food_Go.service.external;

/**
 * Result object returned by the GeocodingService.
 * Contains the latitude and longitude for a given address.
 */
public class GeocodingResult {

    private final Double latitude;
    private final Double longitude;
    private final String displayName;
    private final boolean success;
    private final String errorMessage;

    private GeocodingResult(Double latitude, Double longitude, String displayName, boolean success, String errorMessage) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static GeocodingResult success(Double latitude, Double longitude, String displayName) {
        return new GeocodingResult(latitude, longitude, displayName, true, null);
    }

    public static GeocodingResult failure(String errorMessage) {
        return new GeocodingResult(null, null, null, false, errorMessage);
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getDisplayName() {
        return displayName;
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
            return "GeocodingResult{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", displayName='" + displayName + '\'' +
                    '}';
        } else {
            return "GeocodingResult{error='" + errorMessage + "'}";
        }
    }
}
