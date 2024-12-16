package gonzo.modern.finalproj.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import gonzo.modern.finalproj.data.UserManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userManager: UserManager,
    onLoginSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Attendance Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = if (isSignUp) "Sign Up" else "Login",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = username,
            onValueChange = { 
                username = it
                showError = false
            },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = showError
        )

        TextField(
            value = password,
            onValueChange = { 
                password = it
                showError = false
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = showError
        )

        if (isSignUp) {
            TextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    showError = false
                },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                isError = showError
            )
        }

        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    showError = true
                    errorMessage = "Please fill in all fields"
                    return@Button
                }

                if (isSignUp) {
                    if (password != confirmPassword) {
                        showError = true
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    if (userManager.userExists(username)) {
                        showError = true
                        errorMessage = "Username already exists"
                        return@Button
                    }
                    userManager.saveUser(username, password)
                    onLoginSuccess(username)
                } else {
                    if (userManager.validateUser(username, password)) {
                        onLoginSuccess(username)
                    } else {
                        showError = true
                        errorMessage = "Invalid credentials. Sign up?"
                        isSignUp = true
                        confirmPassword = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignUp) "Sign Up" else "Login")
        }

        if (!isSignUp) {
            TextButton(
                onClick = { 
                    isSignUp = true
                    showError = false
                    confirmPassword = ""
                }
            ) {
                Text("Don't have an account? Sign Up")
            }
        } else {
            TextButton(
                onClick = { 
                    isSignUp = false
                    showError = false
                    confirmPassword = ""
                }
            ) {
                Text("Already have an account? Login")
            }
        }
    }
} 