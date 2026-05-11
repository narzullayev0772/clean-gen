# Migration Guide: v1.1.7 → v1.2.0

## Overview

Version 1.2.0 introduces **JSON-based code generation** and **HTTP method configuration**. This guide helps you understand the changes and migrate existing projects.

## What's New?

### ✅ Backward Compatible
- Your existing code continues to work
- Legacy generator ("Flutter (Legacy)") still available
- No breaking changes to generated code structure

### 🆕 New Features
1. **JSON-based model generation** - No more manual model creation
2. **HTTP method selection** - Configure GET, POST, PUT, DELETE per function
3. **Enhanced dialog** - Better UX with scrollable panels and JSON input areas

## Comparison

### Old Way (v1.1.7)

**Input:**
- Feature name: `auth`
- Function names: `login`, `register`
- API points: `/auth/login`, `/auth/register`
- Model names: `LoginResponse`, `RegisterResponse`

**Manual Work Required:**
1. Create model classes manually
2. Add `fromJson` and `toJson` manually
3. Guess proper types
4. Handle nested objects manually
5. HTTP method hardcoded as GET in template

### New Way (v1.2.0)

**Input:**
- Feature name: `auth`
- Function 1:
  - Name: `login`
  - Endpoint: `/auth/login`
  - Method: `POST` ← **New!**
  - Request JSON: `{"email":"...","password":"..."}` ← **New!**
  - Response JSON: `{"token":"...","user":{...}}` ← **New!**

**Auto-Generated:**
1. ✅ Complete model classes with proper types
2. ✅ `fromJson` and `toJson` methods
3. ✅ Nested class handling
4. ✅ Proper HTTP annotations
5. ✅ Request body handling

## Migration Strategies

### Strategy 1: Keep Using Legacy (No Migration)

**When to use:**
- Happy with current workflow
- Already have custom models
- Small projects with few endpoints

**How:**
- Continue using "Flutter (Legacy)" option
- No changes required
- Your existing code works as-is

### Strategy 2: New Features for New Code

**When to use:**
- Adding new features to existing project
- Want to try new workflow
- Gradual adoption

**How:**
1. Use "Flutter (Legacy)" for existing features
2. Use "Flutter (JSON Enhanced)" for new features
3. Both work side-by-side

**Example:**
```
lib/
├── auth/              # Generated with v1.1.7 (Legacy)
│   └── ...
├── user_profile/      # Generated with v1.2.0 (JSON Enhanced)
│   └── ...
└── products/          # Generated with v1.2.0 (JSON Enhanced)
    └── ...
```

### Strategy 3: Full Migration (Recommended for new projects)

**When to use:**
- Starting new project
- Major refactor
- Want best type safety

**How:**
1. Use only "Flutter (JSON Enhanced)"
2. Prepare JSON examples from API docs
3. Generate all features with new workflow

## Step-by-Step Migration Example

### Before (v1.1.7 Generated Code)

**Manually created model:**
```dart
// lib/auth/data/models/login_response.dart
class LoginResponse {
  final String token;
  
  LoginResponse({required this.token});
  
  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      token: json['token'],
    );
  }
}
```

**API Service:**
```dart
@GET('/auth/login')  // Always GET, had to change manually
Future<HttpResponse<LoginResponse>> login();
```

### After (v1.2.0 Generated Code)

**Dialog Input:**
- Function: `login`
- Endpoint: `/auth/login`
- Method: `POST` ← Selected from dropdown
- Request JSON:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- Response JSON:
```json
{
  "token": "eyJhbGc...",
  "user": {
    "id": 1,
    "name": "John Doe"
  }
}
```

**Auto-generated models:**

