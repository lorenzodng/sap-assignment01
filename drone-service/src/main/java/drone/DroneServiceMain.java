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
        Dotenv dotenv = Dotenv.configure().directory("drone-service").load();
        String deliveryServiceUrl = dotenv.get("DELIVERY_SERVICE_URL");
        int port = Integer.parseInt(dotenv.get("PORT"));

        Vertx vertx = Vertx.vertx();

        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("drone-1", new Position(41.90, 12.49)));
        drones.add(new Drone("drone-2", new Position(41.91, 12.50)));
        drones.add(new Drone("drone-3", new Position(41.92, 12.51)));

        DroneRepository droneRepository = new InMemoryDroneRepository(drones);

        DeliveryServiceNotifier deliveryNotifier = new DeliveryServiceClient(vertx, deliveryServiceUrl);

        CheckDroneAvailability checker = new CheckDroneAvailabilityImpl();
        AssignDrone assigner = new AssignDroneImpl(checker);

        DroneAssignmentOrchestrator orchestrator = new DroneAssignmentOrchestratorImpl(assigner, deliveryNotifier, droneRepository);

        DroneAssignmentController droneController = new DroneAssignmentController(orchestrator);

        Router router = Router.router(vertx);
        droneController.registerRoutes(router);

        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Drone service started on port {}", port);
    }
}