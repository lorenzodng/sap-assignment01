package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import request.application.DroneServiceNotifier;
import request.domain.Shipment;

@Adapter
public class DroneServiceClient implements DroneServiceNotifier {

    private static final Logger log = LoggerFactory.getLogger(DroneServiceClient.class);
    private final WebClient client;
    private final String droneServiceUrl;

    public DroneServiceClient(Vertx vertx, String droneServiceUrl) {
        this.client = WebClient.create(vertx);
        this.droneServiceUrl = droneServiceUrl;
    }

    @Override
    public Future<Void> notifyShipmentRequest(Shipment shipment) {
        JSONObject body = new JSONObject();
        body.put("shipmentId", shipment.getId());
        body.put("pickupLatitude", shipment.getPickupLocation().getLatitude());
        body.put("pickupLongitude", shipment.getPickupLocation().getLongitude());
        body.put("deliveryLatitude", shipment.getDeliveryLocation().getLatitude());
        body.put("deliveryLongitude", shipment.getDeliveryLocation().getLongitude());
        body.put("packageWeight", shipment.getPackage().getWeight());
        body.put("deliveryTimeLimit", shipment.getDeliveryTimeLimit());

        return client.postAbs(droneServiceUrl + "/shipments/assign").putHeader("Content-Type", "application/json").sendBuffer(Buffer.buffer(body.toString()))
                .compose(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return Future.succeededFuture();
                    } else {
                        return Future.failedFuture(new DroneServiceException("Drone service error: " + response.statusCode()));
                    }
                })
                .onSuccess(res -> log.info("Shipment {} request notified", shipment.getId()))
                .onFailure(err -> log.error("Failed to notify drone service for shipment {}", shipment.getId(), err))
                .mapEmpty();
    }
}