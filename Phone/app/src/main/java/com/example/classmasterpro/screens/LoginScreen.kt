package com.example.classmasterpro.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classmasterpro.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onShowToast: (String) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Animation for the logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Hardcoded credentials
    val validUsername = "admin"
    val validPassword = "password"

    fun handleLogin() {
        errorMessage = ""
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all fields"
            onShowToast("Please fill in all fields")
            return
        }

        isLoading = true
        scope.launch {
            // Simulate network delay
            delay(1000)

            if (username == validUsername && password == validPassword) {
                isLoading = false
                onShowToast("Login successful!")
                onLoginSuccess()
            } else {
                isLoading = false
                errorMessage = "Invalid username or password"
                onShowToast("Invalid credentials")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(
                            Color(0xFF001F3F),
                            Color(0xFF003366),
                            PrimaryBlue,
                            SecondaryBlue
                        )
                    } else {
                        listOf(
                            PrimaryBlue,
                            SecondaryBlue,
                            LightBlue,
                            SkyBlue
                        )
                    }
                )
            )
    ) {
        // Dark Mode Toggle (Top Left)
        IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle Dark Mode",
                tint = if (isDarkMode) SkyBlue else PaleBlue,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Icon Section
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PaleBlue.copy(alpha = 0.9f),
                                PaleBlue.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "School Logo",
                    modifier = Modifier.size(80.dp),
                    tint = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "ClassMaster Pro",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) SkyBlue else PaleBlue
            )
            Text(
                text = "Sign in to continue",
                fontSize = 16.sp,
                color = if (isDarkMode) SkyBlue.copy(alpha = 0.8f) else PaleBlue.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1A2332).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = ""
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Username",
                                tint = if (isDarkMode) LightBlue else PrimaryBlue
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkMode) LightBlue else PrimaryBlue,
                            focusedLabelColor = if (isDarkMode) LightBlue else PrimaryBlue,
                            unfocusedBorderColor = if (isDarkMode) SecondaryBlue else SecondaryBlue,
                            unfocusedLabelColor = if (isDarkMode) SkyBlue else SecondaryBlue,
                            cursorColor = if (isDarkMode) LightBlue else PrimaryBlue,
                            focusedTextColor = if (isDarkMode) SkyBlue else PrimaryBlue,
                            unfocusedTextColor = if (isDarkMode) SkyBlue else PrimaryBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Password",
                                tint = if (isDarkMode) LightBlue else PrimaryBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = if (isDarkMode) SkyBlue else SecondaryBlue
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                handleLogin()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkMode) LightBlue else PrimaryBlue,
                            focusedLabelColor = if (isDarkMode) LightBlue else PrimaryBlue,
                            unfocusedBorderColor = if (isDarkMode) SecondaryBlue else SecondaryBlue,
                            unfocusedLabelColor = if (isDarkMode) SkyBlue else SecondaryBlue,
                            cursorColor = if (isDarkMode) LightBlue else PrimaryBlue,
                            focusedTextColor = if (isDarkMode) SkyBlue else PrimaryBlue,
                            unfocusedTextColor = if (isDarkMode) SkyBlue else PrimaryBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Error Message
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                color = Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Login Button
                    Button(
                        onClick = { handleLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Demo Credentials Hint
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SkyBlue.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Demo Credentials",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryBlue
                            )
                            Text(
                                text = "Username: admin",
                                fontSize = 11.sp,
                                color = SecondaryBlue.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Password: password",
                                fontSize = 11.sp,
                                color = SecondaryBlue.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "Secure access for students and staff",
                fontSize = 14.sp,
                color = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else PaleBlue.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Login Screen - Light Mode")
@Composable
fun LoginScreenPreview() {
    ClassMasterProTheme(dynamicColor = false) {
        LoginScreen(
            onLoginSuccess = {},
            onShowToast = {},
            isDarkMode = false,
            onToggleDarkMode = {}
        )
    }
}

@Preview(showBackground = true, name = "Login Screen - Dark Mode")
@Composable
fun LoginScreenDarkPreview() {
    ClassMasterProTheme(dynamicColor = false) {
        LoginScreen(
            onLoginSuccess = {},
            onShowToast = {},
            isDarkMode = true,
            onToggleDarkMode = {}
        )
    }
}
