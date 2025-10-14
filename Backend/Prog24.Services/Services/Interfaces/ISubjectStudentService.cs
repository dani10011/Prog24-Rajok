using Prog24.DataContext.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services.Interfaces
{
    public interface ISubjectStudentService
    {
        public Task<List<SubjectStudent>> GetSubjectStudents();
    }
}
