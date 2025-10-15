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
    public class CourseService : ICourseService
    {
        private readonly AppDbContext _dbContext;
        public CourseService(AppDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<List<Course>> GetCourses()
        {
            var result = await _dbContext.Course.ToListAsync();
            return result;
        }

        public async Task<List<CourseResponse>> GetCoursesByUserId(int userId)
        {
            // Check if user is an Instructor
            var isInstructor = await _dbContext.Instructor
                .AnyAsync(i => i.User_Id == userId);

            List<Course> courses;

            if (isInstructor)
            {
                // Get courses where the user is the instructor
                courses = await _dbContext.Course
                    .Include(c => c.Subject)
                    .Include(c => c.Room)
                        .ThenInclude(r => r.Building)
                    .Include(c => c.CourseStudents)
                    .Where(c => c.Instructor_Id == userId)
                    .ToListAsync();
            }
            else
            {
                // Check if user is a Student
                var isStudent = await _dbContext.Student
                    .AnyAsync(s => s.User_Id == userId);

                if (!isStudent)
                {
                    throw new Exception($"User with ID {userId} is neither an Instructor nor a Student");
                }

                // Get courses where the user is enrolled as a student
                courses = await _dbContext.Course_student
                    .Include(cs => cs.Course)
                        .ThenInclude(c => c.Subject)
                    .Include(cs => cs.Course)
                        .ThenInclude(c => c.Room)
                            .ThenInclude(r => r.Building)
                    .Include(cs => cs.Course)
                        .ThenInclude(c => c.CourseStudents)
                    .Where(cs => cs.Student_Id == userId)
                    .Select(cs => cs.Course)
                    .ToListAsync();
            }

            // Map to CourseResponse DTOs
            return courses.Select(c => MapToCourseResponse(c)).ToList();
        }

        public async Task<CreateCourseResponse> CreateCourse(CreateCourseRequest request)
        {
            // Validate instructor exists
            var instructor = await _dbContext.Instructor
                .FirstOrDefaultAsync(i => i.User_Id == request.InstructorId);

            if (instructor == null)
            {
                throw new Exception($"Instructor with ID {request.InstructorId} not found");
            }

            // Validate subject exists and belongs to instructor
            var originalSubject = await _dbContext.Subject
                .FirstOrDefaultAsync(s => s.Id == request.SubjectId && s.Instructor_Id == request.InstructorId);

            if (originalSubject == null)
            {
                throw new Exception($"Subject with ID {request.SubjectId} not found or does not belong to instructor {request.InstructorId}");
            }

            // Validate room exists
            var room = await _dbContext.Room
                .Include(r => r.Building)
                .FirstOrDefaultAsync(r => r.Id == request.RoomId);

            if (room == null)
            {
                throw new Exception($"Room with ID {request.RoomId} not found");
            }

            // Validate time range
            if (request.StartTime >= request.EndTime)
            {
                throw new Exception("Start time must be before end time");
            }

            // Check for room conflicts
            var hasConflict = await _dbContext.Course
                .AnyAsync(c => c.Room_Id == request.RoomId &&
                    ((request.StartTime >= c.Start_Time && request.StartTime < c.End_Time) ||
                     (request.EndTime > c.Start_Time && request.EndTime <= c.End_Time) ||
                     (request.StartTime <= c.Start_Time && request.EndTime >= c.End_Time)));

            if (hasConflict)
            {
                throw new Exception($"Room {room.Room_Number} is already booked during the specified time");
            }

            // Create or find subject with suffix (e.g., "Mathematics ZH")
            var suffix = string.IsNullOrWhiteSpace(request.NameSuffix) ? "ZH" : request.NameSuffix.Trim();
            var newSubjectName = $"{originalSubject.Name} {suffix}";

            var existingSubject = await _dbContext.Subject
                .FirstOrDefaultAsync(s => s.Name == newSubjectName && s.Instructor_Id == request.InstructorId);

            Subject subjectForCourse;
            bool createdNewSubject = false;

            if (existingSubject != null)
            {
                subjectForCourse = existingSubject;
            }
            else
            {
                // Create new subject with the suffix
                subjectForCourse = new Subject
                {
                    Name = newSubjectName,
                    Instructor_Id = request.InstructorId
                };

                _dbContext.Subject.Add(subjectForCourse);
                await _dbContext.SaveChangesAsync();
                createdNewSubject = true;
            }

            // Create the course
            var newCourse = new Course
            {
                Subject_Id = subjectForCourse.Id,
                Instructor_Id = request.InstructorId,
                Start_Time = request.StartTime,
                End_Time = request.EndTime,
                Room_Id = request.RoomId
            };

            _dbContext.Course.Add(newCourse);
            await _dbContext.SaveChangesAsync();

            // Return response
            return new CreateCourseResponse
            {
                CourseId = newCourse.Id,
                SubjectId = subjectForCourse.Id,
                SubjectName = subjectForCourse.Name,
                InstructorId = request.InstructorId,
                StartTime = newCourse.Start_Time,
                EndTime = newCourse.End_Time,
                RoomId = room.Id,
                RoomNumber = room.Room_Number,
                BuildingName = room.Building.Name,
                Message = createdNewSubject
                    ? $"Course created successfully with new subject '{newSubjectName}'"
                    : $"Course created successfully using existing subject '{newSubjectName}'"
            };
        }

        private CourseResponse MapToCourseResponse(Course course)
        {
            return new CourseResponse
            {
                CourseId = course.Id,
                SubjectName = course.Subject.Name,
                StartTime = course.Start_Time,
                EndTime = course.End_Time,
                DayOfWeek = course.Start_Time.DayOfWeek.ToString(),
                RoomNumber = course.Room.Room_Number,
                BuildingName = course.Room.Building.Name,
                StudentCount = course.CourseStudents.Count
            };
        }
    }
}
