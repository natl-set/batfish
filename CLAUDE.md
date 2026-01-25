# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Batfish is a network validation tool that provides correctness guarantees for security, reliability, and compliance by analyzing network device configurations. It builds complete models of network behavior from device configurations and finds violations of network policies.

**Key Features:**
- Pre-deployment validation of network configurations
- Multi-vendor support (Cisco, Juniper, Arista, AWS, Azure, F5, Palo Alto, etc.)
- No direct device access required - only needs device configurations
- Policy-based validation for security, reliability, and compliance

## Build System & Commands

This project uses Bazel as the build system (not Maven). Java 17+ is required.

### Essential Commands

```bash
# Build all targets
bazel build //...

# Run all tests
bazel test //...

# Run Batfish locally (development server)
./tools/bazel_run.sh

# Run with debug mode (JDWP debugging on port 5009)
./tools/bazel_run.sh -d

# Format Java code
./tools/fix_java_format.sh --replace

# Run Checkstyle
./tools/run_checkstyle.sh

# Format Bazel build files
bazel --noblock_for_lock run //:buildifier.fix
```

### Testing Commands

```bash
# Run specific test target
bazel test //projects/batfish:BatfishTest

# Run tests with coverage
bazel test --config=coverage //...

# Run Python tests (for Pybatfish client)
cd python && python -m pytest
```

## Architecture Overview

### Core Components

1. **Configuration Parser**: ANTLR-based parsers for each vendor's configuration format
2. **Network Model**: Complete representation of network topology and configurations
3. **Analysis Engine**: Uses Binary Decision Diagrams (BDDs) for network behavior modeling
4. **Query Interface**: REST API for querying network behavior and policies
5. **Pybatfish SDK**: Python client for programmatic access

### Project Structure

The codebase contains 11 main projects under `/projects/`:
- `allinone` - Main entry point combining all components
- `batfish` - Core network analysis engine
- `bdd` - Binary Decision Diagram implementation
- `symbolic` - Symbolic analysis capabilities
- `common` - Shared utilities and libraries
- `coordinator` - Service coordination
- `minesweeper` - Configuration analysis
- `question` - Query processing
- `client` - Client libraries

### Technology Stack

- **Primary language**: Java 17+
- **Build system**: Bazel 8.5.1
- **Parser generator**: ANTLR for network configuration parsing
- **Python client**: Pybatfish SDK (Python 3.10+)
- **Key libraries**: Jackson (JSON), Jersey (REST), Guava, BDD (Binary Decision Diagrams)

## Development Workflow

### Pre-commit Hooks

The project uses pre-commit hooks for code quality:

```bash
# Install pre-commit hooks
pre-commit install

# Run hooks manually
pre-commit run --all-files
```

Hooks include:
- Java formatting (Google Java Style)
- Bazel build file formatting (buildifier)
- Python formatting (Black, isort, autoflake)

### Git Workflow

Follows structured commit messages:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type** options: feat, fix, docs, style, refactor, perf, test, chore

### Docker Deployment

```bash
# Pull and run
docker pull batfish/allinone
docker run --name batfish -v batfish-data:/data -p 8888:8888 -p 9997:9997 -p 9996:9996 batfish/allinone
```

## Development Standards

### Java Coding Standards

- Google Java Style Guide (2-space indentation, 100 char line limit)
- Public APIs require Javadoc comments
- Use `@Nullable` and `@Nonnull` annotations
- One top-level class per file
- Prefer composition over inheritance

### Testing Standards

- Unit tests for all non-trivial code
- Arrange-Act-Assert pattern for tests
- Reference tests for parsing logic
- Mock external dependencies
- Test both normal and edge cases

### Performance Considerations

- Be mindful of memory usage for large networks
- Avoid unnecessary object creation
- Use efficient algorithms and data structures
- Profile code to identify bottlenecks

## Key Configuration Files

- `WORKSPACE` - Bazel workspace configuration
- `MODULE.bazel` - Module dependencies (70+ Maven artifacts)
- `.pre-commit-config.yaml` - Code quality hooks
- `tools/log4j2.yaml` - Logging configuration
- `questions/` - Query templates directory

## Important Notes

- Batfish does NOT require direct access to network devices
- Analysis requires only device configurations
- Can enhance analysis with BGP routes and topology information
- Built for enterprise-grade network validation
- Comprehensive CI/CD with GitHub Actions