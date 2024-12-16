package gonzo.modern.finalproj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import gonzo.modern.finalproj.data.UserManager
import gonzo.modern.finalproj.ui.screens.LoginScreen
import gonzo.modern.finalproj.ui.screens.MainScreen
import gonzo.modern.finalproj.ui.theme.FinalProjTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalProjTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userManager = remember { UserManager(applicationContext) }
                    App(userManager = userManager)
                }
            }
        }
    }
}

@Composable
private fun App(userManager: UserManager) {
    var currentUser by remember { mutableStateOf<String?>(null) }

    if (currentUser == null) {
        LoginScreen(
            userManager = userManager,
            onLoginSuccess = { username -> 
                currentUser = username
            }
        )
    } else {
        MainScreen(
            username = currentUser!!,
            userManager = userManager,
            onLogout = {
                currentUser = null
            }
        )
    }
}