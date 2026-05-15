package com.azhar.dosescribe.ui.feature.auth.signup

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.azhar.dosescribe.R
import com.azhar.dosescribe.ui.feature.auth.AuthViewModel
import com.azhar.dosescribe.ui.util.UiEvent
import com.azhar.dosescribe.ui.util.UiState
import kotlinx.coroutines.flow.collectLatest

private val BrandBlue = Color(0xFF0982BA)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val email by viewModel.email.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val firstNameError by viewModel.firstNameError.collectAsState()
    val lastNameError by viewModel.lastNameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = uiState is UiState.Loading

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo("signup") { inclusive = true }
                        popUpTo("signin") { inclusive = true }
                    }
                }
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Brand Panel (Blue header)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(BrandBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "DOSESCRIBE",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Learn, Simulate, Improve",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }

                // Sign Up Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Sign Up",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Please enter your credentials to create your account",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    // First Name + Last Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = viewModel::onFirstNameChange,
                            label = { Text("First Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            isError = firstNameError != null,
                            supportingText = firstNameError?.let { { Text(it) } }
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = viewModel::onLastNameChange,
                            label = { Text("Last Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            isError = lastNameError != null,
                            supportingText = lastNameError?.let { { Text(it) } }
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it) } }
                    )
                    Spacer(Modifier.height(12.dp))

                    // Gender Dropdown
                    GenderDropdown(
                        modifier = Modifier.fillMaxWidth(),
                        value = gender,
                        onValueChange = viewModel::onGenderChange
                    )
                    Spacer(Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it) } }
                    )
                    Spacer(Modifier.height(12.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(it) } }
                    )

                    // Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { navController.navigate("forgot_password") }) {
                            Text("Forgot Password?", color = BrandBlue, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Sign Up Button
                    Button(
                        onClick = { viewModel.onSignUpClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Signup", fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Continue with Google
                    OutlinedButton(
                        onClick = { viewModel.onGoogleSignInClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Filled.GMobiledata,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Continue With Google", fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Already have an account?
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account? ", color = Color.Gray)
                        TextButton(onClick = { navController.navigate("signin") }) {
                            Text("Sign In", fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                    }
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {
    val options = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
