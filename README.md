# Brokerage Firm Challenge

This is a Spring Boot application simulating a basic brokerage platform. It manages customer assets and handles buy/sell orders.

## 🛠 Tech Stack

- Java 24
- Spring Boot 3.5.0
- Spring Data JPA
- H2 In-Memory Database
- Maven
- Postman (for API testing)

## 📦 Features

- Manage assets per customer
- Place buy/sell orders
- Track pending, matched, and canceled orders
- Filter orders by customer and time range

## 🚀 Getting Started

### Prerequisites

- Java 24
- Maven

### Run the App

```bash
./mvnw spring-boot:run
```
### Test the app

Example Postman Usage
Use Basic Auth if enabled:

Username: admin

Password: password


## Endpoints

All api endpoints using Basic Auth

### Create Order

Headers:
Content-Type: application/json

#### Request Body
{
"customerId": 1,
"assetName": "AAPL",
"orderSide": "BUY",
"size": 10,
"price": 200
}

http://localhost:8080/api/orders/create

### List Orders

http://localhost:8080/api/orders/list?customerId=1

### Delete Order

http://localhost:8080/api/orders/delete/1

### List Assets

http://localhost:8080/api/assets/list?customerId=1
