export interface Subject {
  id: number;
  name: string;
  instructor_Id: number;
  instructor?: any;
  courses?: any[];
  subjectStudents?: any[];
}
