# API Gateway Service

## Low-Level Design (LLD) & In-Depth Overview

The **API Gateway** serves as the unified entry point for all external client traffic into the DevOps Pro ecosystem. It is built on **Spring Cloud Gateway**.

### Key Responsibilities
1. **Dynamic Routing**: Routes HTTP requests from the frontend UI to the appropriate backend microservices based on URL path predicates (e.g., `/api/v1/config/**` routes to `config-service`).
2. **Authentication & Authorization**: Simulates an Enterprise Entra ID / SSO flow. It validates incoming JWT tokens and injects the `X-Tenant-Id` header into downstream requests, enabling strict multi-tenancy.
3. **CORS Management**: Handles Cross-Origin Resource Sharing for the React frontend.
4. **Database Seeding**: On startup, the `DataSeeder.java` component automatically provisions the MongoDB `devops_users` database with default mock users and assigns them to tenants.

### How to Interact
- **Port**: `8080` (Internal Docker port)
- **Login Endpoint**: `POST /api/v1/auth/login`
  - **Payload**: `{"email": "sysadmin@devops.com", "password": "password123"}`
  - **Returns**: A JWT token to be used in the `Authorization: Bearer <token>` header for all subsequent requests.
