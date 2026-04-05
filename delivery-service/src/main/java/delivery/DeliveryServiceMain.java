package delivery;

import delivery.infrastructure.ShipmentAssignment;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import delivery.domain.Shipment;
import delivery.infrastructure.TrackingDeliveryController;
import java.util.HashMap;
import java.util.Map;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("delivery-service").load(); //carica le variabili del file .env
        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea i consumer Kafka
        Map<String, Shipment> shipments = new HashMap<>();
        ShipmentAssignment assignmentController = new ShipmentAssignment(shipments);

        //crea il controller REST
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipments);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedHeader("Content-Type")); //registra un handler per la lettura di richieste provenienti da fonti diverse dal server (ovvero dal frontend)
        assignmentController.registerRoutes(router);
        trackingController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Delivery service started on port {}", port);
    }
}