# PeerNotes Application

The **PeerNotes** application allows users to create, update, delete, and share notes. It is built using **Spring Boot**, and utilizes **MongoDB** for data persistence. The application follows RESTful API principles and integrates various third-party tools to handle authentication, data conversion, and other utilities.

## Framework, Database, and Tools Used

### 1. **Spring Boot (Framework)**
- **Choice**: Spring Boot was chosen for its simplicity, scalability, and integration with the Spring ecosystem. It provides an embedded server (Tomcat), which makes deployment easier, and is widely used for building production-ready, scalable microservices.
- **Why**: Spring Boot accelerates development with its convention-over-configuration principle. It also supports building RESTful APIs with great ease, making it suitable for this project.

### 2. **MongoDB / PostgreSQL (Database)**
- **Choice**: Depending on the project configuration, **MongoDB** or **PostgreSQL** is used for data storage.
    - **MongoDB** is a NoSQL database that offers flexibility in handling unstructured data. It is ideal for scenarios where the schema may evolve frequently.
    - **PostgreSQL** is a relational database management system (RDBMS) and is used when a structured schema with relationships is necessary.
- **Why**: MongoDB is chosen for its scalability and ability to handle large datasets in a flexible manner, while PostgreSQL can be used for structured data with complex relationships.

### 3. **JUnit (Testing)**
- **Choice**: **JUnit** is used for writing unit tests. It is the most widely adopted framework for unit testing Java applications.
- **Why**: JUnit integrates seamlessly with Spring Boot and allows easy creation of tests to ensure that the business logic is functioning as expected.

### 4. **Mockito (Mocking)**
- **Choice**: **Mockito** is used to mock dependencies and simulate behavior for unit tests. It is particularly useful for isolating the service logic from external systems such as databases.
- **Why**: Mockito allows you to mock services like the database repository or user service without needing actual database calls, making unit tests faster and more focused.

### 5. **Lombok (Utility)**
- **Choice**: **Lombok** is used to reduce boilerplate code by automatically generating getter/setter methods, constructors, `toString()`, and other methods.
- **Why**: Lombok reduces the verbosity of Java code, enhancing readability and maintainability.

### 6. **Spring Security (Authentication)**
- **Choice**: **Spring Security** is used for securing the application and handling authentication.
- **Why**: It provides a customizable security framework for handling user authentication, authorization, and session management.

---

## Rate Limiting

### Overview
To ensure fair usage and prevent abuse, the **SpeerNotes** API implements **rate limiting** based on the number of requests a user can make within a given time window.

### Rate Limiting Behavior
- **Max Requests**: Each user is limited to **5 requests per minute**.
- **Throttling**: If a user exceeds the maximum request count (5 requests), they will experience a **500ms delay** before being able to make another request. The response will include a header: `X-RateLimit-Throttled: true`.
- **Hard Block**: If the user exceeds **10 requests** within a minute, their requests will be blocked with a status of `HTTP 416 Range Not Satisfiable`, and an error message will be returned: `"Too many requests. Try again later."`

---

## Running the Application

### Prerequisites
Ensure that you have the following installed on your local machine:
- **Java 17** (or higher)
- **Maven** (for building the project)
- **MongoDB** or **PostgreSQL** (depending on the database you want to use)

### Clone the Repository
```bash
git clone https://github.com/abhiramjanardhan/speerassessment.git
cd speernotes
```

### Configuration

- MongoDB is configured through Atlas and the URL is configured into the application.properties.
- Hence, no further setup is required in that case
- Swagger page is configured into the project and can be accessed through /swagger-ui/index.html
- Rate limiting can be controlled via the following configuration property:
```properties
rate-limiting.enabled=true
```

### Execution

- To build the application, use the following Maven command:
```agsl
mvn clean install
```
- To run the application, use the following Maven command:
```agsl
mvn spring-boot:run
```
- Once the application is running, you can access the API at:
```
http://localhost:9090
```
- Swagger is configured into the project and it is available at:
```agsl
http://localhost:9090/swagger-ui/index.html
```
- To run the unit tests, run the Maven command:
```agsl
mvn test
```

### Project Structure

```agsl
speernotes
│
├── src/main/java
│   ├── com/assessment/speernotes
│       ├── controller           # REST API Controllers
│       ├── exceptions           # Custom Exceptions
│       ├── model                # Entity Models (Note, User, etc.)
│           └── dto              # Data Transfer objects 
│       ├── repository           # Database Repositories
│       ├── requests             # Request Operations Handler
│       ├── service              # Business Logic (Services)
│       └── utils                # Utility Classes (e.g., ConvertorUtil)
│
├── src/test/java
│   └── com/assessment/speernotes
│       ├── service              # Unit Tests for Services
│       ├── config               # Unit Tests for Configuration
│       ├── requests             # Unit Tests for Requests based operations
│       └── controller           # Unit Tests for Controllers
│
├── src/main/resources
│   ├── application.properties      # Configuration file for database and other settings
│   └── application.test.properties # Configuration file for test execution
└── pom.xml                     # Maven build file
```

## API Endpoints

Here are some of the main API endpoints for the PeerNotes application:

- POST /api/notes: Create a new note.
- GET /api/notes: Get all notes for the authenticated user.
- GET /api/notes/{id}: Get a specific note by ID.
- PUT /api/notes/{id}: Update an existing note by ID.
- DELETE /api/notes/{id}: Delete a note by ID.
- POST /api/notes/{id}/share: Share a note with another user.
- GET /api/notes/search?query={query}: Search notes by query.