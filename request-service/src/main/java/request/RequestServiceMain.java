package request;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.core.http.HttpMethod;
import request.application.*;
import request.infrastructure.DroneServiceClient;
import request.infrastructure.ShipmentRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceMain {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("request-service").load();
        String droneServiceUrl = dotenv.get("DRONE_SERVICE_URL");
        int port = Integer.parseInt(dotenv.get("PORT"));

        Vertx vertx = Vertx.vertx();

        DroneServiceNotifier droneServiceNotifier = new DroneServiceClient(vertx, droneServiceUrl);

        CreateShipmentRequest createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequest validateShipmentRequest = new ValidateShipmentRequestImpl();
        ShipmentScheduler shipmentScheduler = new ShipmentSchedulerImpl(droneServiceNotifier, vertx);

        ShipmentRequestOrchestrator orchestrator = new ShipmentRequestOrchestratorImpl(createShipmentRequest, validateShipmentRequest, shipmentScheduler);

        ShipmentRequestController shipmentController = new ShipmentRequestController(orchestrator);

        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedHeader("Content-Type"));
        shipmentController.registerRoutes(router);

        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}