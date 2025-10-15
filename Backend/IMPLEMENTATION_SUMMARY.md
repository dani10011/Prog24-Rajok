# NFC-Based Class Attendance System - Implementation Summary

## Overview

This document summarizes the implementation of an NFC-based class attendance tracking system that replaces the previous student ID-based entry system.

## What Changed

### 1. Database Schema Changes

#### Student Table
- Added `Card_Id` (NVARCHAR(100), nullable) - Stores NFC card identifiers
- Added `Phone_Id` (NVARCHAR(100), nullable) - Stores phone NFC identifiers
- Both fields have unique indexes (filtered for non-null values)

#### New Tables

**Student_class_attendance**
- Tracks students currently in class
- Fields:
  - `Id` (INT, PRIMARY KEY, IDENTITY)
  - `Student_Id` (INT, FK to Student.User_Id)
  - `Course_Id` (INT, FK to Course.Id)
  - `Room_Id` (INT, FK to Room.Id)
  - `Entry_Time` (DATETIME2)
  - `Exit_Time` (DATETIME2, nullable)
- When `Exit_Time` is NULL, student is currently in class
- Includes indexes for efficient lookups

**Student_class_log**
- Audit log for all entry/exit attempts
- Fields:
  - `Id` (INT, PRIMARY KEY, IDENTITY)
  - `Student_Id` (INT, FK to Student.User_Id)
  - `Course_Id` (INT, FK to Course.Id, nullable)
  - `Room_Id` (INT, FK to Room.Id)
  - `Attempt_Time` (DATETIME2)
  - `Action` (NVARCHAR(50)) - "Entry" or "Exit"
  - `Success` (BIT) - 1 for success, 0 for failure
  - `Failure_Reason` (NVARCHAR(500), nullable)
  - `Nfc_Id_Used` (NVARCHAR(100)) - The NFC ID that was scanned
- Includes multiple indexes for security monitoring and queries

### 2. Backend Code Changes

#### Entity Changes
- **Student.cs**: Added `Card_Id` and `Phone_Id` properties
- **StudentClassAttendance.cs** (NEW): Entity for tracking active attendance
- **StudentClassLog.cs** (NEW): Entity for logging all attempts
- **AppDbContext.cs**: Added DbSets and indexes for new entities

#### DTO Changes
- **CreateRoomEntryRequestDto.cs**: Changed `StudentId` (int) → `NfcId` (string)

#### Service Logic Changes
- **RoomEntryRequestService.cs**:
  - `CreateRequest()` method completely rewritten with new logic:
    1. Looks up student by NFC ID (checks both Card_Id and Phone_Id)
    2. Validates active course exists in room
    3. Checks if student is already in class (prevents duplicates)
    4. Logs attempt to Student_class_log table
    5. Creates attendance record in Student_class_attendance table
    6. Creates room entry request as before
  - New `LogAttempt()` private method for comprehensive logging

## Database Migration Steps

Execute these SQL scripts **in order** on your SQL Server database:

### Step 1: Update Student Table
```bash
# File: Backend/AlterStudentTable.sql
```
Run this first to add the Card_Id and Phone_Id columns to existing Student table.

### Step 2: Create Attendance Table
```bash
# File: Backend/CreateStudentClassAttendanceTable.sql
```
Creates the Student_class_attendance table for tracking who's in class.

### Step 3: Create Log Table
```bash
# File: Backend/CreateStudentClassLogTable.sql
```
Creates the Student_class_log table for audit logging.

## Testing the Implementation

### 1. Populate Test Data

Before testing, you need to add NFC IDs to existing students:

```sql
USE Prog24;

-- Example: Update students with test NFC IDs
UPDATE Student SET Card_Id = 'AABBCCDD' WHERE User_Id = 1;
UPDATE Student SET Phone_Id = 'EEFFGGHH' WHERE User_Id = 2;
UPDATE Student SET Card_Id = '11223344' WHERE User_Id = 3;
```

### 2. Test API Endpoint

**Endpoint**: `POST /api/RoomEntryRequest/CreateRequest`

**Old Request Format** (no longer works):
```json
{
  "studentId": 1,
  "roomId": 5,
  "reason": "Attending class"
}
```

**New Request Format**:
```json
{
  "nfcId": "AABBCCDD",
  "roomId": 5,
  "reason": "Attending class"
}
```

