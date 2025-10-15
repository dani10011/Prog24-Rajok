# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-platform university classroom management system called "Prog24-Rajok" that includes:
- **Backend**: ASP.NET Core Web API (.NET 8.0) with SQL Server database
- **Frontend**: Angular 16 web application with PrimeNG UI components
- **Phone**: Android mobile app (Kotlin/Jetpack Compose)
- **RaspberryScanner**: Python-based NFC reader for room access control

The system manages students, instructors, courses, rooms, timetables, and handles **NFC-based room entry and attendance tracking** via card/phone scanning.

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

Core entities with relationships defined in AppDbContext.cs:

**User Management:**
- **User** - Base user with unique Neptun_Code and Email
- **Student** - 1:1 with User, includes `Card_Id` and `Phone_Id` for NFC identification
- **Instructor** - 1:1 with User (User_Id as primary key)
- **Role** - User role definitions (Admin, Instructor, Student)

**Academic Structure:**
- **Faculty** - Top-level academic organization
- **Department** - Belongs to Faculty
- **Major** - Student's field of study
- **Subject** - Academic courses
- **Course** - Links Subject, Instructor, Room with Start_Time/End_Time
- **CourseStudent** - Junction table (composite key: Course_Id, Student_Id)
- **SubjectStudent** - Junction table (composite key: Subject_Id, Student_Id)

**Facilities:**
- **Building** - Physical buildings
- **Room** - Classrooms with capacity and equipment
- **Reservation** - Room booking system
- **Event** - Calendar events

**NFC Attendance System:**
- **RoomEntryRequest** - Workflow: Student scans NFC → creates request → Instructor approves/denies
  - Status values: "Pending", "Approved", "Denied", "Expired"
  - Auto-expiry after 24 hours via `ExpireOldRequests()` method
  - **NOW USES NFC ID** instead of Student ID (breaking change from earlier versions)

