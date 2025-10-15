-- ====================================================================
-- SQL Migration Script for RoomEntryRequest Table
-- ====================================================================
-- This script creates the Room_entry_request table for tracking
-- student requests to enter rooms where instructors are teaching.
-- ====================================================================

-- Create the Room_entry_request table
CREATE TABLE Room_entry_request (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Student_Id INT NOT NULL,
    Instructor_Id INT NOT NULL,
    Room_Id INT NOT NULL,
    Course_Id INT NULL,
    Request_Time DATETIME2 NOT NULL DEFAULT GETDATE(),
    Status NVARCHAR(50) NOT NULL DEFAULT 'Pending',
    Reason NVARCHAR(500) NULL,
    Response_Time DATETIME2 NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_RoomEntryRequest_Student FOREIGN KEY (Student_Id)
        REFERENCES Student(User_Id),

    CONSTRAINT FK_RoomEntryRequest_Instructor FOREIGN KEY (Instructor_Id)
        REFERENCES Instructor(User_Id),

    CONSTRAINT FK_RoomEntryRequest_Room FOREIGN KEY (Room_Id)
        REFERENCES Room(Id),

    CONSTRAINT FK_RoomEntryRequest_Course FOREIGN KEY (Course_Id)
        REFERENCES Course(Id) ON DELETE SET NULL,

    -- Check constraint for valid status values
    CONSTRAINT CHK_RoomEntryRequest_Status CHECK (Status IN ('Pending', 'Approved', 'Denied', 'Expired'))
);

-- Create composite index for querying student requests by time
CREATE INDEX IX_RoomEntryRequest_Student_RequestTime
    ON Room_entry_request(Student_Id, Request_Time);

-- Create composite index for querying instructor requests by status
CREATE INDEX IX_RoomEntryRequest_Instructor_Status
    ON Room_entry_request(Instructor_Id, Status);

-- Create index for querying by room
CREATE INDEX IX_RoomEntryRequest_Room
    ON Room_entry_request(Room_Id);

-- Create index for querying by course
CREATE INDEX IX_RoomEntryRequest_Course
    ON Room_entry_request(Course_Id);

-- Create index for querying by status and request time (for cleanup/expiry)
CREATE INDEX IX_RoomEntryRequest_Status_RequestTime
    ON Room_entry_request(Status, Request_Time);

GO

-- ====================================================================
-- Optional: Sample cleanup stored procedure for expired requests
-- ====================================================================
-- This procedure can be called periodically to mark old pending requests as expired
-- Uncomment and modify the expiration time as needed

/*
CREATE PROCEDURE sp_ExpireOldRoomEntryRequests
    @ExpirationHours INT = 24
AS
BEGIN
    UPDATE Room_entry_request
    SET Status = 'Expired'
    WHERE Status = 'Pending'
        AND Request_Time < DATEADD(HOUR, -@ExpirationHours, GETDATE());

    SELECT @@ROWCOUNT AS ExpiredRequestsCount;
END
GO
*/

-- ====================================================================
-- Rollback Script (in case you need to remove the table)
-- ====================================================================
-- Uncomment the following lines to drop the table and its indexes

/*
DROP TABLE IF EXISTS Room_entry_request;
*/
