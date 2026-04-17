# Shipping on the Air - Living Document

**v1.6.0**

---

## Overview
Shipping on the Air is a drone-based delivery service that allows users to send packages from a pickup location to a destination within a range of approximately 40–45 km.  
Each delivery is performed by autonomous drones, which travel at a constant speed of 50 km/h and are subject to operational constraints such as battery consumption (2% per km) and a minimum safety threshold of 10%.  
Users can request deliveries by specifying pickup and destination coordinates, along with time constraints, and can track the shipment in real time, including its status, current position and estimated remaining time.

---

## 1. Analysis

### 1.1 User Stories

#### Delivery Request

    As a user,
    I want to request a package delivery
    so that I can send a package from one place to another.

#### Shipment Tracking

    As a user,
    I want to track the status of my shipment,
    so that I can know whether my package is scheduled, in progress, completed or cancelled.

    As a user,
    I want to track the current position of the drone,
    so that I can know where my package is in real time.

    As a user,
    I want to know the remaining delivery time,
    so that I can know how long it will take to complete the delivery.


### 1.2 Use Cases

The actors involved are:
- **User**: the actor that accesses the system to request and track package deliveries.
- **System**: the Shipping on the Air system.

#### Request a Delivery

- **Primary actor**: User  
- **Goal**: Submit a new package delivery request.  
- **Preconditions**: None.  
- **Postconditions**: A shipment is created and a drone is assigned to the delivery.

**Main flow**

1. The user submits a delivery request.
2. The system validates the request.
3. The system searches for available drones that can complete the delivery.
4. The system assigns the most suitable drone to the delivery.
5. The system creates a shipment and confirms it to the user.

**Alternate/Exception flows**
- 2a: Invalid request (e.g. invalid coordinates, negative weight) → System returns an error.
- 3a: No available drone found → System cancels the shipment and notifies the user.

---

#### Track a Shipment

- **Primary actor**: User  
- **Goal**: Monitor the current status, position and remaining time of a shipment.  
- **Preconditions**: A shipment exists.  
- **Postconditions**: The user receives the requested tracking information.

**Main flow**

1. The user requests tracking information, specifying the shipment to track.
2. The system retrieves the current status of the shipment.
3. The system calculates the current position of the drone.
4. The system calculates the remaining delivery time.
5. The system returns the tracking information to the user.

**Alternate/Exception flows**
- 1a: Shipment not found → System returns an error.
- 3a: Drone not yet assigned → System returns position not available.


### 1.3 Functional Requirements

- The user can request a package delivery specifying pickup location, delivery location, pickup date/time and time limit.
- The user can know the current status of the shipment.
- The user can track the current position of the drone in real time.
- The user can know the remaining time to complete the delivery.


### 1.4 Non-Functional Requirements

- **Availability**: the system must always be reachable even in case of minor failures.
- **Performance**: the system must respond in real time for tracking requests.
- **Scalability**: the system must handle multiple simultaneous deliveries.
- **Maintainability**: the system must be modifiable and deployable without changes to one component impacting the others.

### 1.5 Strategic Design

#### 1.5.1 Ubiquitous Language

- **Shipping on the Air**
    - The online system that allows users to request package deliveries through drones.
    - It provides functionalities to request a delivery, track the package in real time and monitor the delivery status.


- **User**
    - The actor that accesses the system to request and track package deliveries.


- **Package**
    - The physical item to be delivered from a pickup location to a delivery location.


- **Drone**
    - The autonomous vehicle used to deliver packages.


- **Delivery Request**
    - The request submitted by a user to deliver a package from a pickup location to a delivery location.
    - It specifies a pickup date/time and a maximum delivery time limit.


- **Shipment**
    - The actual delivery process associated to a delivery request, once a drone has been assigned.
    - It tracks the current status and position of the delivery.


- **Status**
    - The current state of a shipment, which can be:
        - `Scheduled`: a drone has been assigned to the shipment.
        - `In Progress`: the drone has reached the pickup location and is flying towards the delivery location.
        - `Completed`: the drone has reached the delivery location.
        - `Cancelled`: no drone is available for the shipment.


- **Position**
    - A geographic location expressed as latitude and longitude.
    - Used to represent the pickup location, delivery location and current drone position.


- **To request a delivery** *(Action)*
    - Performed by the user to submit a new delivery request, specifying pickup location, delivery location, pickup date/time and time limit.


- **To assign a drone** *(Action)*
    - Performed by the system to select and assign the most suitable available drone to a delivery request.


- **To track a shipment** *(Action)*
    - Performed by the user to monitor the delivery, including the current status of the shipment, the current position of the drone and the estimated time remaining to complete the delivery.

#### 1.5.2 Bounded Contexts

- **Request**: creates and validates delivery requests.
- **Drone**: check drones availability and assigns drones.
- **Shipment**: tracks the status of the shipments, the current positions of drones and the remaining time to complete deliveries.

#### 1.5.3 Context Map

```mermaid
flowchart LR
    RS[Request]
    DS[Drone]
    DLS[Shipment]
    
    RS -->|Request created| DS
    DS -->|Drone assigned| DLS
    DS -->|No drone available| DLS
```

#### 1.5.4 Domain Events

- **Request**:
    - `Request created`: published when the user creates a shipment request.