- **StudentClassAttendance** - Tracks active attendance (who's currently in class)
  - Entry_Time and Exit_Time (NULL = currently in class)
  - Links Student, Course, and Room

- **StudentClassLog** - Audit log for ALL entry/exit attempts
  - Logs both successful and failed attempts
  - Includes Nfc_Id_Used, Action ("Entry"/"Exit"), Success flag, and Failure_Reason
  - Critical for security monitoring and duplicate prevention

### Authentication

JWT-based authentication configured in Program.cs:21-39:
- Token validation with Issuer, Audience, and Signing Key
- Settings stored in appsettings.json under "JwtSettings"
- ClockSkew set to zero for precise token expiration
- Swagger UI configured with Bearer token support (Program.cs:74-99)
- Access Swagger at: `https://localhost:{port}/swagger`

### Database Connection

SQL Server connection in appsettings.json:9-10:
- Server: 192.168.181.113:1433
- Database: Prog24
- Authentication: SQL Server (SA account)
- TrustServerCertificate enabled for development

**Important:** This project does NOT use EF Core migrations. Database schema changes are managed via SQL scripts in the `Backend/` directory.

## Database Setup

The project uses **manual SQL scripts** instead of EF Core migrations. Run these scripts in order:

### Initial Setup
1. Ensure SQL Server is running and accessible at the connection string in appsettings.json
2. Create the database if it doesn't exist: `CREATE DATABASE Prog24;`

### NFC Attendance System Migration (Latest)

Run these scripts **in order** for the NFC-based attendance system:

```bash
# 1. Add NFC fields to Student table
Backend/AlterStudentTable.sql

# 2. Create attendance tracking table
Backend/CreateStudentClassAttendanceTable.sql

# 3. Create audit log table
Backend/CreateStudentClassLogTable.sql

# 4. Create room entry request table (if not already exists)
Backend/CreateRoomEntryRequestTable.sql
```

After running migrations, populate test NFC IDs:
```sql
UPDATE Student SET Card_Id = 'AABBCCDD' WHERE User_Id = 1;
UPDATE Student SET Phone_Id = 'EEFFGGHH' WHERE User_Id = 2;
```

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

**Default URL:** `https://localhost:{port}`
**Swagger UI:** `https://localhost:{port}/swagger`

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
python scanner.py <roomId>

# Example: Start scanner for Room ID 5
python scanner.py 5
```

**Important:** Update `API_URL` in scanner.py:18 to point to your backend endpoint (ngrok tunnel or production URL).

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
9. Write SQL migration script and save to `Backend/` directory

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

## NFC-Based Room Entry & Attendance Workflow

**Critical:** This is the core business logic that changed significantly in recent updates.

### Current Implementation (NFC-Based with Toggle Entry/Exit)

Located in RoomEntryRequestService.cs:23-87:

**Toggle Behavior:** Scan once to enter, scan again to exit.

1. **Student scans NFC card/phone at room door**
   - RaspberryScanner sends POST request with `NfcId` (not Student ID!)

2. **Backend lookup process:**
   - Looks up student by matching Card_Id OR Phone_Id
   - Throws exception if NFC ID not found in system

3. **Validation checks:**
   - Finds active course in room (Start_Time ≤ now ≤ End_Time)
   - Checks if student already checked into this class

4. **Entry path (student NOT in class):**
   - Creates StudentClassAttendance record (Entry_Time set, Exit_Time NULL)
   - Logs successful entry to StudentClassLog with Action="Entry"
   - Creates RoomEntryRequest with Status="Pending" (requires instructor approval)

5. **Exit path (student ALREADY in class):**
   - Updates existing StudentClassAttendance record (sets Exit_Time)
   - Logs successful exit to StudentClassLog with Action="Exit"
   - Creates RoomEntryRequest with Status="Approved" (auto-approved, no instructor needed)

6. **Instructor approval (entries only):**
   - Instructor receives notification for entry requests (via GetRequestsByInstructor)
   - Instructor approves/denies entry via UpdateRequestStatus
   - Exit requests are auto-approved and don't require instructor action

### API Endpoint Changes

**Old (deprecated):**
```json
POST /api/RoomEntryRequest/CreateRequest
{
  "studentId": 1,
  "roomId": 5,
  "reason": "Attending class"
}
```

**New (current):**
```json
POST /api/RoomEntryRequest/CreateRequest
{
  "nfcId": "AABBCCDD",
  "roomId": 5,
  "reason": "Attending class"
}
```

### Toggle Entry/Exit Logic

The system implements toggle behavior for NFC scans:
- Queries StudentClassAttendance for existing record with NULL Exit_Time
- **If NOT found:** Student is entering → creates new attendance record with Entry_Time
- **If found:** Student is exiting → sets Exit_Time on existing record
- Both entry and exit actions are logged to StudentClassLog with appropriate Action value
- Exit requests are automatically approved (Status="Approved") without requiring instructor approval

### Querying Attendance Data

**Find who's currently in class:**
```sql
SELECT s.User_Id, u.Name, c.Id AS Course_Id, r.Room_Number, sca.Entry_Time
FROM Student_class_attendance sca
JOIN Student s ON sca.Student_Id = s.User_Id
JOIN [User] u ON s.User_Id = u.Id
JOIN Course c ON sca.Course_Id = c.Id
JOIN Room r ON sca.Room_Id = r.Id
WHERE sca.Exit_Time IS NULL
ORDER BY sca.Entry_Time DESC;
```

**View recent attempts (including failures):**
```sql
SELECT TOP 20
    scl.Id, u.Name, r.Room_Number, scl.Attempt_Time,
    scl.Action, scl.Success, scl.Failure_Reason, scl.Nfc_Id_Used
FROM Student_class_log scl
JOIN Student s ON scl.Student_Id = s.User_Id
JOIN [User] u ON s.User_Id = u.Id
JOIN Room r ON scl.Room_Id = r.Id
ORDER BY scl.Attempt_Time DESC;
```

**Security monitoring (failed attempts):**
```sql
SELECT scl.Nfc_Id_Used, COUNT(*) AS Failed_Attempts, MAX(scl.Attempt_Time) AS Last_Attempt
FROM Student_class_log scl
WHERE scl.Success = 0
GROUP BY scl.Nfc_Id_Used
HAVING COUNT(*) > 3
ORDER BY Failed_Attempts DESC;
```

## API Controllers & Endpoints

All controllers inherit from `ControllerBase` and use `[ApiController]` attribute. Most endpoints require JWT Bearer token (except Auth endpoints).

**Key Controllers:**
- **AuthController** - Login, Register, RefreshToken
- **StudentController** - Student management, GetStudents
- **InstructorController** - Instructor management
- **CourseController** - Course CRUD operations
- **RoomController** - Room management
- **RoomEntryRequestController** - NFC-based entry requests (CreateRequest, UpdateRequestStatus, GetRequestsByInstructor)
- **TimetableController** - User schedules
- **CourseStudentController** - Course enrollment
- **SubjectStudentController** - Subject enrollment
- **BuildingController** - Building management
- **DepartmentController** - Department management
- **EventController** - Event management
- **FacultyController** - Faculty management
- **MajorController** - Major management
- **ReservationController** - Room reservations
- **RoleController** - Role management
- **SubjectController** - Subject management
- **UserController** - User management

## Frontend Architecture

Angular 16 application using:
- **PrimeNG** for UI components (tables, dialogs, forms)
- **Services** in `src/app/services/` for HTTP communication
- **Components** for feature modules (students-list, teacher-dashboard, student-dashboard, rooms, courses)
- **Interfaces** in `src/app/interfaces/` for TypeScript models (includes course.ts and room.ts)

API calls use Angular HttpClient with RxJS observables. See `Frontend/CLAUDE.md` for detailed frontend documentation.

## Phone App Architecture

Android app built with Kotlin and Jetpack Compose:
- **LoginScreen** - User authentication
- **NFCScannerScreen** - NFC card reading functionality
- **InstructorScreen** - Instructor-specific features
- **BlackjackScreen** - (Easter egg/demo feature)

See `Phone/CLAUDE.md` for detailed Android documentation.

## Project-Specific Notes

### Database Schema Naming

The project uses snake_case for database columns (User_Id, Neptun_Code, Card_Id) but PascalCase in C# entities. This is reflected in all DTOs and responses.

### CORS Configuration

Development CORS policy "AllowAll" allows any origin (Program.cs:61-69). This should be restricted in production.

### Time Handling

All timestamps use UTC (DateTime.UtcNow) to avoid timezone issues across components.

### NFC Card Integration

- **RaspberryScanner** uses PN532 I2C reader via Adafruit library
- Card UIDs are converted to uppercase hex strings for API transmission
- Both physical NFC cards (Card_Id) and phone NFC (Phone_Id) are supported
- NFC IDs must be unique across all students (enforced by filtered unique indexes)

### Testing & Development

1. **Swagger UI:** Best way to test API endpoints during development
   - Access at `https://localhost:{port}/swagger`
   - Click "Authorize" button and enter JWT token from login response

2. **Test Data:** Use the SQL scripts to populate test data
   - Remember to add NFC IDs to students for testing room entry

3. **ngrok for Mobile Testing:**
   - Backend API URL in environment files points to ngrok tunnel
   - Update `API_URL` in RaspberryScanner/scanner.py when ngrok URL changes
   - All requests include `ngrok-skip-browser-warning: true` header

### Known Warnings

The solution builds successfully with ~16 nullable reference type warnings. These are pre-existing and don't affect functionality.
