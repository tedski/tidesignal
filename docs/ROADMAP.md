# TideSignal Roadmap

This document outlines the development roadmap for TideSignal, an offline-first tide prediction app for WearOS.

## Current Status: MVP Complete

The core functionality of TideSignal is complete and ready for beta testing. The app provides accurate tide predictions using NOAA harmonic analysis with a battery-efficient caching strategy.

### Completed Features

**Foundation**
- ✅ WearOS project structure with Kotlin and Jetpack Compose
- ✅ Room database with bundled SQLite (~3,000 NOAA stations)
- ✅ Data pipeline (Python scripts for NOAA data fetching)
- ✅ Dual-mode operation (test/production databases)

**Calculation Engine**
- ✅ 37 NOAA tidal constituent definitions
- ✅ Astronomical calculations (node factors, equilibrium arguments)
- ✅ Harmonic calculator (core tide prediction)
- ✅ 7-day extrema cache with V0 continuity for midnight boundary
- ✅ Newton's method for finding high/low tides
- ✅ Full subordinate station support

**UI Components**
- ✅ Material You theme optimized for WearOS
- ✅ Main tide screen with current height and next extrema
- ✅ Station picker (nearby and browse modes)
- ✅ Location permission handling
- ✅ Tide graph with 24-hour curve and time axis
- ✅ Direction indicator (rising/falling/slack)
- ✅ High/low tide cards
- ✅ Proper back navigation throughout app

**Polish**
- ✅ Standard WearOS TimeText component
- ✅ Circular screen optimization
- ✅ Navigation with BackHandler

## Next Steps

### Pre-Release (Beta)

**Testing & Validation**
- [ ] Unit tests for harmonic calculations
- [ ] NOAA prediction comparison tests
- [ ] Battery profiling (target: <2% per hour)
- [ ] Field testing with physical devices
- [ ] Multiple station coverage validation

**Documentation**
- [ ] User guide
- [ ] Screenshots for Play Store
- [ ] Privacy policy
- [ ] Terms of service

**Infrastructure**
- [ ] GitHub Actions CI/CD
- [ ] Automated APK builds
- [ ] Data update automation (monthly NOAA refresh)

### v1.0 Release

**Core Features**
- [ ] Tile widget (glanceable tide info from watch face)
- [ ] Settings screen refinements
- [ ] About screen with license and attribution
- [ ] In-app tutorial or onboarding

**Optimization**
- [ ] Always-On Display (AOD) mode
- [ ] Reduced update frequency in AOD
- [ ] Battery optimizations
- [ ] APK size optimization

**Distribution**
- [ ] Google Play Store submission
- [ ] Release notes and changelog
- [ ] Marketing materials

## Post-1.0 Features

### Near Term (v1.1-1.2)

**Watch Face Integrations**
- Watch face complications (short text, long text, ranged value)
- Multiple complication variants
- Complication tap actions

**Enhanced Visualizations**
- Tide curve improvements (pinch zoom, pan)
- Multi-day view (calendar of tides)
- Moon phase display (for fishing correlation)

**User Preferences**
- Multiple favorite stations (quick switch)
- Custom notification preferences
- Theme customization (light/dark mode)

### Medium Term (v1.3-1.5)

**Notifications & Alerts**
- Optimal tide window notifications
- Custom alert thresholds (e.g., "notify when tide >5 ft")
- Daily tide summary notifications
- Fishing times based on tide and moon phase

**Data Export**
- Export tide data for trip planning
- Share tide info with friends
- Calendar integration (add tides to calendar)

**Additional Stations**
- International stations (non-NOAA sources)
- Community-submitted stations
- Custom station support (user-provided constituents)

### Long Term (v2.0+)

**Tidal Currents**
- Current predictions (velocity, not height)
- Different calculation method from tides
- Current direction and speed
- Integration with NOAA current stations

**Advanced Features**
- Offline map view for station selection
- Coastline distance algorithm (avoid land routes)
- Historical tide data and logging
- Fishing log integration
- Weather integration (wind, barometric pressure)

**Platform Expansion**
- Android phone companion app
- Tablet support
- Web dashboard for planning

**Community Features**
- User reviews and ratings for stations
- Fishing reports tied to tide conditions
- Community tide observations
- Share fishing spots (with privacy controls)

## Feature Requests

Track feature requests and vote on priorities in [GitHub Issues](https://github.com/yourusername/tidesignal/issues) with the "enhancement" label.

Top community requests:
- Watch face complications
- Tidal current predictions
- Moon phase display
- Fishing time recommendations
- Multi-language support

## Research & Exploration

**Potential Future Directions**
- Machine learning for fishing prediction
- Augmented reality for tidal visualization
- Solar/lunar event tracking (sunrise, sunset)
- Solunar theory integration (fishing times)
- Species-specific fishing recommendations
- Integration with fishing apps (e.g., Fishbrain)

**Technical Improvements**
- Kotlin Multiplatform (share code with phone app)
- Jetpack Compose multiplatform
- Background sync with cloud (optional)
- Advanced battery optimization techniques

## Non-Goals

Features we've intentionally decided NOT to pursue:

- **Cloud sync**: Offline-first architecture is a core principle
- **Social features**: Privacy-focused, no social media integration
- **Ads**: Open-source, GPL-licensed, no advertising
- **Paid features**: All features free and open source
- **Account system**: No user accounts required
- **Location tracking**: Only coarse location, only when requested

## Contributing

We welcome contributions! See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

Priority areas for contribution:
1. Testing and validation
2. UI/UX improvements for small screens
3. Battery optimization
4. Documentation
5. International station support

## Timeline

**Note**: This is a volunteer open-source project. Timelines are estimates and subject to change based on contributor availability.

- **Q1 2026**: MVP complete, beta testing
- **Q2 2026**: v1.0 release on Play Store
- **Q3 2026**: v1.1-1.2 with complications and enhanced features
- **Q4 2026**: v1.3+ with notifications and data export
- **2027+**: v2.0 with tidal currents and platform expansion

## Versioning

TideSignal follows [Semantic Versioning](https://semver.org/):
- **Major** (x.0.0): Breaking changes, major new features
- **Minor** (1.x.0): New features, backward compatible
- **Patch** (1.0.x): Bug fixes, minor improvements

## Feedback

Have ideas for the roadmap? Open an issue or discussion on GitHub!

- [Feature Requests](https://github.com/yourusername/tidesignal/issues/new?labels=enhancement)
- [Discussions](https://github.com/yourusername/tidesignal/discussions)
