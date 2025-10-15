using Prog24.DataContext.Entities;
using Prog24.Services.Model.Dto;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services.Interfaces
{
    public interface ICourseService
    {
        public Task<List<Course>> GetCourses();
        public Task<List<CourseResponse>> GetCoursesByUserId(int userId);
        public Task<CreateCourseResponse> CreateCourse(CreateCourseRequest request);
    }
}
