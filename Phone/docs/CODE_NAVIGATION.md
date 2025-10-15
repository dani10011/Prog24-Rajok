# ClassMaster Pro - Code Navigation Guide

This document provides quick navigation paths to key components in the codebase.

## Quick Navigation Index

### 🎯 Core Entry Points
- **Main Entry**: `app/src/main/java/com/example/classmasterpro/MainActivity.kt`
  - App initialization, NFC adapter setup
  - Toast message handling
  - Compose UI setup

### 🖥️ UI Screens
- **Login Screen**: `app/src/main/java/com/example/classmasterpro/screens/LoginScreen.kt`
  - Authentication UI with email/password fields
  - Dark mode toggle (top-left)
  - Language toggle (top-right)
  - Real API authentication with token storage
  - Login logic: `handleLogin()` function (lines 87-117)
  - Saves userId, token, and email to SharedPreferences on successful login
  - Error handling with email validation and network error messages

- **NFC Scanner Screen**: `app/src/main/java/com/example/classmasterpro/screens/NFCScannerScreen.kt`
  - NFC status checking UI
  - Test API call button
  - Logout functionality
  - Dark mode toggle
  - NFCStatus enum: `NOT_SUPPORTED`, `DISABLED`, `ENABLED`

- **Blackjack Screen**: `app/src/main/java/com/example/classmasterpro/screens/BlackjackScreen.kt`
  - Mini-game implementation

### 🧭 Navigation
- **App Navigation**: `app/src/main/java/com/example/classmasterpro/navigation/AppNavigation.kt`
  - NavHost setup with routes
  - Dark mode state management
  - Screen sealed class definitions
  - Navigation graph

### 🛠️ Utilities
- **API Helper**: `app/src/main/java/com/example/classmasterpro/utils/ApiHelper.kt`
  - `login(email, password)` - Authenticate user and get JWT token
  - `makeApiCall(token)` - Test API call with optional bearer token
  - Uses OkHttp 4.12.0 and Gson for JSON parsing
  - Runs on Dispatchers.IO

- **Config**: `app/src/main/java/com/example/classmasterpro/utils/Config.kt`
  - API base URL and endpoint configuration
  - Centralized configuration management

- **Auth Preferences**: `app/src/main/java/com/example/classmasterpro/utils/AuthPreferences.kt`
  - JWT token storage in SharedPreferences
  - `saveAuthData()` - Save userId, token, and email
  - `getToken()` - Retrieve stored token
  - `isLoggedIn()` - Check if user has valid session
  - `clearAuthData()` - Logout and clear all auth data

- **Language Helper**: `app/src/main/java/com/example/classmasterpro/utils/LanguageHelper.kt`
  - Language switching logic (English/Hungarian)
  - Locale management

### 📦 Models
- **Auth Models**: `app/src/main/java/com/example/classmasterpro/models/AuthModels.kt`
  - `LoginRequest` - Email and password for login
  - `LoginResponse` - userId and JWT token from API
  - `ApiError` - Generic error response structure

