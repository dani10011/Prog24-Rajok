using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using Prog24.DataContext;
using Prog24.DataContext.Entities;
using Prog24.Services.Model.Dto;
using Prog24.Services.Services.Interfaces;
using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Security.Claims;
using System.Text;
using System.Threading.Tasks;

namespace Prog24.Services.Services
{
    public class AuthService : IAuthService
    {
        private readonly IConfiguration _configuration;
        private readonly AppDbContext _appDbContext;
        public AuthService(IConfiguration configuration, AppDbContext appDbContext)
        {
            _configuration = configuration;
            _appDbContext = appDbContext;
        }
        public async Task<LoginResponse> Login(LoginRequest loginRequest)
        {
            User user;

            user = _appDbContext.User.FirstOrDefault(u => u.Email == loginRequest.Email);

            if (user != null)
            {
                var isValid = BCrypt.Net.BCrypt.Verify(loginRequest.Password, user.Password_Hash);

                if (isValid)
                {
                    // Load role information
                    var role = _appDbContext.Role.FirstOrDefault(r => r.Id == user.Role_Id);
                    
                    var authClaims = new List<Claim>
                    {
                        new(ClaimTypes.NameIdentifier, user.Id.ToString()),
                        new(ClaimTypes.Name, user.Name),
                        new(ClaimTypes.Email, user.Email),
                        new(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
                        new(JwtRegisteredClaimNames.AuthTime, DateTime.Now.ToString(CultureInfo.InvariantCulture))
                    };

                    // Add role claim if role exists
                    if (role != null)
                    {
                        authClaims.Add(new Claim(ClaimTypes.Role, role.Role_Name));
                    }

                    var token = GenerateJwtToken(authClaims);

                    // Check if user is a student and get their Phone_Id
                    string? phoneId = null;
                    var student = _appDbContext.Student.FirstOrDefault(s => s.User_Id == user.Id);
                    if (student != null)
                    {
                        phoneId = student.Phone_Id;
                    }

                    return new LoginResponse()
                    {
                        UserId = user.Id,
                        Token = new JwtSecurityTokenHandler().WriteToken(token),
                        PhoneId = phoneId
                    };
                }
                else
                {
                    throw new Exception("Invalid email or password");
                }
            }
            else
            {
                throw new Exception("Invalid email or password");
            }

            
        }

        public async Task Register(RegisterRequest registerRequest)
        {
            User user = new User();

            user.Email = registerRequest.Email;
            string passwordHash = BCrypt.Net.BCrypt.HashPassword(registerRequest.Password);
            user.Password_Hash = passwordHash;
            user.Role_Id = registerRequest.RoleId;
            user.Name = registerRequest.Name;
            user.Neptun_Code = registerRequest.NeptunCode;

            _appDbContext.User.Add(user);
            await _appDbContext.SaveChangesAsync();
        }

        private JwtSecurityToken GenerateJwtToken(List<Claim> authClaims)
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration.GetSection("JwtSettings")["JwtKey"]));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
            var expireDays = Convert.ToDouble(_configuration.GetSection("JwtSettings")["JwtExpireDays"]);
            var expires = DateTime.Now.AddDays(expireDays);

            var token = new JwtSecurityToken(_configuration.GetSection("JwtSettings")["JwtIssuer"],
                _configuration.GetSection("JwtSettings")["JwtAudience"],
                authClaims,
                expires: expires,
                signingCredentials: creds);

            return token;
        }
    }
}
