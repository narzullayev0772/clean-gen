# Quick Start Guide - Clean Generator v1.2.0

## 5-Minute Tutorial

### Step 1: Install Plugin
1. Open IntelliJ IDEA or Android Studio
2. Go to **Settings/Preferences** → **Plugins**
3. Search for "Clean Generator for Flutter"
4. Click **Install** and restart IDE

### Step 2: Prepare Your Project
Add required dependencies to `pubspec.yaml`:

```yaml
dependencies:
  cubit_base: ^1.0.0  # Or latest version
  dio: ^5.0.0
  retrofit: ^4.0.0
  
dev_dependencies:
  retrofit_generator: ^8.0.0
  build_runner: ^2.4.0
```

Run: `flutter pub get`

### Step 3: Generate Code

#### Example: User Authentication Feature

1. **Right-click** on your project's `lib` folder
2. Select **New** → **Clean Gen** → **Flutter (JSON Enhanced)**
3. Fill in the dialog:

**Feature Name:** `auth`

**Function 1: Login**
- **Function Name:** `login`
- **API Endpoint:** `/api/v1/auth/login`
- **HTTP Method:** `POST`
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
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com",
    "avatar_url": "https://example.com/avatar.jpg"
  },
  "expires_in": 3600
}
```

Click **+** to add another function:

**Function 2: Register**
- **Function Name:** `register`
- **API Endpoint:** `/api/v1/auth/register`
- **HTTP Method:** `POST`
- **Request JSON:**
```json
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "password123",
  "password_confirmation": "password123"
}
```
- **Response JSON:**
```json
{
  "message": "Registration successful",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com"
  }
}
```

4. Click **OK**

### Step 4: Generated Structure

You'll see this folder structure:

```
lib/
└── auth/
    ├── data/
    │   ├── data_sources/
    │   │   └── auth_api_service.dart
    │   ├── models/
    │   │   ├── login_request.dart
    │   │   ├── login_response.dart
    │   │   ├── register_request.dart
    │   │   └── register_response.dart
    │   └── repositories/
    │       └── auth_repository_impl.dart
    ├── domain/
    │   ├── repositories/
    │   │   └── auth_repository.dart
    │   └── use_cases/
    │       ├── login_use_case.dart
    │       └── register_use_case.dart
    ├── presentation/
    │   ├── manager/
    │   │   ├── auth_cubit.dart
    │   │   └── auth_state.dart
    │   ├── pages/
    │   └── widgets/
    └── auth_di.dart
```

### Step 5: Generate Retrofit Code

Run build_runner:
```bash
flutter pub run build_runner build --delete-conflicting-outputs
```

This generates the `auth_api_service.g.dart` file.

### Step 6: Setup Dependency Injection

In your main DI file (e.g., `injection_container.dart`):

```dart
import 'auth/auth_di.dart';

Future<void> init() async {
  // Your existing DI setup
  
  // Add auth feature
  await authDI();
}
```

### Step 7: Use in Your UI

```dart
import 'package:flutter_bloc/flutter_bloc.dart';
import 'auth/presentation/manager/auth_cubit.dart';

class LoginPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => locator<AuthCubit>(),
      child: BlocConsumer<AuthCubit, AuthState>(
        listener: (context, state) {
          state.loginState.whenOrNull(
            success: (data) {
              // Navigate to home
              Navigator.pushReplacementNamed(context, '/home');
            },
            error: (error) {
              // Show error
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text(error)),
              );
            },
          );
        },
        builder: (context, state) {
          return Scaffold(
            body: Column(
              children: [
                TextField(/* email field */),
                TextField(/* password field */),
                ElevatedButton(
                  onPressed: () {
                    final request = LoginRequest(
                      email: emailController.text,
                      password: passwordController.text,
                    );
                    context.read<AuthCubit>().login(request);
                  },
                  child: state.loginState.maybeWhen(
                    loading: () => CircularProgressIndicator(),
                    orElse: () => Text('Login'),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
```

## Common Use Cases

### GET Request with Parameters

**Function Name:** `getUsers`  
**Endpoint:** `/api/v1/users`  
**Method:** `GET`  
**Request JSON:**
```json
{
  "page": 1,
  "per_page": 10,
  "search": "john"
}
```

Generates query parameters: `?page=1&per_page=10&search=john`

### DELETE Request

**Function Name:** `deleteUser`  
**Endpoint:** `/api/v1/users/123`  
**Method:** `DELETE`  
**Request JSON:** (leave empty)  
**Response JSON:**
```json
{
  "message": "User deleted successfully"
}
```

### PUT Request (Update)

**Function Name:** `updateProfile`  
**Endpoint:** `/api/v1/profile`  
**Method:** `PUT`  
**Request JSON:**
```json
{
  "name": "John Updated",
  "bio": "New bio",
  "avatar_url": "https://example.com/new-avatar.jpg"
}
```

## Pro Tips

### 1. Use Real API Responses
Copy actual JSON from your API documentation or Postman for accurate model generation.

### 2. Handle Arrays
Always include at least one item in JSON arrays:
```json
{
  "items": [
    {"id": 1, "name": "Item 1"}
  ]
}
```

### 3. Nested Objects
The plugin automatically creates nested classes:
```json
{
  "user": {
    "profile": {
      "bio": "Developer"
    }
  }
}
```
Generates: `User` class with nested `Profile` class.

### 4. Optional Fields
For optional fields that might be null, the plugin infers nullable types.

### 5. Multiple Features
Generate separate features for different API domains:
- `auth` - Authentication
- `user` - User management
- `product` - Products
- `order` - Orders

Each feature gets its own folder structure.

## Troubleshooting

### Error: "Couldn't create directory"
**Solution:** Directory already exists. Choose a different feature name or delete existing folder.

### Error: Retrofit generation failed
**Solution:** 
1. Check `pubspec.yaml` has all dependencies
2. Run `flutter clean`
3. Run `flutter pub get`
4. Run `build_runner` again

### Warning: Import not found
**Solution:** Ensure `cubit_base` package exports required classes (BaseState, DataState, UseCase, etc.)

### Models not parsing correctly
**Solution:** Validate your JSON at [jsonlint.com](https://jsonlint.com)

## Next Steps

1. **Customize UI**: Add pages and widgets in `presentation/pages/` and `presentation/widgets/`
2. **Add Business Logic**: Extend use cases with additional validation
3. **Error Handling**: Customize error messages in repository implementation
4. **Testing**: Write tests for your use cases and cubits
5. **Documentation**: Document your API endpoints and models

## Resources

- [Cubit Base Documentation](https://pub.dev/packages/cubit_base)
- [Retrofit Documentation](https://pub.dev/packages/retrofit)
- [Clean Architecture Guide](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Plugin GitHub](https://github.com/naviy/clean-gen)

## Support

Questions? Issues? 
- GitHub Issues: [Report a bug](https://github.com/naviy/clean-gen/issues)
- Email: support@naviy.com

---

**Happy Coding! 🚀**
