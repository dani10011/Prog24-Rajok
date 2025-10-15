using Microsoft.EntityFrameworkCore;
using Prog24.DataContext;
using Prog24.DataContext.Entities;
using Prog24.Services.Model.Dto;
using Prog24.Services.Services.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services
{
    public class StudentService : IStudentService
    {
        private readonly AppDbContext _dbContext;
        public StudentService(AppDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<List<StudentResponse>> GetStudents()
        {
            var students = await _dbContext.Student
                .Include(s => s.User)
                .Include(s => s.Major)
                .ToListAsync();

            var studentResponses = students.Select(student => new StudentResponse
            {
                UserId = student.User_Id,
                Name = student.User.Name,
                NeptunCode = student.User.Neptun_Code,
                Email = student.User.Email,
                Gender = student.Gender,
                MajorId = student.Major_Id,
                MajorName = student.Major.Name
            }).ToList();

            return studentResponses;
        }

        public async Task<List<StudentInstructorResponse>> GetStudentInstructors(int studentUserId)
        {
            // Get all courses the student is enrolled in
            var courseEnrollments = await _dbContext.Course_student
                .Where(cs => cs.Student_Id == studentUserId)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Subject)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Instructor)
                    .ThenInclude(i => i.User)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Room)
                .ToListAsync();

            // Group courses by instructor
            var instructorGroups = courseEnrollments
                .GroupBy(cs => cs.Course.Instructor_Id)
                .Select(group =>
                {
                    var firstCourse = group.First().Course;
                    return new StudentInstructorResponse
                    {
                        InstructorId = firstCourse.Instructor_Id,
                        InstructorName = firstCourse.Instructor.User.Name,
                        Courses = group.Select(cs => new InstructorCourseInfo
                        {
                            CourseId = cs.Course.Id,
                            SubjectName = cs.Course.Subject.Name,
                            StartTime = cs.Course.Start_Time,
                            EndTime = cs.Course.End_Time,
                            RoomNumber = cs.Course.Room.Room_Number
                        }).ToList()
                    };
                })
                .ToList();

            return instructorGroups;
        }

        public async Task<List<TimetableItemResponse>> GetStudentCourseLocations(int studentUserId)
        {
            // Get all courses the student is enrolled in
            var studentCourses = await _dbContext.Course_student
                .Where(cs => cs.Student_Id == studentUserId)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Subject)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Room)
                        .ThenInclude(r => r.Building)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Instructor)
                        .ThenInclude(i => i.User)
                .Select(cs => cs.Course)
                .ToListAsync();

            // Map to TimetableItemResponse
            var courseLocations = studentCourses.Select(course => new TimetableItemResponse
            {
                CourseId = course.Id,
                SubjectName = course.Subject.Name,
                StartTime = course.Start_Time,
                EndTime = course.End_Time,
                DayOfWeek = course.Start_Time.DayOfWeek.ToString(),
                RoomNumber = course.Room.Room_Number,
                BuildingName = course.Room.Building.Name,
                InstructorName = course.Instructor.User.Name,
                StudentCount = null
            }).OrderBy(t => t.StartTime).ToList();

            return courseLocations;
        }

        public async Task<StudentCurrentLectureStatusResponse> GetCurrentLectureStatus(int studentUserId)
        {
            var currentTime = DateTime.UtcNow;

            // Find the student's current active course (enrolled + time matches)
            var currentCourse = await _dbContext.Course_student
                .Where(cs => cs.Student_Id == studentUserId)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Subject)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Room)
                        .ThenInclude(r => r.Building)
                .Include(cs => cs.Course)
                    .ThenInclude(c => c.Instructor)
                        .ThenInclude(i => i.User)
                .Select(cs => cs.Course)
                .Where(c => c.Start_Time <= currentTime && c.End_Time >= currentTime)
                .FirstOrDefaultAsync();

            // If no current course, return empty response
            if (currentCourse == null)
            {
                return new StudentCurrentLectureStatusResponse
                {
                    CourseId = null,
                    SubjectName = null,
                    RoomNumber = null,
                    BuildingName = null,
                    InstructorName = null,
                    StartTime = null,
                    EndTime = null,
                    IsInAttendance = false,
                    EntryTime = null,
                    HasPendingRequest = false,
                    PendingRequestId = null,
                    RequestReason = null
                };
            }

            // Check if student is currently in attendance (Exit_Time is NULL)
            var activeAttendance = await _dbContext.Student_class_attendance
                .FirstOrDefaultAsync(sca => sca.Student_Id == studentUserId
                    && sca.Course_Id == currentCourse.Id
                    && sca.Exit_Time == null);

            // Check for pending room entry request for this course
            var pendingRequest = await _dbContext.Room_entry_request
                .Where(r => r.Student_Id == studentUserId
                    && r.Course_Id == currentCourse.Id
                    && r.Status == "Pending")
                .OrderByDescending(r => r.Request_Time)
                .FirstOrDefaultAsync();

            return new StudentCurrentLectureStatusResponse
            {
                CourseId = currentCourse.Id,
                SubjectName = currentCourse.Subject.Name,
                RoomNumber = currentCourse.Room.Room_Number,
                BuildingName = currentCourse.Room.Building.Name,
                InstructorName = currentCourse.Instructor.User.Name,
                StartTime = currentCourse.Start_Time,
                EndTime = currentCourse.End_Time,
                IsInAttendance = activeAttendance != null,
                EntryTime = activeAttendance?.Entry_Time,
                HasPendingRequest = pendingRequest != null,
                PendingRequestId = pendingRequest?.Id,
                RequestReason = pendingRequest?.Reason
            };
        }
    }
}
