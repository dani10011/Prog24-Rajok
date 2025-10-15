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
    public class UserService : IUserService
    {
        private readonly AppDbContext _dbContext;
        public UserService(AppDbContext dbContext)
        {
            _dbContext = dbContext;
        }

        public async Task<List<UserInfoResponse>> GetUsers()
        {
            var users = await _dbContext.User
                .Include(u => u.Role)
                .ToListAsync();

            var result = users.Select(user => new UserInfoResponse
            {
                Id = user.Id,
                Name = user.Name,
                RoleId = user.Role_Id,
                RoleName = user.Role.Role_Name,
                Email = user.Email
            }).ToList();

            return result;
        }

        public async Task<UserInfoResponse?> GetUserInfo(int userId)
        {
            var user = await _dbContext.User
                .Include(u => u.Role)
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return null;
            }

            var userInfo = new UserInfoResponse
            {
                Id = user.Id,
                Name = user.Name,
                RoleId = user.Role_Id,
                RoleName = user.Role.Role_Name,
                Email = user.Email
            };

            return userInfo;
        }
    }
}
