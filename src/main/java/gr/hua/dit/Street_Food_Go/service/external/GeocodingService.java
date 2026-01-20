package gr.hua.dit.Street_Food_Go.service.external;

/**
 * Port (interface) for geocoding services.
 * Converts addresses to geographic coordinates (latitude/longitude).
 *
 * This is part of the Hexagonal Architecture - adapters implement this interface
 * to connect to external geocoding services (Nominatim, Google, etc.).
 */
public interface GeocodingService {

    /**
     * Geocode an address to get its latitude and longitude.
     *
     * @param streetAddress The street address (e.g., "123 Main St")
     * @param city The city name
     * @param postalCode The postal/zip code (optional, can be null)
     * @param country The country name or code (e.g., "Greece" or "GR")
     * @return GeocodingResult containing coordinates or error information
     */
    GeocodingResult geocodeAddress(String streetAddress, String city, String postalCode, String country);

    /**
     * Geocode a full address string.
     *
     * @param fullAddress The complete address as a single string
     * @return GeocodingResult containing coordinates or error information
     */
    GeocodingResult geocodeAddress(String fullAddress);
}
