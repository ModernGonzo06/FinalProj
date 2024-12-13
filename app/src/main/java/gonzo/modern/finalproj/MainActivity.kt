package gonzo.modern.finalproj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import gonzo.modern.finalproj.ui.screens.LoginScreen
import gonzo.modern.finalproj.ui.screens.MainScreen
import gonzo.modern.finalproj.ui.theme.FinalProjTheme
import gonzo.modern.finalproj.data.UserManager

class MainActivity : ComponentActivity() {
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userManager = UserManager(this)
        enableEdgeToEdge()
        setContent {
            FinalProjTheme {
                var isLoggedIn by remember { mutableStateOf<String?>(null) }

                isLoggedIn?.let { username ->
                    MainScreen(
                        username = username,
                        userManager = userManager
                    )
                } ?: LoginScreen(
                    userManager = userManager,
                    onLoginSuccess = { username -> isLoggedIn = username }
                )
            }
        }
    }
}