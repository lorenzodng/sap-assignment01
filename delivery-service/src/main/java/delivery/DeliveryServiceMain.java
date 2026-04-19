package delivery;

import delivery.application.ShipmentManager;
import delivery.application.ShipmentManagerImpl;
import delivery.application.ShipmentRepository;
import delivery.infrastructure.InMemoryShipmentRepository;
import delivery.infrastructure.ShipmentAssignmentController;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import delivery.infrastructure.TrackingDeliveryController;
import io.vertx.ext.web.handler.CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("delivery-service").load();
        int port = Integer.parseInt(dotenv.get("PORT"));

        Vertx vertx = Vertx.vertx();

        ShipmentRepository repository = new InMemoryShipmentRepository();

        ShipmentManager shipmentManager = new ShipmentManagerImpl(repository);

        ShipmentAssignmentController assignmentController = new ShipmentAssignmentController(shipmentManager);
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipmentManager);

        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedHeader("Content-Type"));
        assignmentController.registerRoutes(router);
        trackingController.registerRoutes(router);

        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Delivery service started on port {}", port);
    }
}