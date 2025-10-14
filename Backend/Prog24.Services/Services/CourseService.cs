using Microsoft.EntityFrameworkCore;
using Prog24.DataContext;
using Prog24.DataContext.Entities;
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
    }
}
