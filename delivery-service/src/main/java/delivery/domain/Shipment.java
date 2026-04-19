package delivery.domain;

import buildingblocks.domain.AggregateRoot;

public class Shipment implements AggregateRoot<String> {

    private static final double MS_TO_HOURS = 3600000.0;
    private static final int MINUTES_IN_HOUR = 60;
    private final String id;
    private Position droneInitialPosition;
    private Position pickupPosition;
    private Position deliveryPosition;
    private long assignedAt;
    private double deliverySpeed;
    private ShipmentStatus status;

    public Shipment(String id, Position droneInitialPosition, Position pickupPosition, Position deliveryPosition, long assignedAt, double deliverySpeed) {
        this.id = id;
        this.droneInitialPosition = droneInitialPosition;
        this.pickupPosition = pickupPosition;
        this.deliveryPosition = deliveryPosition;
        this.assignedAt = assignedAt;
        this.deliverySpeed = deliverySpeed;
        this.status = ShipmentStatus.SCHEDULED;
    }

    public void cancelled() {
        if (status != ShipmentStatus.COMPLETED) {
            status = ShipmentStatus.CANCELLED;
        }
    }

    public Position calculateCurrentDronePosition() {

        if (droneInitialPosition == null || status == ShipmentStatus.CANCELLED) {
            return null;
        }

        double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS;
        double distanceCovered = deliverySpeed * elapsedHours;

        //phase 1: drone moves toward the pickup location
        double distanceToPickup = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude());
        if (distanceCovered < distanceToPickup) {
            return interpolate(droneInitialPosition, pickupPosition, distanceCovered / distanceToPickup);
        }

        //phase 2: drone moves toward the delivery destination
        double distanceCovered2 = distanceCovered - distanceToPickup;
        double distanceToDelivery = GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude());
        if (distanceCovered2 < distanceToDelivery) {
            return interpolate(pickupPosition, deliveryPosition, distanceCovered2 / distanceToDelivery);
        }

        return deliveryPosition;
    }

    public double calculateRemainingTime() {

        if (status == ShipmentStatus.CANCELLED) {
            return 0;
        }

        double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS;
        double distanceCovered = deliverySpeed * elapsedHours;
        double totalDistance = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude()) + GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude());
        double remainingDistance = Math.max(0, totalDistance - distanceCovered);
        return (int) Math.ceil((remainingDistance / deliverySpeed) * MINUTES_IN_HOUR);
    }

    public ShipmentStatus updateStatus() {
        if (status != ShipmentStatus.CANCELLED) {
            double elapsedHours = (System.currentTimeMillis() - assignedAt) / MS_TO_HOURS;
            double distanceCovered = deliverySpeed * elapsedHours;
            double distanceToPickup = GeoUtils.haversine(droneInitialPosition.getLatitude(), droneInitialPosition.getLongitude(), pickupPosition.getLatitude(), pickupPosition.getLongitude());
            double distanceToDelivery = GeoUtils.haversine(pickupPosition.getLatitude(), pickupPosition.getLongitude(), deliveryPosition.getLatitude(), deliveryPosition.getLongitude());
            double totalDistance = distanceToPickup + distanceToDelivery;
            if (distanceCovered >= totalDistance) {
                this.status = ShipmentStatus.COMPLETED;
            } else if (distanceCovered >= distanceToPickup) {
                this.status = ShipmentStatus.IN_PROGRESS;
            }
        }

        return status;
    }

    @Override
    public String getId() {
        return id;
    }

    private Position interpolate(Position from, Position to, double fraction) {
        double lat = from.getLatitude() + (to.getLatitude() - from.getLatitude()) * fraction;
        double lon = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * fraction;
        return new Position(lat, lon);
    }
}