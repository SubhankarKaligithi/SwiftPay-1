# SwiftPay – Real-Time Payment Ledger System

## Overview

SwiftPay is a fintech-inspired microservices application designed to process peer-to-peer (P2P) payments in a reliable, scalable, and event-driven manner. The project demonstrates modern backend engineering practices using Spring Boot, Apache Kafka, Redis, PostgreSQL, Docker, Kubernetes, and GitHub Actions.

The primary goal of SwiftPay is to ensure secure payment processing while maintaining transaction consistency, preventing duplicate transactions, and providing real-time ledger and analytics updates.

---

## Key Features

- Real-time payment processing
- Microservices architecture
- Event-driven communication using Kafka
- Redis-based idempotency handling
- PostgreSQL transaction persistence
- Asynchronous ledger processing
- Audit logging and analytics tracking
- Docker containerization
- Kubernetes deployment support
- GitHub Actions CI/CD pipeline
- Swagger/OpenAPI documentation

---

## Technology Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Redis
- Apache Kafka
- Maven
- Docker
- Kubernetes (Minikube)
- GitHub Actions
- Swagger / OpenAPI

---

## System Architecture

SwiftPay follows an event-driven microservices architecture consisting of three core services:

### Gateway Service

The Gateway Service acts as the entry point for all payment requests.

Responsibilities:

- Exposes REST APIs for payment creation
- Validates incoming requests
- Prevents duplicate transaction processing using Redis
- Persists payment data in PostgreSQL
- Publishes payment events to Kafka

Main Endpoint:

POST /v1/payments

---

### Ledger Service

The Ledger Service processes payment events received from Kafka.

Responsibilities:

- Consumes payment events
- Creates ledger records
- Updates payment status
- Maintains transaction consistency
- Publishes payment completion events

Kafka Topic Consumed:

payment.initiated

Kafka Topic Published:

payment.completed

---

### Analytics Service

The Analytics Service provides audit logging and analytics capabilities.

Responsibilities:

- Consumes completed payment events
- Generates audit records
- Stores analytics information
- Maintains transaction history
- Supports reporting and monitoring

Kafka Topic Consumed:

payment.completed

---

### Common Module

The Common module contains reusable components shared across all services.

Shared Components:

- PaymentInitiatedEvent
- PaymentCompletedEvent
- DTOs
- Constants
- Utility Classes
- Common Models

---

## Payment Processing Flow

### Step 1

A client submits a payment request through the Gateway Service.

### Step 2

The Gateway Service validates the request and checks Redis for idempotency.

### Step 3

If valid, the payment is stored in PostgreSQL with PENDING status.

### Step 4

A PaymentInitiatedEvent is published to Kafka.

### Step 5

The Ledger Service consumes the event and creates ledger records.

### Step 6

The Ledger Service updates transaction information and publishes a PaymentCompletedEvent.

### Step 7

The Analytics Service consumes the completion event and generates audit logs and analytics records.

---

## Database

PostgreSQL serves as the primary transactional database.

The system stores:

- Payment Information
- Ledger Records
- Transaction Statuses
- Audit Information

The database provides strong consistency and ACID transaction support required for financial applications.

---

## Redis Usage

Redis is used for idempotency management.

Each payment request is associated with a unique transaction identifier.

If the same transaction identifier is received again, the request is rejected to prevent duplicate processing.

Benefits:

- Duplicate request prevention
- Fast lookup performance
- Improved reliability

---

## Kafka Messaging

Apache Kafka enables asynchronous communication between services.

Topics used:

### payment.initiated

Published by Gateway Service and consumed by Ledger Service.

### payment.completed

Published by Ledger Service and consumed by Analytics Service.

Benefits:

- Loose coupling between services
- Improved scalability
- Better fault tolerance
- Event-driven architecture

---

## Project Structure

swiftpay/

├── common/

├── gateway-service/

├── ledger-service/

├── analytics-service/

├── k8s/

│ ├── namespace.yaml

│ ├── postgres.yaml

│ ├── redis.yaml

│ ├── kafka.yaml

│ ├── gateway-service.yaml

│ ├── ledger-service.yaml

│ ├── analytics-service.yaml

│ └── ingress.yaml

├── .github/

│ └── workflows/

│ └── ci.yml

├── docker-compose.yaml

├── pom.xml

└── README.md

---

## Running the Application

### Build Project

```bash
mvn clean install



                    +-------------------+
                    |   Client/Postman  |
                    +---------+---------+
                              |
                              v

                    +-------------------+
                    |  Gateway Service  |
                    +---------+---------+
                              |
                              |
                     Save Payment
                     PostgreSQL
                              |
                              v

                    +-------------------+
                    |       Kafka       |
                    +---------+---------+
                              |
                    payment.initiated
                              |
                              v

                    +-------------------+
                    |  Ledger Service   |
                    +---------+---------+
                              |
                    payment.completed
                              |
                              v

                    +-------------------+
                    | Analytics Service |
                    +-------------------+

Redis ---> Idempotency Cache
