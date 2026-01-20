package gr.hua.dit.Street_Food_Go.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter that implements DeliveryTimeService using OSRM (Open Source Routing Machine).
 * OSRM provides free routing/distance calculation without requiring an API key.
 *
 * This is part of the Hexagonal Architecture - this adapter handles the external service
 * communication while the rest of the application only depends on the port (interface).
 *
 * OSRM API Documentation: http://project-osrm.org/docs/v5.24.0/api/
 * Demo server: http://router.project-osrm.org
 */
@Service
public class OsrmDeliveryTimeAdapter implements DeliveryTimeService {

    private static final Logger logger = LoggerFactory.getLogger(OsrmDeliveryTimeAdapter.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String osrmBaseUrl;

    public OsrmDeliveryTimeAdapter(
            @Value("${external.osrm.base-url:http://router.project-osrm.org}") String osrmBaseUrl) {
        if (osrmBaseUrl == null) {
            throw new NullPointerException("osrmBaseUrl cannot be null");
        }
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.osrmBaseUrl = osrmBaseUrl;
    }

    @Override
    public DeliveryTimeResult calculateDeliveryTime(
            Double originLat, Double originLon,
            Double destinationLat, Double destinationLon) {

        // Validate coordinates
        if (originLat == null || originLon == null || destinationLat == null || destinationLon == null) {
            logger.warn("Missing coordinates for delivery time calculation");
            return DeliveryTimeResult.failure("Missing coordinates");
        }

        try {
            // OSRM uses longitude,latitude order (not lat,lon)
            String url = String.format(
                    "%s/route/v1/driving/%f,%f;%f,%f?overview=false",
                    osrmBaseUrl,
                    originLon, originLat,      // origin: lon,lat
                    destinationLon, destinationLat  // destination: lon,lat
            );

            logger.info("Calling OSRM API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // Check if the request was successful
            String code = root.path("code").asText();
            if (!"Ok".equals(code)) {
                String message = root.path("message").asText("Unknown error");
                logger.error("OSRM API error: {}", message);
                return DeliveryTimeResult.failure("OSRM error: " + message);
            }

            // Extract duration and distance from the first route
            JsonNode routes = root.path("routes");
            if (routes.isEmpty()) {
                logger.error("No routes found in OSRM response");
                return DeliveryTimeResult.failure("No route found");
            }

            JsonNode firstRoute = routes.get(0);
            double durationSeconds = firstRoute.path("duration").asDouble();
            double distanceMeters = firstRoute.path("distance").asDouble();

            // Convert to minutes and kilometers
            int durationMinutes = (int) Math.ceil(durationSeconds / 60.0);
            double distanceKm = distanceMeters / 1000.0;

            // Return just the route time (prep time is added by the caller)
            logger.info("Route time calculated: {} minutes, {} km", durationMinutes, distanceKm);

            return DeliveryTimeResult.success(durationMinutes, distanceKm);

        } catch (Exception e) {
            logger.error("Error calling OSRM API: {}", e.getMessage(), e);
            return DeliveryTimeResult.failure("Failed to calculate delivery time: " + e.getMessage());
        }
    }
}
