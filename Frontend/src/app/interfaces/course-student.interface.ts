export interface CourseStudent {
  course_id: number;
  student_id: number;
  enrolled_on: string;
}

export interface Course {
  id: number;
  name: string;
  description?: string;
  teacher_id?: number;
  teacher_name?: string;
  room?: string;
  day_of_week?: number; // 0-6 (Vasárnap-Szombat)
  start_time?: string;
  end_time?: string;
  color?: string;
}

export interface ScheduleEvent {
  id: number;
  title: string;
  start: Date;
  end: Date;
  course: Course;
  color?: string;
}
