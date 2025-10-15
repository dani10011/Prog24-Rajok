export interface Student {
  id: number;
  userId: number;
  email: string;
  neptunCode: string;
  majorId: number;
  majorName?: string;
  // Future fields (to be added later)
  name?: string;
  firstName?: string;
  lastName?: string;
  enrollmentYear?: number;
  status?: string;
}

export interface StudentsResponse {
  students: Student[];
  totalCount?: number;
}
