# Changelog

All notable changes to the Clean Generator for Flutter plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-01-25

### Added
- **JSON-based code generation**: Parse JSON request/response and auto-generate Dart models
  - Automatic type inference from JSON values
  - Support for nested objects and arrays
  - Generates `fromJson()` and `toJson()` methods
  - Proper null safety handling
- **HTTP method configuration**: 
  - Dropdown to select GET, POST, PUT, or DELETE for each function
  - Proper Retrofit annotations (`@GET`, `@POST`, `@PUT`, `@DELETE`)
  - Request body handling for POST/PUT operations
  - Query parameter conversion for GET requests
- **Enhanced UI Dialog** (`FeatureDialogNew`):
  - Scrollable function panels for better UX
  - Text areas for JSON input (request/response)
  - HTTP method selector per function
  - Add/remove functions dynamically
- **New action**: "Flutter (JSON Enhanced)" option in context menu
- **Backward compatibility**: Original "Flutter (Legacy)" option still available
- Dependencies: Added `org.json:json:20231013` for JSON parsing

### Fixed
- Missing imports in generated API service (BaseResponse, DataState, etc.)
- Incorrect request body handling for different HTTP methods
- Model generation with proper Dart null safety syntax
- Use case parameter types now properly typed instead of generic placeholders

### Changed
- Plugin version bumped to 1.2.0
- Updated plugin description with new features
- Improved code organization with separate V2 generators
- Enhanced error handling in JSON parsing

### Developer
- Added `JsonParser` utility for JSON-to-Dart conversion
- Created `GeneratorV2` with enhanced generation logic
- Introduced `FunctionConfig` data class for cleaner data flow
- Added comprehensive documentation (README, DEVELOPER_GUIDE)
- Created example file `JsonParserExample.kt` for testing

## [1.1.7] - 2024 (Estimated)

### Added
- Initial stable release
- Basic Clean Architecture folder structure generation
- Cubit-based state management
- API service with Retrofit
- Repository pattern implementation
- Use cases generation
- Dependency injection setup
- Manual model specification

### Features
- Context menu integration: Right-click → New → Clean Gen
- Feature name input
- Multiple function configuration
- API endpoint specification
- Manual model name input

### Dependencies
- IntelliJ Platform 2023.2.6
- Kotlin 1.9.25
- Target IDE: IntelliJ IDEA Community Edition

## [1.0.0] - Unknown

### Added
- Initial plugin structure
- Basic code generation capabilities
- Action group integration

---

## Upcoming Features (Roadmap)

### [1.3.0] - Planned
- [ ] GraphQL support
- [ ] BLoC state management option (alternative to Cubit)
- [ ] Riverpod support
- [ ] Custom template editor
- [ ] Configuration file (.clean-gen.yaml)

### [1.4.0] - Planned
- [ ] Test file generation
- [ ] Mock data generator
- [ ] API documentation generation
- [ ] JSON schema validation
- [ ] Batch generation from OpenAPI/Swagger specs

### [2.0.0] - Future
- [ ] UI redesign with preview panel
- [ ] Multi-project templates
- [ ] Cloud sync for templates
- [ ] Team collaboration features
- [ ] AI-powered code suggestions

---

## Migration Guide

### From 1.1.7 to 1.2.0

**No breaking changes!** The plugin maintains backward compatibility.

**New workflow** (recommended):
1. Use "Flutter (JSON Enhanced)" instead of "Flutter (Legacy)"
2. Paste actual JSON from your API
3. Select appropriate HTTP methods
4. Let the plugin generate typed models automatically

**Benefits**:
- No manual model class creation
- Type-safe request/response handling
- Automatic nested class generation
- Proper JSON serialization

**Old workflow still works**:
- "Flutter (Legacy)" option remains functional
- Existing projects unaffected
- Can migrate gradually

---

## Links

- [Project Repository](https://github.com/naviy/clean-gen)
- [Issue Tracker](https://github.com/naviy/clean-gen/issues)
- [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/XXXXX-clean-generator-for-flutter)
- [Documentation](https://github.com/naviy/clean-gen/wiki)

---

**Legend:**
- `Added` for new features
- `Changed` for changes in existing functionality
- `Deprecated` for soon-to-be removed features
- `Removed` for now removed features
- `Fixed` for any bug fixes
- `Security` for vulnerability fixes
