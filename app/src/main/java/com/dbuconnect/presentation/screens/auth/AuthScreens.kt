package com.dbuconnect.presentation.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dbuconnect.presentation.components.GoogleButton
import com.dbuconnect.presentation.components.PrimaryButton
import com.dbuconnect.presentation.screens.onboarding.DBULogo
import com.dbuconnect.presentation.theme.*
import com.dbuconnect.presentation.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (isProfileComplete: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn, state.user?.isProfileComplete) {
        val user = state.user
        if (state.isLoggedIn && user != null) {
            onLoginSuccess(user.isProfileComplete)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Login and Security",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        DBULogo(modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome back!",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in with your university email to continue.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // University Email
        Text(
            text = "University Email",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.updateEmail(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("name@dbu.edu.et", color = TextTertiary) },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = TextSecondary)
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = BackgroundWhite
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        Text(
            text = "Password",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.updatePassword(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("••••••••", color = TextTertiary) },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Outlined.Visibility
                        else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = BorderDefault,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = BackgroundWhite
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot Password?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen,
                modifier = Modifier
                    .clickable { 
                        viewModel.clearError()
                        showForgotPasswordDialog = true 
                    }
                    .padding(vertical = 4.dp)
            )
        }

        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.clearError()
                    showForgotPasswordDialog = false 
                },
                title = {
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your registered DBU university email address. We'll send you a password recovery link.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("name@dbu.edu.et", color = TextTertiary) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                unfocusedBorderColor = BorderDefault,
                                focusedContainerColor = BackgroundWhite,
                                unfocusedContainerColor = BackgroundWhite
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        if (state.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.error!!,
                                fontSize = 13.sp,
                                color = StatusError
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.recoverPassword() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send Link", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            viewModel.clearError()
                            showForgotPasswordDialog = false 
                        }
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = BackgroundWhite
            )
        }

        if (state.isForgotPasswordSuccess) {
            var newPassword by remember { mutableStateOf("") }
            var confirmNewPassword by remember { mutableStateOf("") }
            var newPasswordVisible by remember { mutableStateOf(false) }
            var confirmNewPasswordVisible by remember { mutableStateOf(false) }
            var resetPasswordError by remember { mutableStateOf<String?>(null) }
            var isResetComplete by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    viewModel.resetForgotPasswordSuccess()
                    showForgotPasswordDialog = false
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.MarkEmailRead,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isResetComplete) "Success!" else "Simulated Email Bypassed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Column {
                        if (!isResetComplete) {
                            Text(
                                text = "📬 Since '@dbu.edu.et' is a simulated university domain, we have bypassed the email inbox step and unlocked your secure password reset form directly here!",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // New Password
                            Text("New Password", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it; resetPasswordError = null },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("••••••••", color = TextTertiary) },
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(
                                            if (newPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            null, tint = TextSecondary
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = BorderDefault,
                                    focusedContainerColor = BackgroundWhite,
                                    unfocusedContainerColor = BackgroundWhite
                                ),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Confirm New Password
                            Text("Confirm Password", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = confirmNewPassword,
                                onValueChange = { confirmNewPassword = it; resetPasswordError = null },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("••••••••", color = TextTertiary) },
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                                        Icon(
                                            if (confirmNewPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            null, tint = TextSecondary
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = BorderDefault,
                                    focusedContainerColor = BackgroundWhite,
                                    unfocusedContainerColor = BackgroundWhite
                                ),
                                singleLine = true
                            )
                            
                            if (resetPasswordError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = resetPasswordError!!,
                                    fontSize = 12.sp,
                                    color = StatusError
                                )
                            }
                        } else {
                            Text(
                                text = "Your password has been successfully updated in our system database! We have automatically filled your new password on the login screen so you can sign in instantly.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!isResetComplete) {
                        Button(
                            onClick = {
                                if (newPassword.length < 6) {
                                    resetPasswordError = "Password must be at least 6 characters"
                                } else if (newPassword != confirmNewPassword) {
                                    resetPasswordError = "Passwords do not match"
                                } else {
                                    viewModel.updatePassword(newPassword)
                                    isResetComplete = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Update Password", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.resetForgotPasswordSuccess()
                                showForgotPasswordDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sign In Now", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    if (!isResetComplete) {
                        TextButton(
                            onClick = {
                                viewModel.resetForgotPasswordSuccess()
                                showForgotPasswordDialog = false
                            }
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                },
                containerColor = BackgroundWhite
            )
        }

        // Error
        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error!!,
                fontSize = 13.sp,
                color = StatusError
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            text = "Sign In",
            onClick = { viewModel.login() },
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "By continuing, you agree to our Terms and Privacy Policy.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Text(
                text = "Sign Up",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onSignUpSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "DBU Connect",
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join the DBU Connect community today.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Form card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = BackgroundWhite,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Full Name
                    FormField(
                        label = "Full Name",
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        placeholder = "John Doe",
                        leadingIcon = Icons.Outlined.Person
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // University Email
                    FormField(
                        label = "University Email",
                        value = state.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        placeholder = "name@dbu.edu.et",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use your @dbu.edu.et email address",
                        fontSize = 12.sp,
                        color = TextTertiary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone (optional)
                    FormField(
                        label = "Phone Number (Optional)",
                        value = state.phone,
                        onValueChange = { viewModel.updatePhone(it) },
                        placeholder = "+251 911 234 567",
                        leadingIcon = Icons.Outlined.Phone,
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Text(
                        text = "Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = TextTertiary) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.Visibility
                                    else Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderDefault,
                            focusedContainerColor = BackgroundPrimary,
                            unfocusedContainerColor = BackgroundPrimary
                        ),
                        singleLine = true
                    )

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.error!!, fontSize = 13.sp, color = StatusError)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "Create Account",
                        onClick = { viewModel.signUp() },
                        isLoading = state.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "By signing up, you agree to our Terms of Service and Privacy Policy.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", fontSize = 14.sp, color = TextSecondary)
                Text(
                    "Log In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                    modifier = Modifier.clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextTertiary) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = TextSecondary)
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderDefault,
            focusedContainerColor = BackgroundPrimary,
            unfocusedContainerColor = BackgroundPrimary
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

