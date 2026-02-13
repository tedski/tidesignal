# Security Policy

## Supported Versions

TideWatch is currently in active development. Security updates will be provided for:

| Version | Supported          |
| ------- | ------------------ |
| main    | :white_check_mark: |

## Reporting a Vulnerability

We take the security of TideWatch seriously. If you discover a security vulnerability, please follow responsible disclosure practices:

### How to Report

**Do not** open a public GitHub issue for security vulnerabilities.

Instead, please report security issues through one of these channels:

1. **GitHub Security Advisories** (Preferred):
   - Go to the [Security tab](https://github.com/yourusername/tidewatch/security/advisories)
   - Click "Report a vulnerability"
   - Provide detailed information about the vulnerability

2. **Private Contact**:
   - If you're unable to use GitHub Security Advisories, please contact the maintainers directly
   - Encrypt sensitive information if possible

### What to Include

When reporting a vulnerability, please include:

- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact
- Suggested fix (if you have one)
- Your contact information for follow-up

### Response Timeline

- **Initial Response**: Within 48 hours of report
- **Status Update**: Within 7 days with assessment and planned fix timeline
- **Fix Release**: Depends on severity, but critical issues will be prioritized

### Disclosure Policy

- We ask that you give us reasonable time to fix the vulnerability before public disclosure
- We will acknowledge your contribution in the security advisory (unless you prefer to remain anonymous)
- We will coordinate the disclosure timeline with you

## Security Considerations

TideWatch is designed with security and privacy in mind:

### Data Privacy
- **No network tracking**: The app does not send usage data, analytics, or personal information
- **No accounts**: No user authentication or account creation required
- **Local-only**: All data processing happens on-device
- **Public data**: Tide data is from NOAA public domain sources

### Permissions
- **Location**: Optional, only used for finding nearby tide stations
- **Storage**: Required for bundled tide database (read-only after installation)

### Known Limitations
- The app bundles a pre-built SQLite database. While we validate the data pipeline, users should verify critical tide information against official NOAA sources
- Tide predictions are for informational purposes only and should not be used as the sole basis for navigation or safety decisions

## Third-Party Dependencies

TideWatch uses standard Android/WearOS libraries and dependencies managed through Gradle. We:

- Keep dependencies up to date
- Monitor security advisories for used libraries
- Use dependency scanning tools (planned for CI/CD)

## Best Practices

If you're contributing to TideWatch:

- Follow secure coding practices
- Validate all user inputs
- Use parameterized database queries (Room handles this)
- Avoid logging sensitive information
- Keep dependencies updated
- Review the [CONTRIBUTING.md](CONTRIBUTING.md) guidelines

## Security Updates

Security updates will be:

- Released as soon as possible after verification
- Documented in commit messages and release notes
- Announced through GitHub releases

Thank you for helping keep TideWatch secure!
