package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.domain.Drone;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.buffer.Buffer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//client che notifica l'assegnazione del drone verso delivery-service
@Adapter
public class DeliveryServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceClient.class);
    private final WebClient client;
    private final String deliveryServiceUrl;

    public DeliveryServiceClient(Vertx vertx, String deliveryServiceUrl) {
        this.client = WebClient.create(vertx);
        this.deliveryServiceUrl = deliveryServiceUrl;
    }

    //invia il messaggio di drone assegnato
    public void notifyDroneAssigned(String shipmentId, Drone drone, double pickupLatitude, double pickupLongitude, double deliveryLatitude, double deliveryLongitude) {

        //costruisce il messaggio
        JSONObject body = new JSONObject();
        body.put("assigned", true);
        body.put("droneId", drone.getId());
        body.put("droneSpeed", Drone.SPEED);
        body.put("droneLatitude", drone.getPosition().getLatitude());
        body.put("droneLongitude", drone.getPosition().getLongitude());
        body.put("pickupLatitude", pickupLatitude);
        body.put("pickupLongitude", pickupLongitude);
        body.put("deliveryLatitude", deliveryLatitude);
        body.put("deliveryLongitude", deliveryLongitude);
        body.put("assignedAt", System.currentTimeMillis());

        client.putAbs(deliveryServiceUrl + "/shipments/" + shipmentId + "/assignment").putHeader("Content-Type", "application/json").sendBuffer(Buffer.buffer(body.toString())) //invia il messaggio http trattando il body con un buffer (richiesto da vertx per recuperare i messaggi)
                .onSuccess(res -> log.info("Drone {} assigned to shipment {}", drone.getId(), shipmentId)) //in caso di successo
                .onFailure(err -> log.error("Failed to notify delivery service for shipment {}", shipmentId, err)); //in caso di fallimento
    }

    //invia il messaggio di drone non disponibile
    public void notifyDroneNotAvailable(String shipmentId) {

        //costruisce il messaggio
        JSONObject body = new JSONObject();
        body.put("assigned", false);

        client.putAbs(deliveryServiceUrl + "/shipments/" + shipmentId + "/assignment").sendBuffer(Buffer.buffer(body.toString())) //invia il messaggio http trattando il body con un buffer (richiesto da vertx per recuperare i messaggi
                .onSuccess(res -> log.warn("No available drones for shipment {}", shipmentId)) //in caso di successo
                .onFailure(err -> log.error("Failed to notify delivery service for shipment {}", shipmentId, err)); //in caso di fallimento
    }
}