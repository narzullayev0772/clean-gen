# Developer Guide - Clean Generator V2

## Architecture Overview

### New Components (V1.2.0)

#### 1. **FeatureDialogNew.kt**
Enhanced UI dialog with:
- HTTP method selection dropdown (GET, POST, PUT, DELETE)
- Text areas for JSON request/response input
- Scrollable function panels for multiple configurations
- Data class `FunctionConfig` to encapsulate function metadata

#### 2. **JsonParser.kt**
JSON parsing utility that:
- Parses JSON strings into Dart class structures
- Generates complete Dart models with:
  - Proper field types (String, int, double, bool)
  - Null safety support
  - Nested class handling
  - List/Array support
  - `fromJson()` factory constructor
  - `toJson()` method
- Handles complex nested objects and arrays

#### 3. **GeneratorV2.kt**
Enhanced code generator that:
- Generates API service with correct HTTP annotations
- Creates repository/repository implementation with proper types
- Generates use cases with request/response types from JSON
- Creates Cubit/State with typed state management
- Handles request bodies for POST/PUT operations
- Converts GET parameters to query parameters

#### 4. **ActionGenerateFlutterV2.kt**
New action handler that:
- Uses FeatureDialogNew for input
- Coordinates model generation from JSON
- Generates all Clean Architecture layers
- Formats and optimizes imports automatically

## Code Generation Flow

```
User Input (Dialog)
    ↓
FunctionConfig (data class)
    ↓
JSON Parser → DartClass models
    ↓
GeneratorV2 → Dart files
    ↓
File System + Code Formatting
```

## JSON Parsing Algorithm

### Type Inference Rules
1. **Primitives**: String, int, double, bool → Direct mapping
2. **Objects**: JSONObject → Nested class
3. **Arrays**: JSONArray → List<T>
4. **Null**: JSONObject.NULL → dynamic with nullable flag

### Example Transformation

**Input JSON:**
```json
{
  "user_name": "John",
  "age": 30,
  "tags": ["dev", "kotlin"],
  "profile": {
    "bio": "Developer"
  }
}
```

**Generated Dart:**
```dart
class Profile {
  final String bio;
  Profile({required this.bio});
  factory Profile.fromJson(Map<String, dynamic> json) => Profile(bio: json['bio']);
  Map<String, dynamic> toJson() => {'bio': bio};
}

class User {
  final String userName;
  final int age;
  final List<String> tags;
  final Profile profile;
  // ... constructors and methods
}
```

## HTTP Method Handling

### GET Requests
- No request body in Retrofit
- Parameters become `@Query()` annotations
- Example: `login(email, password)` → `@Query('email') String? email`

### POST/PUT/DELETE Requests
- Request body becomes `@Body()` parameter
- Full request model class used
- Example: `login(request)` → `@Body() LoginRequest request`

## Extension Points

### Adding New HTTP Methods
1. Add to `HttpMethod` enum in [FeatureDialogNew.kt](src/main/kotlin/com/naviy/uz/clean_gen/ui/FeatureDialogNew.kt)
2. Update `generateApiMethods()` in [GeneratorV2.kt](src/main/kotlin/com/naviy/uz/clean_gen/generator/GeneratorV2.kt)

### Custom Type Mappings
Modify `inferDartType()` in [JsonParser.kt](src/main/kotlin/com/naviy/uz/clean_gen/generator/JsonParser.kt):
```kotlin
private fun inferDartType(value: Any): String {
    return when (value) {
        is String -> "String"
        is Int -> "int"
        // Add custom types here
        else -> "dynamic"
    }
}
```

### Template Customization
Update template methods in `GeneratorV2`:
- `generateApiService()` - API service template
- `generateRepository()` - Repository interface
- `generateCubit()` - Cubit state management
- etc.

## Testing

### Manual Testing Checklist
- [ ] Simple object with primitives
- [ ] Nested objects
- [ ] Arrays of primitives
- [ ] Arrays of objects
- [ ] Mixed nested structures
- [ ] All HTTP methods (GET, POST, PUT, DELETE)
- [ ] Empty JSON handling
- [ ] Invalid JSON handling

### Test JSON Examples
Located in [JsonParserExample.kt](src/main/kotlin/com/naviy/uz/clean_gen/generator/JsonParserExample.kt)

## Building the Plugin

```bash
# Build
./gradlew buildPlugin

# Run in IDE
./gradlew runIde

# Publish (requires credentials)
./gradlew publishPlugin
```

## Common Issues & Solutions

### Issue: Import errors in generated code
**Solution**: Ensure `org.json:json` dependency is in build.gradle.kts

### Issue: Dialog not showing
**Solution**: Check ActionGroup visibility logic and PSI element availability

### Issue: Type inference incorrect
**Solution**: Verify JSON structure, ensure arrays have at least one item

## Future Enhancements

### Planned Features
- [ ] GraphQL support
- [ ] Multiple state management options (BLoC, Riverpod)
- [ ] Custom template editor
- [ ] JSON schema validation
- [ ] API documentation generation
- [ ] Test file generation
- [ ] Mock data generation

### Code Improvements
- [ ] Unit tests for JsonParser
- [ ] Integration tests for full generation flow
- [ ] Error handling improvements
- [ ] Performance optimization for large JSON
- [ ] Configuration file support (.clean-gen.yaml)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions focused and small
- Use data classes for DTOs

## Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Flutter Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Cubit State Management](https://pub.dev/packages/cubit_base)
