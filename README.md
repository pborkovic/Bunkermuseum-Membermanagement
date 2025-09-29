# Bunkermuseum Wurzenpass - Member Management Platform

A comprehensive member management platform for the Bunkermuseum Wurzenpass, providing tools for member registration, management, and administrative functions.

## Tech Stack

| Technology | Version | Description |
|------------|---------|-------------|
| ☕ **Java** | 21 | Backend language |
| 🌱 **Spring Boot** | 3.5.5 | Application framework |
| 🛡️ **Spring Security** | 3.5.5 | Authentication & authorization |
| 🗄️ **Spring Data JPA** | 3.5.5 | Data persistence layer |
| 🔧 **Vaadin** | 24.8.8 | Full-stack web framework |
| ⚛️ **React** | 18.3.1 | Frontend components |
| 📜 **TypeScript** | 5.8.3 | Type-safe frontend development |
| 🐘 **PostgreSQL** | Runtime | Production database |
| ☕ **Caffeine** | Latest | Caching layer |
| 🐳 **Docker** | Multi-stage | Containerization |
| 📊 **JaCoCo** | 0.8.11 | Code coverage |
| 🚀 **Vite** | 6.3.5 | Frontend build tool |

## Prerequisites

- Java 21 or higher
- Node.js (for frontend dependencies)
- PostgreSQL (for production)
- Docker (optional, for containerized deployment)

## Quick Start

### Development Mode

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd Bunkermuseum
   ```

2. **Start the application:**
   ```bash
   ./dev-start.sh
   ```

3. **Access the application:**
   - Open your browser to `http://localhost:8080`


## Testing

Run the test suite:

```bash
./mvnw test
```

Generate code coverage report:

```bash
./mvnw test jacoco:report
```

Coverage reports will be available in `target/site/jacoco/`

## Database Configuration

## Development Resources

- [Vaadin Documentation](https://vaadin.com/docs)
- [Spring Boot Reference](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
