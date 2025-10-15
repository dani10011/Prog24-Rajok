package com.example.classmasterpro.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classmasterpro.R
import com.example.classmasterpro.models.UserRole
import com.example.classmasterpro.models.RoomEntryRequestResponse
import com.example.classmasterpro.models.ApproveStudentEntryRequest
import com.example.classmasterpro.ui.theme.*
import com.example.classmasterpro.utils.AuthPreferences
import com.example.classmasterpro.utils.ApiHelper
import kotlinx.coroutines.launch

/**
 * Instructor Dashboard Screen
 * Allows instructors to manage student entry
 */
@Composable
fun InstructorScreen(
    onShowToast: (String) -> Unit,
    onLogout: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Get user info from preferences
    val userName = AuthPreferences.getName(context) ?: "Instructor"
    val roleId = AuthPreferences.getRoleId(context)
    val userRole = UserRole.fromId(roleId)?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Instructor"
    val instructorId = AuthPreferences.getUserId(context)
    val token = AuthPreferences.getToken(context) ?: ""

    // State for pending requests
    var pendingRequests by remember { mutableStateOf<List<RoomEntryRequestResponse>>(emptyList()) }
    var isLoadingRequests by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Function to load pending requests
    fun loadPendingRequests() {
        scope.launch {
            isLoadingRequests = true
            try {
                android.util.Log.d("InstructorScreen", "Loading requests - instructorId: $instructorId, token: ${token.take(20)}...")

                if (token.isEmpty()) {
                    onShowToast("Not logged in - please re-login")
                    pendingRequests = emptyList()
                    return@launch
                }

                val requests = ApiHelper.getPendingRequestsForOngoingLecture(instructorId, token)
                android.util.Log.d("InstructorScreen", "Received ${requests.size} pending requests")
                pendingRequests = requests
                if (requests.isEmpty()) {
                    onShowToast("No pending requests")
                }
            } catch (e: Exception) {
                android.util.Log.e("InstructorScreen", "Error loading requests", e)
                onShowToast("Error: ${e.message?.take(100) ?: "Unknown error"}")
                pendingRequests = emptyList()
            } finally {
                isLoadingRequests = false
                isRefreshing = false
            }
        }
    }

    // Load requests on first render
    LaunchedEffect(Unit) {
        loadPendingRequests()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(
                            Color(0xFF0A1929),
                            Color(0xFF001F3F),
                            PrimaryBlue.copy(alpha = 0.6f),
                            SecondaryBlue.copy(alpha = 0.4f)
                        )
                    } else {
                        listOf(
                            PaleBlue,
                            SkyBlue,
                            LightBlue.copy(alpha = 0.5f),
                            SecondaryBlue.copy(alpha = 0.2f)
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
                .offset(y = 32.dp)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = context.getString(R.string.toggle_dark_mode),
                tint = if (isDarkMode) SkyBlue else PrimaryBlue,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Logout Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = context.getString(R.string.instructor_title),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) SkyBlue else PrimaryBlue
                    )
                    Text(
                        text = context.getString(R.string.instructor_subtitle),
                        fontSize = 16.sp,
                        color = if (isDarkMode) SkyBlue.copy(alpha = 0.8f) else SecondaryBlue,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Logout Button
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = context.getString(R.string.logout),
                        tint = Accent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // User Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1A2332).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = userName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) SkyBlue else PrimaryBlue
                        )
                        Text(
                            text = userRole,
                            fontSize = 14.sp,
                            color = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else SecondaryBlue
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "User",
                        tint = if (isDarkMode) LightBlue else PrimaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content - Pending Requests List
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pending Entry Requests",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) SkyBlue else PrimaryBlue
                        )
                        Text(
                            text = "${pendingRequests.size} waiting",
                            fontSize = 12.sp,
                            color = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else SecondaryBlue
                        )
                    }

                    IconButton(
                        onClick = {
                            isRefreshing = true
                            loadPendingRequests()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isDarkMode) LightBlue else PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pending Requests List
                if (isLoadingRequests && pendingRequests.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue
                        )
                    }
                } else if (pendingRequests.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) Color(0xFF1A2332).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isDarkMode) SkyBlue else PrimaryBlue,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Pending Requests",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDarkMode) SkyBlue else PrimaryBlue,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Students waiting to enter will appear here",
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else SecondaryBlue,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pendingRequests) { request ->
                            PendingRequestCard(
                                request = request,
                                isDarkMode = isDarkMode,
                                onApprove = {
                                    scope.launch {
                                        try {
                                            ApiHelper.approveStudentEntry(
                                                ApproveStudentEntryRequest(
                                                    instructorId = instructorId,
                                                    studentId = request.studentId,
                                                    isApproved = true
                                                ),
                                                token
                                            )
                                            onShowToast("Approved ${request.studentName}")
                                            loadPendingRequests()
                                        } catch (e: Exception) {
                                            onShowToast("Error: ${e.message}")
                                        }
                                    }
                                },
                                onDeny = {
                                    scope.launch {
                                        try {
                                            ApiHelper.approveStudentEntry(
                                                ApproveStudentEntryRequest(
                                                    instructorId = instructorId,
                                                    studentId = request.studentId,
                                                    isApproved = false
                                                ),
                                                token
                                            )
                                            onShowToast("Denied ${request.studentName}")
                                            loadPendingRequests()
                                        } catch (e: Exception) {
                                            onShowToast("Error: ${e.message}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Card displaying a single pending entry request
 */
@Composable
fun PendingRequestCard(
    request: RoomEntryRequestResponse,
    isDarkMode: Boolean,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1A2332).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Student Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (isDarkMode) LightBlue else PrimaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = request.studentName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) SkyBlue else PrimaryBlue
                        )
                        Text(
                            text = request.studentEmail,
                            fontSize = 12.sp,
                            color = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else SecondaryBlue
                        )
                    }
                }
            }

            // Course and Room Info
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = if (isDarkMode) SkyBlue.copy(alpha = 0.2f) else PrimaryBlue.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Course Info
                request.courseName?.let { courseName ->
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Book,
                                contentDescription = null,
                                tint = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else SecondaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = courseName,
                                fontSize = 12.sp,
                                color = if (isDarkMode) SkyBlue.copy(alpha = 0.9f) else SecondaryBlue
                            )
                        }
                    }
                }

                // Room Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Room,
                            contentDescription = null,
                            tint = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else SecondaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Room ${request.roomNumber}",
                            fontSize = 12.sp,
                            color = if (isDarkMode) SkyBlue.copy(alpha = 0.9f) else SecondaryBlue
                        )
                    }
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Deny Button
                Button(
                    onClick = onDeny,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Deny",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Approve Button
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Approve",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Instructor Screen - Light Mode")
@Composable
fun InstructorScreenPreview() {
    ClassMasterProTheme(dynamicColor = false) {
        InstructorScreen(
            onShowToast = {},
            onLogout = {},
            isDarkMode = false,
            onToggleDarkMode = {}
        )
    }
}

@Preview(showBackground = true, name = "Instructor Screen - Dark Mode")
@Composable
fun InstructorScreenDarkPreview() {
    ClassMasterProTheme(dynamicColor = false) {
        InstructorScreen(
            onShowToast = {},
            onLogout = {},
            isDarkMode = true,
            onToggleDarkMode = {}
        )
    }
}
