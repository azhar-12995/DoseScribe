package com.azhar.dosescribe.ui.feature.menu

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.ui.feature.dashboard.allModules
import com.azhar.dosescribe.ui.feature.dashboard.pharmacistAvatarIcons
import com.azhar.dosescribe.ui.feature.lessons.LessonProgressViewModel
import java.io.ByteArrayOutputStream

private val BrandBlue = Color(0xFF0982BA)
private val BrandBlueLight = Color(0xFFE8F4F8)
private val SurfaceBg = Color(0xFFF5F7FA)
private val CorrectGreen = Color(0xFF2E7D32)

// ════════════════════════════════════════════════════════════════════════
//  SIMPLE TOP BAR (shared)
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun SimpleTopBar(title: String, onBackClick: () -> Unit) {
    Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  SUPPORT SCREEN
// ════════════════════════════════════════════════════════════════════════
@Composable
fun SupportScreen(
    navController: NavController,
    feedbackViewModel: FeedbackViewModel = hiltViewModel()
) {
    val userEmail by feedbackViewModel.userEmail.collectAsState()
    val submitted by feedbackViewModel.submitted.collectAsState()
    var feedbackMessage by remember { mutableStateOf("") }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SimpleTopBar(title = "Support & Feedback", onBackClick = { navController.popBackStack() })

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Support", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text("How can we assist you today?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                SettingsCard(
                    icon = Icons.Filled.HeadsetMic,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "Contact Us",
                    subtitle = "Reach out to our support team",
                    extra = "dosescribe1@gmail.com",
                    onClick = { }
                )
                Spacer(Modifier.height(12.dp))

                SettingsCard(
                    icon = Icons.Filled.Info,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "About The App",
                    subtitle = "Learn more about DoseScribe",
                    onClick = { }
                )

                Spacer(Modifier.height(32.dp))

                Text("Feedback", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text("Help us improve DoseScribe", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = feedbackMessage,
                    onValueChange = { feedbackMessage = it },
                    label = { Text("What's on your mind?") },
                    placeholder = { Text("Type here....") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        feedbackViewModel.submitFeedback(feedbackMessage)
                        feedbackMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = feedbackMessage.isNotBlank() && !submitted
                ) {
                    Text(if (submitted) "✓ Feedback Submitted" else "Submit Feedback")
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  CERTIFICATES SCREEN (only shows completed courses)
// ════════════════════════════════════════════════════════════════════════
@Composable
fun CertificatesScreen(
    navController: NavController,
    progressVm: LessonProgressViewModel = hiltViewModel()
) {
    @Suppress("UNUSED_VARIABLE")
    val progressVersion = progressVm.version  // observe version for reactivity
    val completedModules = progressVm.getCompletedModules()

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SimpleTopBar(title = "Certificates", onBackClick = { navController.popBackStack() })

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Certificates", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text("Your completed lessons and earned certificates are listed below:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                if (completedModules.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No Certificates Yet", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            Text("Complete lessons to earn certificates", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                } else {
                    completedModules.forEach { module ->
                        CertificateCard(module.title, "View and download your certificate")
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color(0xFFF0A030), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("Download PDF", style = MaterialTheme.typography.labelMedium, color = BrandBlue, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  ACCOUNT SCREEN (with real user data, avatar picker)
// ════════════════════════════════════════════════════════════════════════
@Composable
fun AccountScreen(
    navController: NavController,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val email by viewModel.email.collectAsState()
    val avatarId by viewModel.avatarId.collectAsState()
    val profileImageBase64 by viewModel.profileImageBase64.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val baos = ByteArrayOutputStream()
                    val scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                    scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    val base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                    viewModel.updateProfileImage(base64)
                }
            } catch (_: Exception) { }
        }
    }

    // Camera picker
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val baos = ByteArrayOutputStream()
            val scaled = Bitmap.createScaledBitmap(it, 200, 200, true)
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            viewModel.updateProfileImage(base64)
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully!")
            viewModel.resetSaveSuccess()
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About DoseScribe") },
            text = {
                Column {
                    Text("DoseScribe v1.0", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("DoseScribe is a comprehensive pharmacist training application designed to help pharmacy professionals improve their clinical skills through interactive simulations, learning modules, and assessments.")
                    Spacer(Modifier.height(8.dp))
                    Text("Features:", fontWeight = FontWeight.SemiBold)
                    Text("• 18 Learning Modules")
                    Text("• Interactive Simulations")
                    Text("• Pre & Post Assessments")
                    Text("• Progress Tracking")
                    Text("• Certificates on Completion")
                    Spacer(Modifier.height(8.dp))
                    Text("© 2024 DoseScribe. All rights reserved.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("Choose Avatar") },
            text = {
                Column {
                    Text("Select a pharmacist avatar:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pharmacistAvatarIcons.size) { index ->
                            val isSelected = avatarId == index
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BrandBlue else BrandBlueLight)
                                    .clickable {
                                        viewModel.updateAvatarId(index)
                                        showAvatarPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    pharmacistAvatarIcons[index],
                                    null,
                                    tint = if (isSelected) Color.White else BrandBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarPicker = false }) { Text("Done") }
            }
        )
    }

    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Profile Picture") },
            text = {
                Column {
                    Text("Choose how to set your profile picture:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    // Camera option
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showImageSourceDialog = false
                            cameraLauncher.launch(null)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlueLight)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CameraAlt, null, tint = BrandBlue)
                            Spacer(Modifier.width(12.dp))
                            Text("Take Photo", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // Gallery option
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlueLight)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PhotoLibrary, null, tint = BrandBlue)
                            Spacer(Modifier.width(12.dp))
                            Text("Choose from Gallery", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // Avatar option
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showImageSourceDialog = false
                            showAvatarPicker = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlueLight)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Face, null, tint = BrandBlue)
                            Spacer(Modifier.width(12.dp))
                            Text("Choose Avatar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(containerColor = SurfaceBg, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SimpleTopBar(title = "Account", onBackClick = { navController.popBackStack() })

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile avatar - clickable to change
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BrandBlueLight)
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageBase64.isNotBlank()) {
                        // Show uploaded image
                        val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        val avatarIcon = pharmacistAvatarIcons.getOrElse(avatarId) { Icons.Filled.Person }
                        Icon(avatarIcon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(48.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Tap to change picture", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                Spacer(Modifier.height(12.dp))
                Text("$firstName $lastName".trim().ifBlank { "User" }, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                // Name fields - editable
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { viewModel.updateFirstName(it) },
                        label = { Text("First Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { viewModel.updateLastName(it) },
                        label = { Text("Last Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true
                )
                Spacer(Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes")
                }

                Spacer(Modifier.height(24.dp))

                // Settings cards
                SettingsCard(
                    icon = Icons.Filled.Notifications,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "Notification Settings",
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
                Spacer(Modifier.height(10.dp))
                SettingsCard(
                    icon = Icons.Filled.Info,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "About Us",
                    onClick = { showAboutDialog = true }
                )
                Spacer(Modifier.height(10.dp))
                SettingsCard(
                    icon = Icons.Filled.Lock,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "Change Password",
                    onClick = { navController.navigate("change_password") }
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  CHANGE PASSWORD SCREEN (functional)
// ════════════════════════════════════════════════════════════════════════
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val currentPassword by viewModel.currentPassword.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val passwordStrength by viewModel.passwordStrength.collectAsState()
    val strengthLabel by viewModel.strengthLabel.collectAsState()
    val strengthColor by viewModel.strengthColor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(containerColor = SurfaceBg, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SimpleTopBar(title = "Change Password", onBackClick = { navController.popBackStack() })

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Change Password", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text("Enter your existing password then set a new one", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { viewModel.updateCurrentPassword(it) },
                    label = { Text("Current Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(if (currentPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                        }
                    },
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { viewModel.updateNewPassword(it) },
                    label = { Text("New Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                        }
                    },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { viewModel.updateConfirmPassword(it) },
                    label = { Text("Confirm New Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = confirmPassword.isNotBlank() && confirmPassword != newPassword
                )
                if (confirmPassword.isNotBlank() && confirmPassword != newPassword) {
                    Text("Passwords don't match", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
                }
                Spacer(Modifier.height(8.dp))

                // Password strength
                if (newPassword.isNotBlank()) {
                    Text("Password Strength", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { passwordStrength },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = strengthColor,
                        trackColor = Color(0xFFE0E0E0)
                    )
                    Text(strengthLabel, style = MaterialTheme.typography.bodySmall, color = strengthColor)
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.changePassword() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword && newPassword.length >= 6
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Update Password", fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Forgot password section
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.sendResetEmail() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Forgot Password?", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                            Text("We'll send a reset link to your registered email address", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            Text("Send Reset Link to Email", style = MaterialTheme.typography.labelMedium, color = BrandBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  SETTINGS SCREEN (functional)
// ════════════════════════════════════════════════════════════════════════
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val language by viewModel.language.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    listOf("English", "Arabic", "French", "Spanish", "Urdu").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = language == lang, onClick = {
                                viewModel.setLanguage(lang)
                                showLanguageDialog = false
                            })
                            Spacer(Modifier.width(8.dp))
                            Text(lang)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SimpleTopBar(title = "Settings", onBackClick = { navController.popBackStack() })

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Settings", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(20.dp))

                // Notification Settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandBlueLight), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Notifications, null, tint = BrandBlue, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Notifications", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandBlue)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Dark Mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandBlueLight), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.DarkMode, null, tint = BrandBlue, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Dark Mode", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BrandBlue)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Language
                SettingsCard(
                    icon = Icons.Filled.Language,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "Language",
                    subtitle = language,
                    onClick = { showLanguageDialog = true }
                )
                Spacer(Modifier.height(10.dp))

                // Privacy Policy
                SettingsCard(
                    icon = Icons.Filled.PrivacyTip,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "Privacy Policy",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dosescribe.com/privacy"))
                        context.startActivity(intent)
                    }
                )
                Spacer(Modifier.height(10.dp))

                // Terms of Service
                SettingsCard(
                    icon = Icons.Filled.Gavel,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "Terms of Service",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dosescribe.com/terms"))
                        context.startActivity(intent)
                    }
                )
                Spacer(Modifier.height(10.dp))

                // Account
                SettingsCard(
                    icon = Icons.Filled.Person,
                    iconBg = BrandBlueLight,
                    iconTint = BrandBlue,
                    title = "Account",
                    subtitle = "Manage your profile",
                    onClick = { navController.navigate("account") }
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  PROGRESS SCREEN (functional with real data)
// ════════════════════════════════════════════════════════════════════════
@Composable
fun ProgressScreen(
    navController: NavController,
    progressVm: LessonProgressViewModel = hiltViewModel()
) {
    @Suppress("UNUSED_VARIABLE")
    val progressVersion = progressVm.version  // observe version for reactivity
    val overallProgress = progressVm.getOverallProgress()
    val completedCount = progressVm.getCompletedLessonCount()
    val inProgressModules = progressVm.getInProgressModules()
    val completedModules = progressVm.getCompletedModules()

    Scaffold(containerColor = SurfaceBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SimpleTopBar(title = "Progress", onBackClick = { navController.popBackStack() })

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your Progress", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text("Track your learning journey", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Overall Progress", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(12.dp))
                        Text("${(overallProgress * 100).toInt()}% Complete", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("$completedCount/${allModules.size} Lessons Completed", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                // Completed lessons
                if (completedModules.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text("✅ Completed", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CorrectGreen)
                    Spacer(Modifier.height(8.dp))
                    completedModules.forEach { module ->
                        ProgressModuleCard(module.title, 1.0f, CorrectGreen)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // In-progress lessons
                if (inProgressModules.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text("📚 In Progress", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = BrandBlue)
                    Spacer(Modifier.height(8.dp))
                    inProgressModules.forEach { module ->
                        val prog = progressVm.getProgress(module.id)
                        ProgressModuleCard(module.title, prog, BrandBlue)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressModuleCard(title: String, progress: Float, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  SHARED SETTINGS CARD
// ════════════════════════════════════════════════════════════════════════
@Composable
private fun SettingsCard(
    icon: ImageVector,
    iconBg: Color = BrandBlueLight,
    iconTint: Color = BrandBlue,
    title: String,
    subtitle: String? = null,
    extra: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (extra != null) {
                    Text(extra, style = MaterialTheme.typography.bodySmall, color = BrandBlue, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
