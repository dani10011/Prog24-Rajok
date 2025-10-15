package com.example.classmasterpro.screens

import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.content.ComponentName
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.classmasterpro.ui.theme.*
import com.example.classmasterpro.utils.ApiHelper
import com.example.classmasterpro.utils.AuthPreferences
import com.example.classmasterpro.models.UserRole
import com.example.classmasterpro.models.CurrentLectureStatusResponse
import com.example.classmasterpro.nfc.CardEmulationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

enum class NFCStatus {
    NOT_SUPPORTED,
    DISABLED,
    ENABLED
}

@Composable
fun NFCScannerScreen(
    nfcAdapter: NfcAdapter?,
    onShowToast: (String) -> Unit,
    onLogout: () -> Unit = {},
    onOpenBlackjack: () -> Unit = {},
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val context = LocalContext.current
    var nfcStatus by remember { mutableStateOf(NFCStatus.DISABLED) }
    var lectureStatus by remember { mutableStateOf<CurrentLectureStatusResponse?>(null) }
    var isLoadingLectureStatus by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Get user info from preferences
    val userName = AuthPreferences.getName(context) ?: "Student"
    val roleId = AuthPreferences.getRoleId(context)
    val userRole = UserRole.fromId(roleId)?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Student"
    val userId = AuthPreferences.getUserId(context)

    // Easter egg corner tap detection
    var tapSequence by remember { mutableStateOf(listOf<String>()) }
    var lastTapTime by remember { mutableStateOf(0L) }

    // Check NFC status
    LaunchedEffect(Unit) {
        nfcStatus = when {
            nfcAdapter == null -> NFCStatus.NOT_SUPPORTED
            nfcAdapter.isEnabled -> NFCStatus.ENABLED
            else -> NFCStatus.DISABLED
        }

        // Set the NFC UID from the saved phoneId, or fallback to userId
        val savedPhoneId = AuthPreferences.getPhoneId(context)
        if (!savedPhoneId.isNullOrEmpty()) {
            CardEmulationService.setUidFromPhoneId(savedPhoneId)
        } else {
            if (userId > 0) {
                CardEmulationService.setUidFromStudentId(userId)
            }
        }
    }

    // Animated scale for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

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
                contentDescription = "Toggle Dark Mode",
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
                        text = "ClassMaster Pro",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "NFC Class Entry System",
                        fontSize = 16.sp,
                        color = SecondaryBlue,
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
                        contentDescription = "Logout",
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

            // Main NFC Icon and Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Large NFC Icon with pulsing animation
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(if (nfcStatus == NFCStatus.ENABLED) scale else 1f)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryBlue.copy(alpha = 0.2f),
                                    SecondaryBlue.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Nfc,
                        contentDescription = "NFC",
                        modifier = Modifier.size(120.dp),
                        tint = when (nfcStatus) {
                            NFCStatus.ENABLED -> PrimaryBlue
                            NFCStatus.DISABLED -> Accent
                            NFCStatus.NOT_SUPPORTED -> Color.Gray
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1A2332).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (nfcStatus) {
                                NFCStatus.ENABLED -> Icons.Filled.CheckCircle
                                NFCStatus.DISABLED -> Icons.Filled.Warning
                                NFCStatus.NOT_SUPPORTED -> Icons.Filled.Error
                            },
                            contentDescription = null,
                            tint = when (nfcStatus) {
                                NFCStatus.ENABLED -> PrimaryBlue
                                NFCStatus.DISABLED -> Accent
                                NFCStatus.NOT_SUPPORTED -> Color.Gray
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (nfcStatus) {
                                NFCStatus.ENABLED -> "NFC is Ready"
                                NFCStatus.DISABLED -> "NFC is Disabled"
                                NFCStatus.NOT_SUPPORTED -> "NFC Not Supported"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (nfcStatus) {
                                NFCStatus.ENABLED -> PrimaryBlue
                                NFCStatus.DISABLED -> Accent
                                NFCStatus.NOT_SUPPORTED -> Color.Gray
                            }
                        )
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Check NFC Status Button
                Button(
                    onClick = {
                        nfcStatus = when {
                            nfcAdapter == null -> NFCStatus.NOT_SUPPORTED
                            nfcAdapter.isEnabled -> NFCStatus.ENABLED
                            else -> NFCStatus.DISABLED
                        }

                        onShowToast("NFC: ${nfcStatus.name}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        text = "Check NFC Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Get Current Lecture Status Button
                Button(
                    onClick = {
                        scope.launch {
                            isLoadingLectureStatus = true
                            try {
                                val token = AuthPreferences.getToken(context)
                                if (token == null) {
                                    onShowToast("Not logged in")
                                    return@launch
                                }

                                val status = ApiHelper.getCurrentLectureStatus(userId, token)
                                lectureStatus = status

                                // Display a toast with the lecture status
                                if (status.isInLecture) {
                                    val message = buildString {
                                        append("In Lecture")
                                        status.courseName?.let { append(": $it") }
                                        status.roomNumber?.let { append(" - Room $it") }
                                        status.attendanceStatus?.let { append(" ($it)") }
                                    }
                                    onShowToast(message)
                                } else {
                                    onShowToast(status.message ?: "No active lecture")
                                }
                            } catch (e: Exception) {
                                onShowToast("Error: ${e.message}")
                                lectureStatus = null
                            } finally {
                                isLoadingLectureStatus = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryBlue
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    enabled = !isLoadingLectureStatus
                ) {
                    if (isLoadingLectureStatus) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = "Lecture Status",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Check Lecture Status",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Display lecture status card if available
                lectureStatus?.let { status ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (status.isInLecture) {
                                if (isDarkMode) PrimaryBlue.copy(alpha = 0.3f) else LightBlue.copy(alpha = 0.3f)
                            } else {
                                if (isDarkMode) Color(0xFF1A2332).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (status.isInLecture) "Active Lecture" else "No Active Lecture",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) SkyBlue else PrimaryBlue
                                )
                                Icon(
                                    imageVector = if (status.isInLecture) Icons.Filled.CheckCircle else Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = if (status.isInLecture) PrimaryBlue else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (status.isInLecture) {
                                Spacer(modifier = Modifier.height(8.dp))
                                status.courseName?.let {
                                    Text(
                                        text = "Course: $it",
                                        fontSize = 12.sp,
                                        color = if (isDarkMode) SkyBlue.copy(alpha = 0.9f) else SecondaryBlue
                                    )
                                }
                                status.instructorName?.let {
                                    Text(
                                        text = "Instructor: $it",
                                        fontSize = 12.sp,
                                        color = if (isDarkMode) SkyBlue.copy(alpha = 0.9f) else SecondaryBlue
                                    )
                                }
                                status.roomNumber?.let { room ->
                                    Text(
                                        text = "Room: $room${status.buildingName?.let { " ($it)" } ?: ""}",
                                        fontSize = 12.sp,
                                        color = if (isDarkMode) SkyBlue.copy(alpha = 0.9f) else SecondaryBlue
                                    )
                                }
                                status.attendanceStatus?.let {
                                    Text(
                                        text = "Status: $it",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when (it.lowercase()) {
                                            "present" -> if (isDarkMode) LightBlue else PrimaryBlue
                                            "absent" -> Accent
                                            "late" -> if (isDarkMode) SkyBlue else SecondaryBlue
                                            else -> if (isDarkMode) SkyBlue else SecondaryBlue
                                        }
                                    )
                                }
                            } else {
                                status.message?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it,
                                        fontSize = 12.sp,
                                        color = if (isDarkMode) SkyBlue.copy(alpha = 0.7f) else SecondaryBlue.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Easter egg corner tap zones (invisible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left corner tap zone
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val currentTime = System.currentTimeMillis()
                        // Reset sequence if too much time has passed (3 seconds)
                        if (currentTime - lastTapTime > 3000) {
                            tapSequence = listOf("left")
                        } else {
                            tapSequence = tapSequence + "left"
                        }
                        lastTapTime = currentTime

                        // Check for correct pattern: left, right, left, right
                        if (tapSequence.size >= 4) {
                            val lastFour = tapSequence.takeLast(4)
                            if (lastFour == listOf("left", "right", "left", "right")) {
                                onOpenBlackjack()
                                tapSequence = emptyList() // Reset after opening
                            }
                        }
                    }
            )

            // Right corner tap zone
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val currentTime = System.currentTimeMillis()
                        // Reset sequence if too much time has passed (3 seconds)
                        if (currentTime - lastTapTime > 3000) {
                            tapSequence = listOf("right")
                        } else {
                            tapSequence = tapSequence + "right"
                        }
                        lastTapTime = currentTime

                        // Check for correct pattern: left, right, left, right
                        if (tapSequence.size >= 4) {
                            val lastFour = tapSequence.takeLast(4)
                            if (lastFour == listOf("left", "right", "left", "right")) {
                                onOpenBlackjack()
                                tapSequence = emptyList() // Reset after opening
                            }
                        }
                    }
            )
        }
    }
}

@Preview(showBackground = true, name = "NFC Scanner - Light Mode - Enabled")
@Composable
fun NFCScannerScreenPreview() {
    ClassMasterProTheme(dynamicColor = false) {
        NFCScannerScreen(
            nfcAdapter = null, // Will show as "Not Supported" in preview
            onShowToast = {},
            onLogout = {},
            onOpenBlackjack = {},
            isDarkMode = false,
            onToggleDarkMode = {}
        )
    }
}

@Preview(showBackground = true, name = "NFC Scanner - Dark Mode")
@Composable
fun NFCScannerScreenDarkPreview() {
    ClassMasterProTheme(dynamicColor = false) {
        NFCScannerScreen(
            nfcAdapter = null,
            onShowToast = {},
            onLogout = {},
            onOpenBlackjack = {},
            isDarkMode = true,
            onToggleDarkMode = {}
        )
    }
}
