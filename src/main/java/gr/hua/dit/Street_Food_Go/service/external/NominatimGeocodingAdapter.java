package gr.hua.dit.Street_Food_Go.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Adapter that implements GeocodingService using Nominatim (OpenStreetMap).
 * Nominatim provides free geocoding without requiring an API key.
 *
 * This is part of the Hexagonal Architecture - this adapter handles the external service
 * communication while the rest of the application only depends on the port (interface).
 *
 * Nominatim API Documentation: https://nominatim.org/release-docs/develop/api/Search/
 * Usage Policy: https://operations.osmfoundation.org/policies/nominatim/
 *
 * Note: Nominatim has a rate limit of 1 request per second.
 */
@Service
public class NominatimGeocodingAdapter implements GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(NominatimGeocodingAdapter.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String nominatimBaseUrl;

    public NominatimGeocodingAdapter(
            @Value("${external.nominatim.base-url:https://nominatim.openstreetmap.org}") String nominatimBaseUrl) {
        if (nominatimBaseUrl == null) {
            throw new NullPointerException("nominatimBaseUrl cannot be null");
        }
        // Use HTTP/1.1 to avoid HTTP/2 issues with Nominatim
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.nominatimBaseUrl = nominatimBaseUrl;
    }

    @Override
    public GeocodingResult geocodeAddress(String streetAddress, String city, String postalCode, String country) {
        // Build the full address query
        StringBuilder query = new StringBuilder();

        if (streetAddress != null && !streetAddress.isBlank()) {
            query.append(streetAddress);
        }
        if (city != null && !city.isBlank()) {
            if (query.length() > 0) query.append(", ");
            query.append(city);
        }
        if (postalCode != null && !postalCode.isBlank()) {
            if (query.length() > 0) query.append(", ");
            query.append(postalCode);
        }
        if (country != null && !country.isBlank()) {
            if (query.length() > 0) query.append(", ");
            query.append(country);
        }

        if (query.length() == 0) {
            return GeocodingResult.failure("No address provided");
        }

        return geocodeAddress(query.toString());
    }

    @Override
    public GeocodingResult geocodeAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            logger.warn("Empty address provided for geocoding");
            return GeocodingResult.failure("Empty address");
        }

        try {
            // Build URL with proper encoding - replace spaces with + for better compatibility
            String encodedAddress = fullAddress.replace(" ", "+");
            encodedAddress = encodedAddress.replace(",", "%2C");
            String url = String.format(
                    "%s/search?q=%s&format=json&limit=1&addressdetails=1",
                    nominatimBaseUrl,
                    encodedAddress
            );

            logger.info("Calling Nominatim API for address: {}", fullAddress);
            logger.info("Full URL: {}", url);

            // Build HTTP request with proper headers
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) StreetFoodGo/1.0")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String response = httpResponse.body();

            logger.info("Nominatim response: {}", response);

            JsonNode results = objectMapper.readTree(response);

            if (results == null || results.isEmpty() || !results.isArray() || results.size() == 0) {
                logger.warn("No geocoding results found for address: {}", fullAddress);
                return GeocodingResult.failure("Address not found");
            }

            JsonNode firstResult = results.get(0);
            String latStr = firstResult.path("lat").asText();
            String lonStr = firstResult.path("lon").asText();
            logger.info("Parsed lat={}, lon={}", latStr, lonStr);

            double lat = Double.parseDouble(latStr);
            double lon = Double.parseDouble(lonStr);
            String displayName = firstResult.path("display_name").asText();

            logger.info("Geocoded '{}' to lat={}, lon={}", fullAddress, lat, lon);

            return GeocodingResult.success(lat, lon, displayName);

        } catch (Exception e) {
            logger.error("Error calling Nominatim API: {}", e.getMessage(), e);
            return GeocodingResult.failure("Geocoding failed: " + e.getMessage());
        }
    }
}
