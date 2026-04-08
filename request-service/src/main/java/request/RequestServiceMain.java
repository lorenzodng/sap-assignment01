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
        Dotenv dotenv = Dotenv.configure().directory("request-service").load(); //carica le variabili del file .env
        String droneServiceUrl = dotenv.get("DRONE_SERVICE_URL");
        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea i use case
        CreateShipmentRequest createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequest validateShipmentRequest = new ValidateShipmentRequestImpl();
        ShipmentScheduler shipmentScheduler = new ShipmentSchedulerImpl();

        //crea il producer
        DroneServiceNotifier droneServiceNotifier = new DroneServiceClient(vertx, droneServiceUrl);

        //crea l'orchestratore
        ShipmentRequestOrchestrator orchestrator = new ShipmentRequestOrchestratorImpl(createShipmentRequest, validateShipmentRequest, shipmentScheduler);

        //crea il controller REST
        ShipmentRequestController shipmentController = new ShipmentRequestController(orchestrator, droneServiceNotifier);

        //crea il router e registra la rotta
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedHeader("Content-Type")); //registra un handler per la lettura di richieste provenienti da fonti diverse dal server (ovvero dal frontend)
        shipmentController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}