package com.azhar.dosescribe.ui.feature.auth.signin

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
import androidx.compose.runtime.saveable.rememberSaveable
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo("signin") { inclusive = true }
                    }
                }
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
        }
    }

    val isLoading = uiState is UiState.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
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
                        .height(180.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(BrandBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "DOSESCRIBE",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Learn, Simulate, Improve",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }

                // Sign In Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Sign In",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Please enter your credentials to create your account",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )
                    Spacer(Modifier.height(12.dp))

                    // Password field
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }
                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "toggle password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
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

                    // Sign In Button
                    Button(
                        onClick = viewModel::onSignInClick,
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
                        Text("Sign In", fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Continue with Google Button
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

                    Spacer(Modifier.height(20.dp))

                    // Don't have an account?
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", color = Color.Gray)
                        TextButton(onClick = { navController.navigate("signup") }) {
                            Text("Sign Up", fontWeight = FontWeight.Bold, color = BrandBlue)
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
