# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-platform university classroom management system called "Prog24-Rajok" that includes:
- **Backend**: ASP.NET Core Web API (.NET) with SQL Server database
- **Frontend**: Angular 16 web application with PrimeNG UI components
- **Phone**: Android mobile app (Kotlin/Jetpack Compose)
- **RaspberryScanner**: Python-based NFC reader for room access control

The system manages students, instructors, courses, rooms, timetables, and handles room entry requests via NFC card scanning.

## Backend Architecture

### Project Structure (3-Layer Architecture)

1. **Prog24.WebAPI** - API layer
   - Controllers for all endpoints
   - JWT authentication configuration
   - CORS policy: "AllowAll" for development
   - Dependency injection setup in Program.cs:18-58

2. **Prog24.Services** - Business logic layer
   - Service interfaces in `Services/Interfaces/`
   - Service implementations following interface pattern
   - DTOs in `Model/Dto/` for request/response models
   - All services registered as Scoped in DI container

3. **Prog24.DataContext** - Data access layer
   - `AppDbContext.cs` - Entity Framework DbContext
   - `Entities/` - Database entity models
   - Composite keys for junction tables (CourseStudent, SubjectStudent)
   - Custom foreign key relationships with restricted deletion

### Key Domain Entities

Core entities with relationships defined in AppDbContext.cs:34-84:
- **User** - Base user with unique Neptun_Code and Email
- **Student/Instructor** - 1:1 relationship with User (User_Id as primary key)
- **Course** - Links Subject, Instructor, Room with Start_Time/End_Time
- **RoomEntryRequest** - Workflow: Student scans NFC → creates request → Instructor approves/denies
  - Status values: "Pending", "Approved", "Denied", "Expired"
  - Auto-expiry after 24 hours via `ExpireOldRequests()` method

### Authentication

JWT-based authentication configured in Program.cs:21-39:
- Token validation with Issuer, Audience, and Signing Key
- Settings stored in appsettings.json under "JwtSettings"
- ClockSkew set to zero for precise token expiration
- Swagger UI configured with Bearer token support (Program.cs:74-99)

### Database Connection

SQL Server connection in appsettings.json:9-10:
- Server: 192.168.181.113:1433
- Database: Prog24
- Authentication: SQL Server (SA account)
- TrustServerCertificate enabled for development

## Development Commands

### Backend (.NET)

From the `Backend/` directory:

```bash
# Build the solution
dotnet build Prog24.WebAPI/Prog24.WebAPI.sln

# Run the Web API (starts on HTTPS)
dotnet run --project Prog24.WebAPI/Prog24.WebAPI.csproj

# Restore packages
dotnet restore Prog24.WebAPI/Prog24.WebAPI.sln

# Run with watch (auto-rebuild on changes)
dotnet watch --project Prog24.WebAPI/Prog24.WebAPI.csproj
```

### Frontend (Angular)

From the `Frontend/` directory:

```bash
# Install dependencies
npm install

# Start development server (default: http://localhost:4200)
npm start

# Build for production
npm run build

# Build with watch mode
npm run watch
```

### RaspberryScanner (Python/NFC)

From the `RaspberryScanner/` directory:

```bash
# Run the NFC scanner (requires Adafruit PN532 hardware)
python scanner.py
```

Note: scanner.py:8 contains API_URL that needs to be updated for your ngrok tunnel or API endpoint.

### Phone (Android/Kotlin)

From the `Phone/` directory:

```bash
# Build the app
./gradlew build

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## Important Implementation Patterns

### Adding a New Entity/Feature

1. Create entity in `Prog24.DataContext/Entities/`
2. Add DbSet to AppDbContext.cs
3. Configure relationships in OnModelCreating if needed
4. Create DTOs in `Prog24.Services/Model/Dto/`
5. Create service interface in `Services/Interfaces/`
6. Implement service in `Services/`
7. Register service in Program.cs DI container
8. Create controller in `Prog24.WebAPI/Controllers/`

### Service Pattern Example

All services follow this pattern (see RoomEntryRequestService.cs):
- Constructor injection of AppDbContext
- Async methods returning DTOs (not entities directly)
- Include() chains for eager loading related data
- Private mapping methods to convert entities to response DTOs
- Exception throwing for business logic violations

### Working with EF Core Queries

Common patterns in services:
- Use `.Include()` for eager loading navigation properties
- Chain `.ThenInclude()` for nested relationships
- Apply `.Where()` filters before `.ToListAsync()`
- Use `.FirstOrDefaultAsync()` for single records with null handling

### Room Entry Request Workflow

Critical business logic in RoomEntryRequestService.cs:23-54:
1. Student scans NFC card at room door
2. System finds active course in room (Start_Time ≤ now ≤ End_Time)
3. Creates request with Student_Id, Instructor_Id, Room_Id, Course_Id
4. Instructor receives notification (via GetRequestsByInstructor)
5. Instructor approves/denies via UpdateRequestStatus
6. Status must be "Pending" to allow updates

## Frontend Architecture

Angular 16 application using:
- **PrimeNG** for UI components (tables, dialogs, forms)
- **Services** in `src/app/services/` for HTTP communication
- **Components** for feature modules (students-list, teacher-dashboard, rooms)
- **Interfaces** in `src/app/interfaces/` for TypeScript models

API calls use Angular HttpClient with RxJS observables.

## Project-Specific Notes

### Database Schema Naming

The project uses snake_case for database columns (User_Id, Neptun_Code) but PascalCase in C# entities. This is reflected in all DTOs and responses.

### CORS Configuration

Development CORS policy "AllowAll" allows any origin (Program.cs:61-69). This should be restricted in production.

### Time Handling

All timestamps use UTC (DateTime.UtcNow) to avoid timezone issues across components.

### NFC Card Integration

RaspberryScanner uses PN532 I2C reader. Card UIDs are converted to uppercase hex strings for API transmission.
