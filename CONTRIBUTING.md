# Contributing to TideWatch

Thank you for your interest in contributing to TideWatch! This document provides guidelines for contributing to the project.

## Code of Conduct

Be respectful and constructive. We're building a tool for the maritime community.

## How to Contribute

### Reporting Bugs

Use [GitHub Issues](https://github.com/yourusername/tidewatch/issues/new/choose) to report bugs using our bug report template. The template will guide you through providing:
- Clear description and steps to reproduce
- Expected vs actual behavior
- Device info (watch model, WearOS version)
- Logs and screenshots if applicable

**Security Issues**: For security vulnerabilities, please see [SECURITY.md](SECURITY.md) instead of opening a public issue.

### Suggesting Features

Use our [feature request template](https://github.com/yourusername/tidewatch/issues/new/choose) to suggest enhancements. The template helps you describe:
- The problem this solves
- Your proposed solution
- Use cases and alternatives considered

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Write or update tests
5. Ensure all tests pass (`./gradlew test`)
6. Commit with clear messages
7. Push to your fork
8. Open a Pull Request

### Development Guidelines

**Code Style:**
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex algorithms
- Keep functions focused and small

**Testing:**
- Write unit tests for new calculations
- Validate against NOAA predictions
- Test on physical WearOS device when possible

**Commits:**
- Use clear, descriptive commit messages
- Reference issue numbers when applicable
- Keep commits atomic and focused

### Areas for Contribution

**High Priority:**
- UI/UX improvements for small screens
- Battery optimization
- Test coverage
- Documentation

**Medium Priority:**
- International stations (non-NOAA)
- Tidal currents
- Watch face complications
- Multi-language support

**Nice to Have:**
- Advanced features (moon phase, fishing times)
- Data export
- Offline maps

## Development Setup

See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for comprehensive setup instructions including:
- Environment prerequisites
- Data pipeline usage
- Build commands
- Testing procedures
- Code structure overview

### Running Tests

```bash
# Unit tests
./gradlew test

# Lint
./gradlew lint

# All checks
./gradlew check
```

### Data Pipeline

The data pipeline fetches NOAA station data and builds the bundled database:

```bash
cd tools/data-pipeline

# Recommended: Install uv for easier setup
curl -LsSf https://astral.sh/uv/install.sh | sh

# Test mode (5 stations, ~30 seconds)
./run.sh
cp tides-test.db ../../app/src/main/assets/tides.db

# Production mode (all 3,379 stations, ~45 minutes)
./run.sh --mode production
cp tides.db ../../app/src/main/assets/tides.db
```

See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for detailed data pipeline documentation.

## Documentation

Before contributing, review:
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - System architecture and calculation engine
- **[DEVELOPMENT.md](docs/DEVELOPMENT.md)** - Developer guide and code structure
- **[DESIGN.md](docs/DESIGN.md)** - Full design specification
- **[ROADMAP.md](docs/ROADMAP.md)** - Development roadmap and priorities

## Questions?

Use our [question template](https://github.com/yourusername/tidewatch/issues/new?template=question.md) or start a [GitHub Discussion](https://github.com/yourusername/tidewatch/discussions). We're happy to help!

## License

By contributing, you agree that your contributions will be licensed under GPL-3.0.
