# Contributing to Spockk

Thank you for considering contributing to Spockk!

## Getting Started

Before starting work on a significant change, please [open an issue](https://github.com/pshevche/spockk/issues) to discuss your proposal with the maintainers.

## Development Setup

### Prerequisites

- **JDK 21**: Set the `JDK21` environment variable to the JDK path to make it discoverable by Gradle's toolchains.
- **IntelliJ IDEA**: While any IDE with Kotlin support works, working on Spockk specifications may be tricky, as the IDE support is only provided via the Spockk IntelliJ plugin

### Setup Steps

1. Fork and clone the repository:
   ```bash
   git clone https://github.com/YOUR-USERNAME/spockk.git
   cd spockk
   ```

2. Import the project into IntelliJ IDEA (Gradle configuration will be detected automatically)

## Building and Testing

### Build the project

```bash
./gradlew build
```

This compiles the code, runs all tests, and performs code quality checks.

### Run tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :spockk-compiler-plugin:test
```

### Format code

The project uses Spotless with ktlint for code formatting:

```bash
# Check formatting
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply
```

Code style is enforced by `.editorconfig` (2-space indentation, LF line endings).

### Testing the IntelliJ Plugin

To test your changes to the IntelliJ plugin:

1. Build the plugin:
   ```bash
   ./gradlew :spockk-intellij-plugin:build
   ```

2. Install in IntelliJ IDEA:
   - Navigate to **Settings → Plugins → ⚙️ → Install Plugin from Disk**
   - Select `spockk-intellij-plugin/build/libs/spockk-intellij-plugin-<version>.jar`
   - Restart IntelliJ IDEA

## Submitting Changes

1. Create a feature branch:
   ```bash
   git checkout -b feature/my-feature
   ```

2. Make your changes and commit with clear messages

3. Ensure all checks pass:
   ```bash
   ./gradlew build
   ```

4. Push to your fork and open a Pull Request with:
   - A clear description of the changes
   - Reference to related issues (e.g., "Fixes #123")

## Project Structure

- **[`spockk-compiler-plugin`](spockk-compiler-plugin/README.adoc)**: IR transformations for Kotlin compiler
- **[`spockk-core`](spockk-core/README.adoc)**: Kotlin-specific specification syntax
- **[`spockk-gradle-plugin`](spockk-gradle-plugin/README.adoc)**: Gradle plugin for applying the compiler plugin
- **[`spockk-intellij-plugin`](spockk-intellij-plugin/README.adoc)**: IntelliJ IDEA integration
- **[`spockk-specs`](spockk-specs/README.adoc)**: Framework test specifications
- **[`spockk-docs`](spockk-docs/README.adoc)**: User guide documentation

## Resources

- [User Guide](https://pshevche.github.io/spockk/)
- [Example Project](https://github.com/pshevche/spockk-example)
