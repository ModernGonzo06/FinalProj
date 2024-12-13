package gonzo.modern.finalproj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import gonzo.modern.finalproj.ui.screens.LoginScreen
import gonzo.modern.finalproj.ui.screens.MainScreen
import gonzo.modern.finalproj.ui.theme.FinalProjTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                } else {
                    MainScreen()
                }
            }
        }
    }
}