- **Drone**:
    - `Drone assigned`: published when a drone is successfully assigned to a shipment.
    - `No drone available`: published when no drone is available for the shipment.

- **Shipment**:
    - No domain events published; only consumes events from other contexts.

## 2. Design

### 2.1 Tactical Design

#### Request

- **Entities**:
    - `User`: represents the user that submitted the delivery request.
    - `Package`: represents the package to be delivered.
- **Aggregates**:
    - `Shipment` *(root)*: represents the delivery request, composed of `User` and `Package`.
- **Value Objects**:
    - `Position`: represents a geographic location expressed as latitude and longitude.
- **Invariants**:
    - Pickup date and time must be in the future.
    - Package weight must be greater than 0.
    - Delivery time limit must be greater than 0.
    - Pickup and delivery coordinates must be valid.

#### Drone

- **Entities**:
    - `Drone`: represents the drone available for deliveries.
- **Value Objects**:
    - `Position`: represents the current geographic position of the drone.
- **Invariants**:
    - Drone weight capacity must be equal or greater than the package weight.
    - Drone must be able to complete the delivery within the time limit.
    - Drone battery must be sufficient to cover the full route.
    - The assigned drone is the one closest to the pickup location.

#### Shipment

- **Aggregates**:
    - `Shipment` *(root)*: represents the delivery process, composed of `Position`.
- **Value Objects**:
    - `Position`: represents a geographic location expressed as latitude and longitude.
- **Domain Types**:
    - `ShipmentStatus`: represents the possible states of a shipment (`Scheduled`, `In Progress`, `Completed`, `Cancelled`).

### 2.2 Architecture

#### Architectural Style

The system adopts a **microservices** architectural style, decomposing the domain into independent services, each responsible for a specific bounded context:

- **Request Service**: implements the **Request** bounded context.
- **Drone Service**: implements the **Drone** bounded context.
- **Delivery Service**: implements the **Shipment** bounded context.

#### Internal Architecture

Each microservice adopts a **clean architecture** style, organized into three layers:
- **Domain**: contains the core business logic, including entities, aggregates, value objects and domain events.
- **Application**: contains the application logic, orchestrating the domain objects to fulfill use cases, and defines the ports used by the infrastructure layer.
- **Infrastructure**: contains the adapters that implement the ports defined in the application layer.

Clean architecture was chosen because it explicitly separates the application logic from the domain logic, providing a clearer structure for orchestrating use cases without mixing them with the core domain, and making the system easier to extend and maintain in case of future changes.

```mermaid
flowchart LR
    C([Client])

    subgraph System
        subgraph RS[Request Service]
            RS_I[Infrastructure] --> RS_A[Application] --> RS_D[Domain]
        end

        subgraph DS[Drone Service]
            DS_I[Infrastructure] --> DS_A[Application] --> DS_D[Domain]
        end

        subgraph DLS[Delivery Service]
            DLS_I[Infrastructure] --> DLS_A[Application] --> DLS_D[Domain]
        end
    end

    C -->|request| RS
    C -->|request| DLS
    RS -->|request| DS
    DS -->|request| DLS
```

### 2.3 API Design

#### 2.3.1 Conceptual Design

##### Interaction Model

All interactions follow a non-blocking request/response pattern in which each request is propagated through multiple microservices without suspending threads:

- **Client → Request Service**: the client sends a shipment request and waits for the response.
- **Client → Delivery Service**: the client sends tracking requests and waits for the response.
- **Request Service → Drone Service**: once a shipment request is created, Request Service notifies Drone Service asynchronously to assign a drone.
- **Drone Service → Delivery Service**: once a drone is assigned, Drone Service notifies Delivery Service asynchronously to start tracking the shipment.

This approach allows the system to handle many simultaneous requests efficiently, avoiding thread exhaustion and improving scalability.

##### Execution Model

Each microservice adopts an asynchronous event-loop execution model, using a pool of event-loop threads to handle multiple concurrent requests efficiently.
Although interactions appear synchronous from the client perspective, each microservice processes requests in a non-blocking way, delegating I/O operations to the event loop.

This model was chosen to satisfy the scalability requirement, allowing each microservice to handle multiple simultaneous deliveries without blocking threads on I/O operations, thus improving throughput and responsiveness under high load.

#### 2.3.2 Technical Design

- **Interaction**: REST is chosen as the communication protocol between microservices, implemented in a non-blocking way.
- **Execution**: Vert.x is chosen to implement the asynchronous event-loop execution.


### 2.4 Functional Requirements Assignment

- **Request Service**: 
  - The user can request a package delivery.


- **Drone Service**: no direct functional requirements.


- **Delivery Service**:
  - The user can know the current status of the shipment.
  - The user can track the current position of the drone.
  - The user can know the remaining time to complete the delivery.

### 2.5 Non-Functional Requirements Conformance

The non-functional requirements are satisfied by the following architectural choices:

- **Availability**: independence of microservices — if one service goes down, the others continue to operate.
- **Performance**: non-blocking asynchronous model, which ensures real-time responses for tracking requests without suspending threads.
- **Scalability**: microservices architecture combined with an event-loop execution model, which allows each service to scale independently and handle multiple concurrent requests efficiently.
- **Maintainability**: clean architecture and microservices style, which allows each service to be modified and deployed independently.