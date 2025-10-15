-- Create Student_class_attendance table
-- This table tracks which students are currently in which classes
-- A record with NULL Exit_Time means the student is currently in class

USE Prog24;
GO

CREATE TABLE Student_class_attendance (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Student_Id INT NOT NULL,
    Course_Id INT NOT NULL,
    Room_Id INT NOT NULL,
    Entry_Time DATETIME2(7) NOT NULL,
    Exit_Time DATETIME2(7) NULL,

    -- Foreign key constraints
    CONSTRAINT FK_StudentClassAttendance_Student FOREIGN KEY (Student_Id)
        REFERENCES Student(User_Id) ON DELETE CASCADE,

    CONSTRAINT FK_StudentClassAttendance_Course FOREIGN KEY (Course_Id)
        REFERENCES Course(Id) ON DELETE CASCADE,

    CONSTRAINT FK_StudentClassAttendance_Room FOREIGN KEY (Room_Id)
        REFERENCES Room(Id) ON DELETE CASCADE
);
GO

-- Create composite index for efficient lookups
-- This helps find if a student is currently in a specific course
CREATE NONCLUSTERED INDEX IX_StudentClassAttendance_Student_Course_Exit
ON Student_class_attendance(Student_Id, Course_Id, Exit_Time);
GO

-- Create index for room-based queries
CREATE NONCLUSTERED INDEX IX_StudentClassAttendance_Room_Entry
ON Student_class_attendance(Room_Id, Entry_Time DESC);
GO

-- Create index for finding current attendance (where Exit_Time is NULL)
CREATE NONCLUSTERED INDEX IX_StudentClassAttendance_CurrentAttendance
ON Student_class_attendance(Course_Id, Student_Id)
WHERE Exit_Time IS NULL;
GO

-- Verify the table was created
SELECT
    c.COLUMN_NAME,
    c.DATA_TYPE,
    c.IS_NULLABLE,
    c.CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS c
WHERE c.TABLE_NAME = 'Student_class_attendance'
ORDER BY c.ORDINAL_POSITION;
GO

PRINT 'Student_class_attendance table created successfully';
GO
