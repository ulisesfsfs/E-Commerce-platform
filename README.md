#  E-Commerce Platform

Scalable e-commerce platform built with **Spring Boot Microservices** and **Spring Cloud**.


## Architecture

This project implements a **microservices architecture** with the following components:

- **API Gateway**: Single entry point for all client requests with JWT authentication
- **Service Discovery**: Eureka server for service registration and discovery
- **Config Server**: Centralized configuration management
- **Microservices**: Independent services handling specific business domains
- **Event Bus**: Apache Kafka for asynchronous communication
- **Databases**: PostgreSQL (relational), MongoDB (document), Redis (caching)

## Microservices

| **API Gateway** 
| **Discovery Server** 
| **Config Server** 
| **User Service** 
| **Product Service** |
| **Cart Service** 
| **Order Service** 
| **Payment Service** 
| **Notification Service** 

##  Infrastructure

**Docker Services** (defined in `docker-compose.yml`):

- **PostgreSQL**: Primary relational database 
- **MongoDB**: Document database for flexible schemas 
- **Redis**: In-memory cache for performance optimization 
- **Kafka**: Message broker for event-driven architecture
##  Observability & Monitoring

The platform features a production-ready observability stack built upon the three core pillars of observability:

- **Metrics**: [Micrometer](https://micrometer.io/) + [Prometheus](https://prometheus.io/)
  - Full exposure of Spring Boot Actuator management endpoints.
  - Custom business metrics (`ecommerce_orders_total`, `ecommerce_payments_processed_total`, `ecommerce_products_low_stock`, `ecommerce_orders_amount_total`).
- **Centralized Logging**: [Grafana Loki](https://grafana.com/oss/loki/) + [Promtail](https://grafana.com/docs/loki/latest/send-data/promtail/)
  - Real-time log aggregation and querying across all microservice containers.
- **Distributed Tracing**: [Micrometer Tracing](https://micrometer.io/) + [Zipkin](https://zipkin.io/)
  - Automatic `traceId` and `spanId` propagation for end-to-end request correlation across the API Gateway and microservices.
- **Visualization**: [Grafana](https://grafana.com/)

###  Dashboards & Visualizations

| Grafana — Business & System Metrics | Grafana Loki — Centralized Logs |
| :---: | :---: |
| ![Grafana Dashboard](./docs/images/grafana-metrics.png) | ![Loki Logs](./docs/images/loki-logs.png) |

| Zipkin — Distributed Tracing |
| :---: |
| ![Zipkin Tracing](./docs/images/zipkin-trace.png) |

### Local Monitoring Endpoints

| Service | URL | Credentials |
| :--- | :--- | :--- |
| **Grafana** | `http://localhost:3002` | `admin` / `admin` |
| **Prometheus** | `http://localhost:9090` | — |
| **Zipkin UI** | `http://localhost:9411` | — |
| **Mailpit (SMTP)** | `http://localhost:8025` | — |


##  Event-Driven Communication with Kafka

The platform uses **Apache Kafka** for asynchronous communication between services:
### Kafka Topics & Event Flow

```mermaid
flowchart LR
    %% Productores (Izquierda)
    subgraph Emisores ["Publisher"]
        OrderService["Order Service"]
        ProductService["Product Service"]
        PaymentService["Payment Service"]
    end

    %% Tópicos Kafka (Centro)
    subgraph Kafka ["  Kafka topic"]
        T_Order["order-events"]
        T_Stock["stock-events"]
        T_Payment["payment-events"]
        T_Cancel["order-cancelled-events"]
    end

    %% Consumidores (Derecha)
    subgraph Receptores ["Consumer"]
        ProductConsumer["Product Service"]
        OrderConsumer["Order Service"]
        NotifConsumer["Notification Service"]
    end

    %% Conexiones: Publicadores -> Tópicos
    OrderService --> T_Order
    ProductService --> T_Stock
    PaymentService --> T_Payment
    OrderService --> T_Cancel

    %% Conexiones: Tópicos -> Consumidores
    T_Order --> ProductConsumer
    
    T_Stock --> OrderConsumer
    T_Stock --> NotifConsumer

    T_Payment --> OrderConsumer
    T_Payment --> ProductConsumer
    T_Payment --> NotifConsumer

    T_Cancel --> ProductConsumer
```

##  Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17+
- Maven 3.8+

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd E-Commerce-platform
   ```

2. **Start infrastructure**
   ```bash
   docker-compose up -d
   ```

3. **Build the project**
   ```bash
   mvn clean build
   ```

4. **Run microservices** (each in separate terminal)
   ```bash
   cd discovery-server && mvn spring-boot:run
   cd config-server && mvn spring-boot:run
   cd api-gateway && mvn spring-boot:run
   cd user-service && mvn spring-boot:run
   cd product-service && mvn spring-boot:run
   cd cart-service && mvn spring-boot:run
   cd order-service && mvn spring-boot:run
   cd payment-service && mvn spring-boot:run
   cd notification-service && mvn spring-boot:run
   ```

5. **Access services**
   - API Gateway: http://localhost:8080
   - Eureka Dashboard: http://localhost:8761
   - Kafka: localhost:9092

##  Technology Stack

### Core Framework
- **Spring Boot 3.4.1** - Application framework
- **Spring Cloud 2024.0.0** - Cloud-native development
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Eureka** - Service Discovery
- **Spring Cloud Config** - Configuration Server

### Data & Messaging
- **PostgreSQL 16** - Relational database
- **MongoDB 7** - Document database
- **Redis 7** - Caching layer
- **Apache Kafka 3.7.0** - Event streaming

### Security
- **JWT (JJWT 0.12.5)** - Token-based authentication
- **Spring Security** - Authorization & access control

### Development Tools
- **Lombok 1.18.34** - Reduce boilerplate code
- **Maven** - Build management
- **Docker** - Containerization

##  License

This project is licensed under the MIT License.

