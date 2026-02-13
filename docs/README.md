# TideWatch Documentation

Welcome to the TideWatch documentation! This directory contains comprehensive guides for users, developers, and contributors.

## Quick Links

- **[Architecture](ARCHITECTURE.md)** - Technical architecture and implementation details
- **[Development Guide](DEVELOPMENT.md)** - Complete developer setup and workflow
- **[Roadmap](ROADMAP.md)** - Project roadmap and future features
- **[Design Specification](DESIGN.md)** - Original design document and comprehensive reference

## Documentation Overview

### For Users

Currently, TideWatch is in beta testing. User documentation will be added before the v1.0 release.

### For Developers

**Getting Started:**
1. Read the [Development Guide](DEVELOPMENT.md) for environment setup
2. Review the [Architecture](ARCHITECTURE.md) to understand the codebase
3. Check the [Roadmap](ROADMAP.md) for planned features

**Key Documents:**
- **[DEVELOPMENT.md](DEVELOPMENT.md)** - Your primary resource for development
  - Environment setup (JDK, Android Studio, Python)
  - Data pipeline usage
  - Building and testing
  - Code structure
  - Adding new features
  - Debugging and profiling

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Deep dive into technical details
  - High-level architecture diagram
  - Data layer (database schema, data pipeline)
  - Calculation engine (harmonic analysis, astronomical calculations)
  - Battery optimization strategy
  - Performance targets

### For Contributors

**First-time Contributors:**
1. Read [CONTRIBUTING.md](../CONTRIBUTING.md) for contribution guidelines
2. Check [GitHub Issues](https://github.com/yourusername/tidewatch/issues) for good first issues

**Areas for Contribution:**
- Testing and validation
- UI/UX improvements
- Battery optimization
- Documentation improvements
- International station support

### Technical Specifications

- **[DESIGN.md](DESIGN.md)** - Original PRD and comprehensive design specification
  - Full project specification
  - Detailed feature requirements
  - Technical implementation details
  - Historical reference document

### Data Pipeline

- **[tools/data-pipeline/README.md](../tools/data-pipeline/README.md)** - Data pipeline documentation
  - NOAA data fetching
  - Database generation
  - Test vs. production modes
  - Command reference

### Project Management

- **[ROADMAP.md](ROADMAP.md)** - Project roadmap and timeline
  - Current status (MVP complete)
  - Upcoming features
  - Post-1.0 plans
  - Non-goals

### Historical Documents

- **[archive/](archive/)** - Archived documentation
  - `IMPLEMENTATION_STATUS_2026-02-11.md` - Historical implementation status snapshot

## Repository Structure

```
tidewatch/
├── app/                      # Android app source code
│   ├── src/main/kotlin/      # Kotlin source files
│   ├── src/main/assets/      # Bundled database
│   └── build.gradle.kts      # App build configuration
├── tools/                    # Build and development tools
│   └── data-pipeline/        # Python scripts for NOAA data
├── docs/                     # Documentation (you are here)
│   ├── README.md             # This file
│   ├── ARCHITECTURE.md       # Technical architecture
│   ├── DEVELOPMENT.md        # Developer guide
│   ├── ROADMAP.md            # Project roadmap
│   ├── DESIGN.md             # Design specification
│   └── archive/              # Historical docs
├── .github/                  # GitHub configuration
│   ├── ISSUE_TEMPLATE/       # Issue templates
│   └── PULL_REQUEST_TEMPLATE.md
├── README.md                 # Project README
├── CONTRIBUTING.md           # Contribution guidelines
├── SECURITY.md               # Security policy
├── LICENSE                   # GPL-3.0 license
└── CLAUDE.md                 # Claude Code instructions

```

## External Resources

### NOAA Tide Data
- [NOAA CO-OPS API Documentation](https://api.tidesandcurrents.noaa.gov/api/prod/)
- [Harmonic Analysis of Tides (Schureman, 1958)](https://tidesandcurrents.noaa.gov/publications/)
- [NOAA Tides & Currents](https://tidesandcurrents.noaa.gov/)

### WearOS Development
- [WearOS Design Guidelines](https://developer.android.com/training/wearables/design)
- [Jetpack Compose for WearOS](https://developer.android.com/training/wearables/compose)
- [WearOS Developer Guide](https://developer.android.com/training/wearables)

### Android Development
- [Android Developer Guides](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)

## Asking Questions

- **General Questions**: Open a [GitHub Discussion](https://github.com/yourusername/tidewatch/discussions)
- **Bug Reports**: Use [GitHub Issues](https://github.com/yourusername/tidewatch/issues) with bug report template
- **Feature Requests**: Use [GitHub Issues](https://github.com/yourusername/tidewatch/issues) with feature request template
- **Security Issues**: See [SECURITY.md](../SECURITY.md) for responsible disclosure

## Contributing to Documentation

Documentation improvements are welcome! If you find errors, unclear sections, or missing information:

1. Open an issue describing the problem
2. Or submit a PR with your improvements
3. Follow the same contribution guidelines as code contributions

**Documentation Style Guide:**
- Use clear, concise language
- Include code examples where helpful
- Add links to related documentation
- Keep formatting consistent
- Test all commands and code snippets

## License

All documentation is licensed under the same GPL-3.0 license as the project code.

See [LICENSE](../LICENSE) for details.