### 🎨 UI Theme
- **Colors**: `app/src/main/java/com/example/classmasterpro/ui/theme/Color.kt`
  - `PrimaryBlue` (#0357AF) - Primary actions
  - `SecondaryBlue` (#0180CC) - Secondary elements
  - `LightBlue` (#74CEF7) - Accents
  - `SkyBlue` (#9BE8F0) - Light backgrounds
  - `PaleBlue` (#E6FBFA) - Backgrounds/surfaces
  - `Accent` (#E4815A) - Warnings/errors

- **Theme**: `app/src/main/java/com/example/classmasterpro/ui/theme/Theme.kt`
  - Material3 theme configuration
  - **IMPORTANT**: `dynamicColor = false` to prevent Android 12+ from overriding custom colors
  - Dark/Light theme definitions

- **Typography**: `app/src/main/java/com/example/classmasterpro/ui/theme/Type.kt`
  - Font definitions and text styles

### 📝 Resources
- **English Strings**: `app/src/main/res/values/strings.xml`
  - All English text resources
  - Error messages
  - UI labels

- **Hungarian Strings**: `app/src/main/res/values-hu/strings.xml`
  - Hungarian translations

- **Manifest**: `app/src/main/AndroidManifest.xml`
  - App permissions (NFC, Internet)
  - Activity declarations
  - App configuration

### ⚙️ Configuration
- **App Build**: `app/build.gradle.kts`
  - Dependencies (Compose, OkHttp, etc.)
  - SDK versions (min 29, target 35)
  - Build configuration

- **Version Catalog**: `gradle/libs.versions.toml`
  - Centralized version management for core libraries

- **Project Build**: `build.gradle.kts`
  - Project-level Gradle configuration

## Common Tasks Quick Reference

### Authentication
- **Login API endpoint**: `POST https://09cc208360a9.ngrok-free.app/api/Auth/Login`
- **Login logic**: `handleLogin()` function in `LoginScreen.kt:87-117`
- **API authentication**: `ApiHelper.login()` in `utils/ApiHelper.kt:42-76`
- **Response structure**: `LoginResponse` model in `models/AuthModels.kt` with `userId` and `token`
- **Token storage**: `AuthPreferences.saveAuthData()` in `LoginScreen.kt:94-99`
- **Persistent login**: Auto-login on app startup if token exists (`AppNavigation.kt:38-40`)
- **Logout**: Clears auth data via `AuthPreferences.clearAuthData()` (`AppNavigation.kt:64-69`)

### API Integration
- **Change API endpoint**: Edit `makeApiCall()` in `utils/ApiHelper.kt`
- **Add new API calls**: Add functions to `utils/ApiHelper.kt`

### UI Modifications
- **Add new screen**:
  1. Create new file in `screens/` package
  2. Add route to `Screen` sealed class in `navigation/AppNavigation.kt`
  3. Add composable to NavHost in `AppNavigation.kt`

- **Change colors**: Edit `ui/theme/Color.kt`
- **Modify theme**: Edit `ui/theme/Theme.kt` (remember `dynamicColor = false`)

### Localization
- **Add text**: Update both `values/strings.xml` and `values-hu/strings.xml`
- **Add language**: Create new `values-{lang}/strings.xml` and update `LanguageHelper.kt`

### NFC Functionality
- **NFC initialization**: `MainActivity.onCreate()`
- **NFC status**: `NFCScannerScreen.kt` - `NFCStatus` enum
- **NFC permissions**: `AndroidManifest.xml`

## File Structure Map

```
Phone/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/classmasterpro/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   ├── NFCScannerScreen.kt
│   │   │   │   │   └── BlackjackScreen.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── AppNavigation.kt
│   │   │   │   ├── models/
│   │   │   │   │   └── AuthModels.kt
│   │   │   │   ├── utils/
│   │   │   │   │   ├── ApiHelper.kt
│   │   │   │   │   ├── AuthPreferences.kt
│   │   │   │   │   ├── Config.kt
│   │   │   │   │   └── LanguageHelper.kt
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Color.kt
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Type.kt
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   └── strings.xml
│   │   │       ├── values-hu/
│   │   │       │   └── strings.xml
│   │   │       ├── drawable/
│   │   │       └── AndroidManifest.xml
│   │   └── test/ (unit tests)
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── CLAUDE.md (project instructions)
└── docs/
    └── CODE_NAVIGATION.md (this file)
```

## Key Dependencies

- **Jetpack Compose**: Modern UI toolkit
- **Material3**: Material Design 3 components
- **OkHttp 4.12.0**: HTTP client for API calls
- **Gson 2.10.1**: JSON serialization/deserialization
- **Kotlin Coroutines**: Async operations
- **Navigation Compose**: Screen navigation
- **Android NFC API**: NFC tag reading

## Important Constants

### Configuration
- Base API URL: `https://09cc208360a9.ngrok-free.app` (Config.kt:9)
- Login endpoint: `/api/Auth/Login` (Config.kt:12)
- Swagger docs: `https://09cc208360a9.ngrok-free.app/swagger/index.html`

### SDK Versions
- minSdk: 29 (Android 10)
- targetSdk: 35 (Android 15)
- Kotlin: 2.0.21

## Search Patterns

When looking for specific functionality:
- **Authentication logic**: Search for `handleLogin`, `AuthPreferences`, or `saveAuthData`
- **Token management**: Search for `getToken`, `isLoggedIn`, or `clearAuthData`
- **API calls**: Search for `makeApiCall`, `ApiHelper`, or `OkHttpClient`
- **NFC status**: Search for `NFCStatus` or `nfcAdapter`
- **Navigation**: Search for `NavHost` or `Screen.`
- **Colors**: Search for `PrimaryBlue` or color hex codes
- **Error messages**: Search in `strings.xml` for `error_`
- **Dark mode**: Search for `isDarkMode`
