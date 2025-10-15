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
                Email = student.User.Email,
                Gender = student.Gender,
                MajorId = student.Major_Id,
                MajorName = student.Major.Name
            }).ToList();

            return studentResponses;
        }
    }
}