```dart
// lib/auth/data/models/user.dart
class User {
  final int id;
  final String name;

  User({required this.id, required this.name});

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int? ?? 0,
      name: json['name'] as String? ?? "",
    );
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'name': name};
  }
}

// lib/auth/data/models/login_request.dart
class LoginRequest {
  final String email;
  final String password;

  LoginRequest({required this.email, required this.password});

  factory LoginRequest.fromJson(Map<String, dynamic> json) {
    return LoginRequest(
      email: json['email'] as String? ?? "",
      password: json['password'] as String? ?? "",
    );
  }

  Map<String, dynamic> toJson() {
    return {'email': email, 'password': password};
  }
}

// lib/auth/data/models/login_response.dart
class LoginResponse {
  final String token;
  final User user;

  LoginResponse({required this.token, required this.user});

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      token: json['token'] as String? ?? "",
      user: json['user'] != null
          ? User.fromJson(json['user'])
          : throw Exception('user is required'),
    );
  }

  Map<String, dynamic> toJson() {
    return {'token': token, 'user': user.toJson()};
  }
}
```

**API Service:**
```dart
@POST('/auth/login')  // Correct method!
Future<HttpResponse<LoginResponse>> login(@Body() LoginRequest request);
```

## Common Migration Questions

### Q: Do I need to regenerate all my existing features?
**A:** No. Existing features work fine. Only regenerate if you want the new benefits.

### Q: Can I mix old and new generated code?
**A:** Yes! They work side-by-side without conflicts.

### Q: What if my JSON structure changes?
**A:** Regenerate that specific feature. Delete the folder and run the generator again with updated JSON.

### Q: How do I handle optional fields?
**A:** The JSON parser detects null values and makes fields nullable automatically.

### Q: What about authentication headers?
**A:** That's configured in your Dio setup (not generated). Same as before.

### Q: Can I customize generated models?
**A:** Yes, but regenerating will overwrite changes. Consider extending generated classes instead:

```dart
class LoginResponseExtended extends LoginResponse {
  // Add custom methods
  bool get isTokenValid => token.isNotEmpty;
}
```

## Migration Checklist

- [ ] Update plugin to v1.2.0
- [ ] Read this migration guide
- [ ] Try "Flutter (JSON Enhanced)" on a test feature
- [ ] Prepare JSON examples from your API
- [ ] Decide migration strategy (Legacy, Gradual, or Full)
- [ ] Update documentation for your team
- [ ] Run build_runner after generation
- [ ] Test generated code
- [ ] Commit changes

## Rollback Plan

If you need to rollback:

1. **Revert plugin:**
   - Go to Settings → Plugins
   - Right-click plugin → Uninstall
   - Install v1.1.7 from disk or marketplace

2. **Use Git:**
   ```bash
   git checkout HEAD -- lib/your_feature/
   ```

3. **Keep both versions:**
   - Install v1.2.0 in one IDE instance
   - Keep v1.1.7 in another IDE instance

## Support During Migration

Need help?
- **GitHub Issues**: [Report issues](https://github.com/naviy/clean-gen/issues)
- **Discussions**: [Ask questions](https://github.com/naviy/clean-gen/discussions)
- **Email**: support@naviy.com

## Best Practices for v1.2.0

1. **Always use actual API responses** for JSON input
2. **Test with Postman first**, then copy JSON
3. **Keep JSON examples** in documentation
4. **Use descriptive function names** (e.g., `getUserById` not `get`)
5. **One feature per folder** for better organization
6. **Run build_runner after every generation**
7. **Commit generated code** to version control

## What's Next?

After migrating, explore:
- [ ] Generate complex nested structures
- [ ] Try all HTTP methods (GET, POST, PUT, DELETE)
- [ ] Use with GraphQL (coming in v1.3.0)
- [ ] Custom templates (coming in v1.3.0)
- [ ] Share feedback for future improvements

---

**Need more help?** Check out:
- [Quick Start Guide](QUICK_START.md)
- [Developer Guide](DEVELOPER_GUIDE.md)
- [README](README.md)
- [Changelog](CHANGELOG.md)
