# Clean Generator for Flutter - IntelliJ Plugin

An IntelliJ IDEA plugin that automates the generation of Clean Architecture boilerplate code for Flutter projects with Cubit state management.

## Version 1.2.0 - New Features 🎉

### JSON-Based Code Generation
- **Automatic Model Generation**: Parse JSON request/response bodies and generate complete Dart model classes
- **Type-Safe Models**: Generates models with proper types, constructors, `fromJson()`, and `toJson()` methods
- **Nested Class Support**: Handles nested objects and lists automatically
- **Null Safety**: Generated code follows Dart 3.x null safety standards

### HTTP Method Configuration
- **Multiple HTTP Methods**: Support for GET, POST, PUT, and DELETE
- **Request Body Handling**: Automatic handling of request bodies for POST/PUT operations
- **Query Parameters**: GET requests with parameters are converted to query parameters
- **Retrofit Annotations**: Correct HTTP method annotations in generated API service

## Features

### What Gets Generated?
- **Data Layer**
  - API Service with Retrofit annotations
  - Request/Response model classes from JSON
  - Repository implementation
- **Domain Layer**
  - Repository interfaces
  - Use cases
- **Presentation Layer**
  - Cubit state management
  - State classes
- **Dependency Injection**
  - Complete DI setup file

### Folder Structure
```
feature_name/
├── data/
│   ├── data_sources/
│   │   └── feature_api_service.dart
│   ├── models/
│   │   ├── function_request.dart
│   │   └── function_response.dart
│   └── repositories/
│       └── feature_repository_impl.dart
├── domain/
│   ├── repositories/
│   │   └── feature_repository.dart
│   └── use_cases/
│       └── function_use_case.dart
├── presentation/
│   ├── manager/
│   │   ├── feature_cubit.dart
│   │   └── feature_state.dart
│   ├── pages/
│   └── widgets/
└── feature_di.dart
```

## Installation

1. Download the plugin from JetBrains Marketplace
2. Install in IntelliJ IDEA / Android Studio
3. Restart IDE

## Usage

### Method 1: Flutter (JSON Enhanced) - **Recommended**

1. Right-click on the target directory
2. Navigate to **New** → **Clean Gen** → **Flutter (JSON Enhanced)**
3. In the dialog:
   - Enter **Feature Name** (e.g., "auth", "user", "product")
   - Click **+** to add a function
   - For each function:
     - **Function Name**: e.g., "login", "getUsers", "createProduct"
     - **API Endpoint**: e.g., "/auth/login", "/users", "/products"
     - **HTTP Method**: Select GET, POST, PUT, or DELETE
     - **Request JSON**: Paste your request body JSON (optional for GET)
     - **Response JSON**: Paste your expected response JSON
   - Click **+** to add more functions
   - Click **OK** to generate

### Method 2: Flutter (Legacy)
- Original functionality for backward compatibility
- Manual model specification

## Example

### Input Configuration

**Feature Name:** `auth`

**Function 1:**
- **Name:** `login`
- **Endpoint:** `/auth/login`
- **Method:** `POST`
- **Request JSON:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response JSON:**
```json
{
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com"
  }
}
```

### Generated Output

**LoginRequest Model:**
```dart
class User {
  final int id;
  final String name;
  final String email;

  User({
    required this.id,
    required this.name,
    required this.email,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int? ?? 0,
      name: json['name'] as String? ?? "",
      email: json['email'] as String? ?? "",
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
    };
  }
}

class LoginRequest {
  final String email;
  final String password;

  LoginRequest({
    required this.email,
    required this.password,
  });

  factory LoginRequest.fromJson(Map<String, dynamic> json) {
    return LoginRequest(
      email: json['email'] as String? ?? "",
      password: json['password'] as String? ?? "",
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'email': email,
      'password': password,
    };
  }
}
```

**API Service:**
```dart
@RestApi()
abstract class AuthApiService {
  factory AuthApiService(Dio dio, {String baseUrl}) = _AuthApiService;
  
  @POST('/auth/login')
  Future<HttpResponse<LoginResponse>> login(@Body() LoginRequest request);
}
```

## Requirements

### Flutter Project Dependencies
Add these packages to your `pubspec.yaml`:

```yaml
dependencies:
  cubit_base: ^latest_version  # State management base
  dio: ^5.0.0                   # HTTP client
  retrofit: ^4.0.0              # REST API client
  
dev_dependencies:
  retrofit_generator: ^8.0.0
  build_runner: ^2.4.0
```

### After Generation
Run build_runner to generate Retrofit code:
```bash
flutter pub run build_runner build --delete-conflicting-outputs
```

## Configuration Tips

### JSON Best Practices
- Use actual API response examples for accurate type inference
- Include all possible fields in your JSON
- For arrays, include at least one item for type detection

### Naming Conventions
- **Feature names**: lowercase, snake_case (e.g., "user_profile")
- **Function names**: camelCase (e.g., "getUserProfile")
- **API endpoints**: with leading slash (e.g., "/api/v1/users")

## Troubleshooting

### Models not generating correctly?
- Verify your JSON is valid (use a JSON validator)
- Ensure JSON objects have at least one property
- For arrays, include at least one sample item

### Import errors after generation?
- Run `build_runner` to generate Retrofit code
- Check that all dependencies are in `pubspec.yaml`
- Run `flutter pub get`

### Compilation errors?
- Ensure `cubit_base` package is properly configured
- Check that BaseState, DataState, UseCase classes are available
- Verify your dependency injection setup (locator)

## Changelog

### Version 1.2.0 (January 2026)
- ✨ JSON-based model generation
- ✨ HTTP method configuration (GET, POST, PUT, DELETE)
- ✨ Enhanced UI with scrollable function panels
- ✨ Automatic type inference from JSON
- ✨ Support for nested objects and arrays
- 🐛 Fixed missing imports in generated code
- 🐛 Fixed null safety issues

### Version 1.1.7
- Initial stable release
- Basic Clean Architecture generation
- Cubit state management support

## Contributing

Found a bug or have a feature request? Please open an issue on our GitHub repository.

## License

MIT License - see LICENSE file for details

## Author

**Naviy**
- Email: support@naviy.com
- Website: https://www.naviy.uz

## Support

For issues and feature requests, please visit our GitHub repository or contact support@naviy.com
