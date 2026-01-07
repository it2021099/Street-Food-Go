# StreetFoodGo

A food ordering and delivery system built with Spring Boot for the Distributed Systems course (HUA DIT).

## Project Overview

StreetFoodGo is a web application that allows customers to order food from restaurants for delivery or pickup. Restaurant owners can manage their establishments, menus, and incoming orders.

### Features

- **Customer Features**
  - Browse restaurants and menus
  - Place orders (delivery or pickup)
  - Manage delivery addresses
  - Track order status

- **Restaurant Owner Features**
  - Create and manage restaurants
  - Manage menu items
  - Accept/reject incoming orders
  - Update order status through the fulfillment workflow

- **External Service Integration**
  - OSRM (Open Source Routing Machine) for delivery time estimation
  - Calculates route distance and estimated delivery time

## Technology Stack

- **Backend**: Spring Boot 3.5.8
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA / Hibernate
- **Security**:
  - JWT authentication for REST API
  - Session/Cookie authentication for web UI
- **Frontend**: Thymeleaf templates
- **API Documentation**: OpenAPI 3.0 / Swagger UI
- **Build Tool**: Maven

## Architecture

The project follows a layered architecture:

```
├── controller/          # REST API controllers
│   └── web/            # Thymeleaf web controllers
├── service/            # Business logic interfaces
│   ├── impl/           # Service implementations
│   └── external/       # External service integration (Hexagonal/Ports-Adapters)
├── repository/         # Data access layer
├── model/              # JPA entities
├── dto/                # Data Transfer Objects
├── util/               # Mappers and utilities
├── security/           # JWT and authentication
└── config/             # Configuration classes
```

### External Service (Hexagonal Architecture)

The delivery time calculation uses the Ports and Adapters pattern:
- **Port**: `DeliveryTimeService` interface
- **Adapter**: `OsrmDeliveryTimeAdapter` implementation

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd Street-Food-Go

# Build and run
./mvnw spring-boot:run
```

The application will start at `http://localhost:8080`

### Default URLs

| URL | Description |
|-----|-------------|
| `http://localhost:8080/` | Home page (restaurant listing) |
| `http://localhost:8080/login` | Login page |
| `http://localhost:8080/register` | Registration page |
| `http://localhost:8080/swagger-ui.html` | API Documentation |
| `http://localhost:8080/h2-console` | H2 Database Console |

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Restaurants
- `GET /api/restaurants` - Get all restaurants
- `GET /api/restaurants/{id}` - Get restaurant by ID
- `GET /api/restaurants/open` - Get open restaurants
- `GET /api/restaurants/search?keyword=` - Search restaurants
- `POST /api/restaurants/owner/{ownerId}` - Create restaurant (Owner)
- `PUT /api/restaurants/{id}` - Update restaurant (Owner)
- `PATCH /api/restaurants/{id}/open` - Open restaurant (Owner)
- `PATCH /api/restaurants/{id}/close` - Close restaurant (Owner)

### Orders
- `POST /api/orders` - Create order (Customer)
- `POST /api/orders/{id}/items` - Add item to order (Customer)
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}` - Get customer orders
- `GET /api/orders/restaurant/{restaurantId}` - Get restaurant orders (Owner)
- `PATCH /api/orders/{id}/status/{status}` - Update order status (Owner)
- `PATCH /api/orders/{id}/cancel` - Cancel order

### Delivery
- `GET /api/delivery/estimate?restaurantId=&addressId=` - Estimate delivery time

## User Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | Can browse, order, and manage addresses |
| `RESTAURANT_OWNER` | Can manage restaurants, menus, and orders |
| `GUEST` | Can browse restaurants (read-only) |

## Order Status Flow

```
PENDING → ACCEPTED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
    ↓         ↓                        ↓
REJECTED  CANCELLED                COMPLETED (for pickup)
```

## Configuration

Key configuration in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:h2:mem:streetfoodgo
spring.h2.console.enabled=true

# JWT
jwt.secret=<base64-encoded-secret>
jwt.expiration=86400000

# External Service
external.osrm.base-url=http://router.project-osrm.org
```

## Project Structure

```
src/main/
├── java/gr/hua/dit/Street_Food_Go/
│   ├── StreetFoodGoApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── RestaurantController.java
│   │   ├── OrderController.java
│   │   ├── MenuItemController.java
│   │   ├── AddressController.java
│   │   ├── DeliveryController.java
│   │   └── web/
│   │       ├── WebController.java
│   │       ├── CustomerWebController.java
│   │       └── OwnerWebController.java
│   ├── service/
│   │   ├── impl/
│   │   └── external/
│   ├── model/
│   ├── repository/
│   ├── dto/
│   ├── util/
│   └── security/
└── resources/
    ├── application.properties
    └── templates/
        ├── fragments/
        ├── customer/
        └── owner/
```

## Authors

- Grigorios Pavlou: 2021081
- Theodoros Triantafillou: 2021099
- Prountzos Konstantinos: 21984

## License

This project is part of the Distributed Systems course at Harokopio University of Athens.
