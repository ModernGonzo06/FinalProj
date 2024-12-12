package gonzo.modern.finalproj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import gonzo.modern.finalproj.ui.screens.MainScreen
import gonzo.modern.finalproj.ui.theme.FinalProjTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalProjTheme {
                MainScreen()
            }
        }
    }
}