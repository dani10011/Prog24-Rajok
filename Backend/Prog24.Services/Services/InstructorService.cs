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
    public class InstructorService : IInstructorService
    {
        private readonly AppDbContext _dbContext;
        public InstructorService(AppDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<List<Instructor>> GetInstructors()
        {
            var result = await _dbContext.Instructor.ToListAsync();
            return result;
        }

        public async Task<List<CourseResponse>?> GetInstructorCourses(int instructorId)
        {
            // Check if instructor exists
            var instructorExists = await _dbContext.Instructor.AnyAsync(i => i.User_Id == instructorId);

            if (!instructorExists)
            {
                return null;
            }

            // Get courses taught by instructor
            var courses = await _dbContext.Course
                .Where(c => c.Instructor_Id == instructorId)
                .Include(c => c.Subject)
                .Include(c => c.Room)
                    .ThenInclude(r => r.Building)
                .Include(c => c.CourseStudents)
                .ToListAsync();

            var courseResponses = courses.Select(course => new CourseResponse
            {
                CourseId = course.Id,
                SubjectName = course.Subject.Name,
                StartTime = course.Start_Time,
                EndTime = course.End_Time,
                DayOfWeek = course.Start_Time.DayOfWeek.ToString(),
                RoomNumber = course.Room.Room_Number,
                BuildingName = course.Room.Building.Name,
                StudentCount = course.CourseStudents.Count
            }).OrderBy(c => c.StartTime).ToList();

            return courseResponses;
        }
    }
}
