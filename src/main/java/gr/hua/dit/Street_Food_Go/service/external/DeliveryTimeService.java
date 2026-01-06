package gr.hua.dit.Street_Food_Go.service.external;

/**
 * Port (interface) for delivery time calculation.
 * This is part of the Hexagonal Architecture - the application depends on this interface,
 * not on any specific implementation (adapter).
 */
public interface DeliveryTimeService {

    /**
     * Calculates the estimated delivery time between two locations.
     *
     * @param originLat       Latitude of the origin (restaurant)
     * @param originLon       Longitude of the origin (restaurant)
     * @param destinationLat  Latitude of the destination (delivery address)
     * @param destinationLon  Longitude of the destination (delivery address)
     * @return DeliveryTimeResult containing duration in minutes and distance in km
     */
    DeliveryTimeResult calculateDeliveryTime(
            Double originLat, Double originLon,
            Double destinationLat, Double destinationLon
    );
}
