package drone;

import drone.application.*;
import drone.infrastructure.DeliveryServiceClient;
import drone.infrastructure.DroneAssignmentController;
import drone.infrastructure.InMemoryDroneRepository;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import drone.domain.Drone;
import drone.domain.Position;
import java.util.ArrayList;
import java.util.List;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DroneServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("drone-service").load(); //carica le variabili del file .env
        String deliveryServiceUrl = dotenv.get("DELIVERY_SERVICE_URL");
        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea la flotta di droni (posizionati a Roma)
        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("drone-1", new Position(41.90, 12.49)));
        drones.add(new Drone("drone-2", new Position(41.91, 12.50)));
        drones.add(new Drone("drone-3", new Position(41.92, 12.51)));

        //crea il livello infrastruttura
        DroneRepository droneRepository = new InMemoryDroneRepository(drones);
        DeliveryServiceNotifier deliveryNotifier = new DeliveryServiceClient(vertx, deliveryServiceUrl);

        //crea i use case
        CheckDroneAvailability checker = new CheckDroneAvailabilityImpl();
        AssignDrone assigner = new AssignDroneImpl(checker);

        //crea l'orchestratore
        DroneAssignmentOrchestrator orchestrator = new DroneAssignmentOrchestratorImpl(assigner, deliveryNotifier, droneRepository);

        //crea il controller
        DroneAssignmentController droneController = new DroneAssignmentController(orchestrator);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        droneController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Drone service started on port {}", port);
    }
}