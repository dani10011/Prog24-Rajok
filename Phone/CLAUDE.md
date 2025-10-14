# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ClassMaster Pro is an Android NFC-based class entry system for school use. The app allows students to scan NFC tags/cards to check into classes.

**Tech Stack:**
- Android (minSdk 29, targetSdk 35)
- Kotlin 2.0.21
- Jetpack Compose (Material3)
- OkHttp for API calls
- Android NFC API

## Build Commands

### Basic Build
```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### Run on Device/Emulator
```bash
# Install debug build
gradlew.bat installDebug

# Run app (requires device/emulator connected)
gradlew.bat assembleDebug
```

### Clean Build
```bash
gradlew.bat clean build
```

### Run Tests
```bash
# Unit tests
gradlew.bat test

# Instrumented tests (requires device/emulator)
gradlew.bat connectedAndroidTest

# Run specific test
gradlew.bat test --tests ExampleUnitTest
```

## Architecture

### Project Structure
The app follows a modular architecture with clear separation of concerns:

```
com.example.classmasterpro/
├── MainActivity.kt              # App entry point, NFC initialization
├── screens/
│   ├── LoginScreen.kt          # Login UI with authentication
│   └── NFCScannerScreen.kt     # NFC scanner UI and controls
├── navigation/
│   └── AppNavigation.kt        # Navigation logic and routes
├── utils/
│   └── ApiHelper.kt            # API call utilities
└── ui/theme/
    ├── Color.kt                # Color definitions
    ├── Theme.kt                # Material3 theme
    └── Type.kt                 # Typography
```

### UI Structure
The app uses a single-activity architecture with Jetpack Compose:
- **MainActivity.kt**: Main entry point, initializes NFC adapter and sets up Compose
- **screens/LoginScreen.kt**: Login screen with hardcoded authentication (admin/password)
- **screens/NFCScannerScreen.kt**: NFC scanner screen with status display and API testing
- **navigation/AppNavigation.kt**: Navigation setup between screens with dark mode state management
- **Theme files**: Custom blue color scheme defined in `ui/theme/Color.kt` and `Theme.kt`

### Color Scheme
The app uses a custom blue gradient color palette (in order of prominence):
1. `PrimaryBlue` (#0357AF) - Primary actions and branding
2. `SecondaryBlue` (#0180CC) - Secondary elements
3. `LightBlue` (#74CEF7) - Accents and highlights
4. `SkyBlue` (#9BE8F0) - Light backgrounds
5. `PaleBlue` (#E6FBFA) - Backgrounds and surfaces
6. `Accent` (#E4815A) - Warning/error states

**Important**: When making UI changes, maintain this color order and use `dynamicColor = false` in the theme to prevent Android 12+ from overriding the custom colors.

### NFC Functionality
- NFC adapter is initialized in `MainActivity.onCreate()`
- Status checking handled via `NFCStatus` enum in `screens/NFCScannerScreen.kt`
  - `NOT_SUPPORTED`: Device doesn't have NFC hardware
  - `DISABLED`: NFC is turned off
  - `ENABLED`: NFC is ready to use
- The app checks NFC status on launch and on button press
- NFC permissions are declared in `AndroidManifest.xml`

### Authentication
- Login screen uses hardcoded credentials (for now)
- Default credentials: username=`admin`, password=`password`
- Located in `screens/LoginScreen.kt`
- 1-second simulated network delay for realistic UX
- **To integrate real auth**: Replace the `handleLogin()` function logic

### API Integration
- `makeApiCall()` function in `utils/ApiHelper.kt` handles HTTP requests
- Currently uses JSONPlaceholder as a test endpoint
- Uses OkHttp 4.12.0 for networking
- Runs on Dispatchers.IO for background execution
- **To integrate real API**: Replace URL in `utils/ApiHelper.kt`

### Navigation & Dark Mode
- Navigation managed by `navigation/AppNavigation.kt`
- Dark mode state is maintained across navigation
- Toggle available on both login and NFC scanner screens (top-left corner)
- Dark mode uses deeper blue tones with adjusted contrast

## Key Files

### Source Code
- `app/src/main/java/com/example/classmasterpro/MainActivity.kt` - App entry point
- `app/src/main/java/com/example/classmasterpro/screens/LoginScreen.kt` - Login UI
- `app/src/main/java/com/example/classmasterpro/screens/NFCScannerScreen.kt` - NFC scanner UI
- `app/src/main/java/com/example/classmasterpro/navigation/AppNavigation.kt` - Navigation logic
- `app/src/main/java/com/example/classmasterpro/utils/ApiHelper.kt` - API utilities
- `app/src/main/java/com/example/classmasterpro/ui/theme/Color.kt` - Color definitions
- `app/src/main/java/com/example/classmasterpro/ui/theme/Theme.kt` - Material3 theme configuration

### Configuration
- `app/src/main/AndroidManifest.xml` - Permissions and app configuration
- `app/build.gradle.kts` - App dependencies and build configuration
- `gradle/libs.versions.toml` - Centralized version management

## Development Notes

### Adding New Dependencies
Add dependencies to `app/build.gradle.kts`. The project uses version catalogs (`libs.versions.toml`) for core Android dependencies, but third-party dependencies (like OkHttp) are declared directly in build.gradle.kts.

### NFC Testing
- Real NFC functionality requires a physical device with NFC hardware
- The emulator will show "NFC Not Supported" status
- Test NFC status checking and UI states work on emulators, but actual NFC reading requires hardware

### API Endpoint Configuration
When connecting to the actual backend:
1. Update the URL in `utils/ApiHelper.kt` in the `makeApiCall()` function
2. Modify request body/headers as needed for your API
3. Update response parsing to match your API's response format
4. Consider adding proper error handling for network failures

### Adding New Screens
1. Create a new composable file in `screens/` package
2. Add a new route in `navigation/AppNavigation.kt` (update the `Screen` sealed class)
3. Add the composable to the NavHost in `AppNavigation.kt`
4. Follow the existing pattern for dark mode support