### 3. Expected Behavior

#### Success Case
- Student exists with matching Card_Id or Phone_Id
- Active course exists in the room
- Student not already checked in
- **Result**: Creates attendance record, logs success, returns room entry request

#### Failure Cases

**Unknown NFC ID**:
- Error: "Student not found with provided NFC ID"
- Logs attempt with Student_Id = 0

**No Active Course**:
- Error: "No active course found in the specified room at this time"
- Logs failed attempt

**Duplicate Entry**:
- Error: "Student is already checked into this class"
- Logs failed attempt (this is the key feature!)

## How the System Works

### Normal Flow
1. Student approaches room door with NFC card/phone
2. Raspberry Pi scanner reads NFC ID (Card UID)
3. Scanner sends POST request to `/api/RoomEntryRequest/CreateRequest` with NFC ID
4. Backend looks up student by Card_Id or Phone_Id
5. Backend finds active course in that room
6. Backend checks if student already in class
7. If not duplicate, creates attendance record
8. Logs the attempt (success or failure)
9. Creates room entry request for instructor approval

### Duplicate Prevention
- If student scans twice, second attempt is rejected
- Both attempts are logged in Student_class_log
- Prevents accidental double check-ins

## Querying the New Tables

### Check who's currently in class
```sql
SELECT
    s.User_Id,
    u.Name,
    c.Id AS Course_Id,
    r.Room_Number,
    sca.Entry_Time
FROM Student_class_attendance sca
JOIN Student s ON sca.Student_Id = s.User_Id
JOIN [User] u ON s.User_Id = u.Id
JOIN Course c ON sca.Course_Id = c.Id
JOIN Room r ON sca.Room_Id = r.Id
WHERE sca.Exit_Time IS NULL
ORDER BY sca.Entry_Time DESC;
```

### View recent entry attempts (including failures)
```sql
SELECT TOP 20
    scl.Id,
    u.Name AS Student_Name,
    r.Room_Number,
    scl.Attempt_Time,
    scl.Action,
    scl.Success,
    scl.Failure_Reason,
    scl.Nfc_Id_Used
FROM Student_class_log scl
JOIN Student s ON scl.Student_Id = s.User_Id
JOIN [User] u ON s.User_Id = u.Id
JOIN Room r ON scl.Room_Id = r.Id
ORDER BY scl.Attempt_Time DESC;
```

### Find failed entry attempts (security monitoring)
```sql
SELECT
    scl.Nfc_Id_Used,
    COUNT(*) AS Failed_Attempts,
    MAX(scl.Attempt_Time) AS Last_Attempt
FROM Student_class_log scl
WHERE scl.Success = 0
GROUP BY scl.Nfc_Id_Used
HAVING COUNT(*) > 3
ORDER BY Failed_Attempts DESC;
```

## Future Enhancements

### Exit Functionality
Currently, the system only handles entry. To add exit functionality:

1. Create a new endpoint: `POST /api/RoomEntryRequest/ExitClass`
2. Update the most recent Student_class_attendance record with `Exit_Time = NOW()`
3. Log the exit attempt to Student_class_log with `Action = 'Exit'`

### NFC ID Management Endpoints
You might want to create endpoints for:
- Adding/updating student Card_Id
- Adding/updating student Phone_Id
- Validating NFC IDs before assignment

### Reporting
- Daily attendance reports by course
- Student attendance history
- Room utilization reports

## Important Notes

### Database Consistency
- The SQL scripts create filtered unique indexes for Card_Id and Phone_Id
- This allows multiple students to have NULL values without violating uniqueness
- Only non-null values must be unique

### Logging Strategy
- **All** attempts are logged, including failures
- Even if student lookup fails (Student_Id = 0), the attempt is logged
- This provides complete audit trail for security and debugging

### Performance
- All critical queries have proper indexes
- Composite indexes optimize the most common queries
- Filtered indexes reduce index size and improve performance

## Build Status
✅ **Build Successful** - 0 Errors, 16 Warnings (mostly pre-existing nullable warnings)

## Next Steps
1. Run the three SQL scripts in order
2. Update existing student records with test NFC IDs
3. Test the new API endpoint with Postman/Swagger
4. Update the Raspberry Pi scanner code to send NfcId instead of StudentId
5. Monitor Student_class_log table for any issues
