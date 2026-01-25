# Plugin Test Scenarios

## Test Case 1: Simple POST Login
**Input:**
- Feature Name: `auth`
- Function Name: `login`
- HTTP Method: `POST`
- API Endpoint: `/api/v1/auth/login`
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
    "name": "John Doe",
    "email": "user@example.com"
  }
}
```

**Expected Output:**
- `auth/data/models/login_body_model.dart` - LoginBodyModel class
- `auth/data/models/login_model.dart` - LoginModel class with nested User class
- API Service: `@POST('/api/v1/auth/login')` with `@Body() LoginBodyModel request`
- Repository: `Future<DataState<LoginModel>> login(LoginBodyModel request)`

---

## Test Case 2: GET with Parameters
**Input:**
- Feature Name: `user`
- Function Name: `getUsers`
- HTTP Method: `GET`
- API Endpoint: `/api/v1/users`
- Request JSON:
```json
{
  "page": 1,
  "limit": 10,
  "search": "john"
}
```
- Response JSON:
```json
{
  "data": [
    {
      "id": 1,
      "name": "John Doe"
    }
  ],
  "total": 100
}
```

**Expected Output:**
- `user/data/models/get_users_param_model.dart` - GetUsersParamModel class
- `user/data/models/get_users_model.dart` - GetUsersModel class
- API Service: `@GET('/api/v1/users')` with `@Query()` parameters
- Repository: `Future<DataState<GetUsersModel>> getUsers(GetUsersParamModel request)`

---

## Test Case 3: PUT Update Profile
**Input:**
- Feature Name: `profile`
- Function Name: `updateProfile`
- HTTP Method: `PUT`
- API Endpoint: `/api/v1/profile`
- Request JSON:
```json
{
  "name": "John Updated",
  "bio": "New bio"
}
```
- Response JSON:
```json
{
  "message": "Profile updated successfully",
  "profile": {
    "id": 1,
    "name": "John Updated",
    "bio": "New bio"
  }
}
```

**Expected Output:**
- `profile/data/models/update_profile_body_model.dart` - UpdateProfileBodyModel
- `profile/data/models/update_profile_model.dart` - UpdateProfileModel with nested Profile
- API Service: `@PUT('/api/v1/profile')` with `@Body() UpdateProfileBodyModel request`

---

## Test Case 4: DELETE Request
**Input:**
- Feature Name: `user`
- Function Name: `deleteUser`
- HTTP Method: `DELETE`
- API Endpoint: `/api/v1/users/123`
- Request JSON: (empty)
- Response JSON:
```json
{
  "message": "User deleted successfully",
  "success": true
}
```

**Expected Output:**
- `user/data/models/delete_user_model.dart` - DeleteUserModel
- No request model generated (empty request)
- API Service: `@DELETE('/api/v1/users/123')` with no parameters
- Repository: `Future<DataState<DeleteUserModel>> deleteUser()`

---

## Checklist for Manual Testing

### UI Testing
- [ ] Dialog opens when clicking "New → Clean Gen → Flutter (JSON Enhanced)"
- [ ] Feature name input works
- [ ] Add (+) button adds new function panel
- [ ] Remove (-) button removes last function panel
- [ ] HTTP method dropdown shows all 4 methods
- [ ] JSON text areas accept multi-line input
- [ ] Dialog scrolls when multiple functions added

### Code Generation Testing
- [ ] Folder structure created correctly (data/domain/presentation)
- [ ] Model files generated with correct names (body_model/param_model/model)
- [ ] JSON comments appear above each model class
- [ ] fromJson and toJson methods generated
- [ ] Nested classes handled properly
- [ ] API service has correct HTTP annotations
- [ ] Repository interface matches implementation
- [ ] Use cases have correct parameter types
- [ ] Cubit and State classes compile
- [ ] DI file includes all dependencies
- [ ] All imports are correct

### Edge Cases
- [ ] Empty JSON (no request) - should skip request model
- [ ] Complex nested objects
- [ ] Arrays of objects
- [ ] Mixed types (string, int, double, bool)
- [ ] Multiple functions in one feature
- [ ] Special characters in function names
- [ ] Very long JSON

---

## Quick Start for Real Testing

1. Install JDK 17
2. Set JAVA_HOME
3. Run: `.\gradlew runIde`
4. Create a test Flutter project in the sandbox IDE
5. Right-click on `lib` folder → New → Clean Gen → Flutter (JSON Enhanced)
6. Enter test data from Test Case 1
7. Verify generated files
