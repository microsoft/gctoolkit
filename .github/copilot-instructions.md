# GitHub Copilot Instructions for GCToolKit

## Project Overview
GCToolKit is a Java-based toolkit for analyzing Garbage Collection logs from various JVM implementations. The project uses Maven as its build system and is structured as a multi-module project.

## Architecture & Design Patterns
- **Multi-module Maven project** with clear separation of concerns
- **Event-driven architecture** using Vert.x for message passing
- **Parser framework** for processing various GC log formats
- **Channel-based communication** between parsers and event consumers
- **Verticle pattern** for asynchronous processing with Vert.x

## Module Structure
- `api/` - Core API definitions, interfaces, and base classes
- `parser/` - GC log parsing implementations for different JVM types
- `vertx/` - Vert.x-based messaging and event handling infrastructure
- `sample/` - Example usage and sample applications
- `IT/` - Integration tests
- `gclogs/` - Test data and sample GC logs

## Key Technologies
- **Java 21+** with module system (module-info.java files)
- **Vert.x 5.0.2** for reactive programming and event bus
- **JUnit 5** for testing
- **Maven** for build management
- **Spotbugs, PMD, Checkstyle** for code quality

## Code Style & Conventions
- Follow standard Java naming conventions
- Use proper JavaDoc for public APIs
- Implement equals/hashCode when appropriate
- Use logging with java.util.logging
- Handle exceptions appropriately with try-catch blocks
- Use Promise/Future patterns for asynchronous operations with Vert.x

## Common Patterns in This Codebase

### Event Processing
```java
public void start(Promise<Void> promise) {
    vertx.eventBus().<EventType>consumer(inbox, message -> {
        processor.receive(message.body());
    }).completion()
      .onComplete(ar -> promise.complete());
}
```

### Channel Implementation
- Extend appropriate base channel classes
- Implement proper lifecycle management (open/close)
- Use Vert.x deployVerticle for async operations
- Handle deployment IDs for proper cleanup

### Error Handling
- Use java.util.logging.Logger for logging
- Log warnings/errors with proper context
- Handle Future completion with onComplete callbacks

## Vert.x Specific Guidelines
- Use Verticles for isolated processing units
- Deploy verticles asynchronously with completion handlers
- Use the event bus for inter-verticle communication
- Properly manage deployment IDs to avoid double-undeploy issues
- Handle Promise<Void> completion in start() methods

## Testing Conventions
- Integration tests go in the `IT/` module
- Unit tests use JUnit 5
- Test GC log files are stored in `gclogs/` with organized subdirectories
- Mock external dependencies appropriately

## Build & Dependencies
- Maven 3.9.11+ required
- Use the Maven Wrapper (i.e., `mvnw`)
- Keep dependencies up to date, unless you see a comment saying why not to
- Use dependencyManagement in parent POM for version consistency
- Include proper test scopes for test dependencies

## When Suggesting Code Changes
1. Consider the event-driven nature of the architecture
2. Ensure proper async handling with Vert.x patterns
3. Maintain compatibility with existing interfaces
4. Add appropriate logging and error handling
5. Follow the established module boundaries
6. Consider performance implications for GC log processing
7. Ensure proper resource cleanup (channels, verticles, etc.)

## Common Issues to Avoid
- Double-undeployment of Vert.x verticles
- Blocking operations in Vert.x event loops
- Memory leaks from unclosed channels or resources
- Incorrect Promise/Future completion handling
- Missing error handling in async operations

## File Naming Patterns
- Verticles: `*Verticle.java`
- Channels: `*Channel.java`
- Events: `*Event.java`
- Tests: `*Test.java`
- Integration tests: `*IT.java` or in IT module
