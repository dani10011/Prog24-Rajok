export interface Course {
  courseId: number;
  subjectName: string;
  startTime: string;
  endTime: string;
  dayOfWeek: string;
  roomNumber: string;
  buildingName: string;
  studentCount: number;
}

export interface CreateCourseRequest {
  subjectId: number;
  instructorId: number;
  startTime: string;
  endTime: string;
  roomId: number;
  nameSuffix?: string;
}
