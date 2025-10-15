export interface TimetableItem {
  courseId: number;
  subjectName: string;
  startTime: string; // ISO 8601 date-time string
  endTime: string; // ISO 8601 date-time string
  dayOfWeek: string; // e.g., "Monday", "Tuesday", etc.
  roomNumber: string;
  buildingName: string;
  instructorName: string | null;
  studentCount: number;
}

export interface TimetableResponse {
  userId: number;
  userName: string;
  userRole: string;
  timetableItems: TimetableItem[];
}

export interface DaySchedule {
  date: Date;
  dayOfWeek: string;
  items: TimetableItem[];
}
