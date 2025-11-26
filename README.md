# BookTicket :: Services :: Notification Service

## Overview

The **Notification Service** is an asynchronous, event-driven microservice responsible for handling all user-facing communications. It is designed to be highly reliable and decoupled from the core business logic, ensuring that notifications are sent without impacting critical user flows.

## Core Responsibilities

-   **Asynchronous Event Consumption:** Listens to Apache Kafka topics for events published by other services (e.g., `BookingConfirmationEvent`).
-   **Email Generation & Sending:** Uses email templates (Thymeleaf) and a third-party API (Brevo) to construct and send rich HTML emails.
-   **PDF & QR Code Generation:** Generates PDF e-tickets, complete with a unique QR code for validation, and attaches them to confirmation emails.
-   **Resilient Error Handling:** Implements a robust retry and Dead Letter Queue (DLQ) mechanism to ensure that failed notifications are never lost and can be reprocessed.

## Architecture & Asynchronous Flow
<img width="800" height="1000" alt="NotificationService" src="https://github.com/user-attachments/assets/d059ade6-e910-4c68-bc47-e2083ce5e98d" />


### How It Works

1.  **Event-Driven:** The service is completely decoupled from upstream services. It does not expose a REST API for sending notifications. Instead, it subscribes to Kafka topics and reacts to business events.
2.  **Third-Party API for Email:** To ensure high deliverability and bypass cloud provider SMTP blocks, the service uses the **Brevo** (formerly Sendinblue) HTTP API to send emails. This is a modern, reliable approach for cloud-native applications.
3.  **Dead Letter Queue (DLQ):** If a notification fails to send after several retries (e.g., due to an external API outage), the message is not lost. It is forwarded to a dedicated DLQ topic in Kafka. A scheduled job periodically attempts to re-process messages from the DLQ, providing a high degree of fault tolerance.
4.  **Template-Based Content:** Email content is generated using **Thymeleaf** templates, allowing the design and layout of emails to be managed independently of the application logic.

## Key Dependencies

-   **Spring Kafka:** For consuming messages from Apache Kafka topics.
-   **Brevo Java SDK:** The official library for interacting with the Brevo email API.
-   **Spring Boot Starter Thymeleaf:** For processing HTML email templates.
-   **OpenPDF & ZXing:** For generating PDF tickets and QR codes.
-   **Spring Retry:** To handle transient failures when sending emails.
-   **Eureka Discovery Client:** To register with the service registry.

## Configuration

The service is configured via properties from the **Config Server**. Key properties include:

-   `kafka.bootstrap-servers`: The address of the Kafka cluster.
-   `brevo.api.key`: The API key for authenticating with the Brevo service.
-   `spring.mail.username`: The "from" address used in outgoing emails.
-   `management.health.mail.enabled: false`: Disables the default mail health check to prevent timeouts in a cloud environment.
