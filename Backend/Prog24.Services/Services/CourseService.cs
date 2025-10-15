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
