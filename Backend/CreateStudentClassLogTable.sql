-- Create Student_class_log table
-- This table logs all entry/exit attempts by students
-- Logs both successful and failed attempts for audit and security purposes

USE Prog24;
GO

CREATE TABLE Student_class_log (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Student_Id INT NOT NULL,
    Course_Id INT NULL,
    Room_Id INT NOT NULL,
    Attempt_Time DATETIME2(7) NOT NULL,
    Action NVARCHAR(50) NOT NULL,  -- 'Entry' or 'Exit'
    Success BIT NOT NULL,  -- 1 = successful, 0 = failed
    Failure_Reason NVARCHAR(500) NULL,
    Nfc_Id_Used NVARCHAR(100) NOT NULL,

    -- Foreign key constraints
    CONSTRAINT FK_StudentClassLog_Student FOREIGN KEY (Student_Id)
        REFERENCES Student(User_Id) ON DELETE NO ACTION,

    CONSTRAINT FK_StudentClassLog_Course FOREIGN KEY (Course_Id)
        REFERENCES Course(Id) ON DELETE NO ACTION,

    CONSTRAINT FK_StudentClassLog_Room FOREIGN KEY (Room_Id)
        REFERENCES Room(Id) ON DELETE NO ACTION,

    -- Check constraint to ensure Action is either 'Entry' or 'Exit'
    CONSTRAINT CK_StudentClassLog_Action CHECK (Action IN ('Entry', 'Exit'))
);
GO

-- Create index for student-based queries (sorted by most recent first)
CREATE NONCLUSTERED INDEX IX_StudentClassLog_Student_Time
ON Student_class_log(Student_Id, Attempt_Time DESC);
GO

-- Create index for room-based queries (sorted by most recent first)
CREATE NONCLUSTERED INDEX IX_StudentClassLog_Room_Time
ON Student_class_log(Room_Id, Attempt_Time DESC);
GO

-- Create index for course-based queries
CREATE NONCLUSTERED INDEX IX_StudentClassLog_Course_Time
ON Student_class_log(Course_Id, Attempt_Time DESC)
WHERE Course_Id IS NOT NULL;
GO

-- Create index for failed attempts (security monitoring)
CREATE NONCLUSTERED INDEX IX_StudentClassLog_FailedAttempts
ON Student_class_log(Success, Attempt_Time DESC)
WHERE Success = 0;
GO

-- Create index for NFC ID lookups (tracking specific card/phone usage)
CREATE NONCLUSTERED INDEX IX_StudentClassLog_NfcId
ON Student_class_log(Nfc_Id_Used, Attempt_Time DESC);
GO

-- Verify the table was created
SELECT
    c.COLUMN_NAME,
    c.DATA_TYPE,
    c.IS_NULLABLE,
    c.CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS c
WHERE c.TABLE_NAME = 'Student_class_log'
ORDER BY c.ORDINAL_POSITION;
GO

PRINT 'Student_class_log table created successfully';
GO
