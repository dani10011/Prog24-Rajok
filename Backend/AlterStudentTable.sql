-- Add Card_Id and Phone_Id columns to Student table
-- These columns will store NFC identifiers for physical cards and phones

USE Prog24;
GO

-- Add Card_Id column (nullable, will store NFC card identifiers)
ALTER TABLE Student
ADD Card_Id NVARCHAR(100) NULL;
GO

-- Add Phone_Id column (nullable, will store phone NFC identifiers)
ALTER TABLE Student
ADD Phone_Id NVARCHAR(100) NULL;
GO

-- Create unique index on Card_Id (only for non-null values)
CREATE UNIQUE NONCLUSTERED INDEX IX_Student_Card_Id
ON Student(Card_Id)
WHERE Card_Id IS NOT NULL;
GO

-- Create unique index on Phone_Id (only for non-null values)
CREATE UNIQUE NONCLUSTERED INDEX IX_Student_Phone_Id
ON Student(Phone_Id)
WHERE Phone_Id IS NOT NULL;
GO

-- Verify the changes
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Student'
AND COLUMN_NAME IN ('Card_Id', 'Phone_Id');
GO
