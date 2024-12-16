package gonzo.modern.finalproj.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import gonzo.modern.finalproj.data.UserManager
import gonzo.modern.finalproj.model.ClassWithStudents
import gonzo.modern.finalproj.ui.composables.AttendanceSheet
import gonzo.modern.finalproj.ui.composables.ClassList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    username: String,
    userManager: UserManager,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    
    var selectedClass by remember { mutableStateOf<ClassWithStudents?>(null) }
    var classes by remember { mutableStateOf(userManager.getClassesForUser(username)) }
    
    if (selectedClass == null) {
        ClassList(
            classes = classes,
            onClassesUpdated = { classes = it },
            onClassSelected = { selectedClass = it },
            onLogout = onLogout
        )
    } else {
        AttendanceSheet(
            context = context,
            classWithStudents = selectedClass!!,
            onClassUpdated = { updated ->
                selectedClass = updated
                classes = classes.map { 
                    if (it.className == updated.className) updated else it 
                }
                userManager.saveClassesForUser(username, classes)
            },
            onSaveAndExit = { selectedClass = null }
        )
    }
} 