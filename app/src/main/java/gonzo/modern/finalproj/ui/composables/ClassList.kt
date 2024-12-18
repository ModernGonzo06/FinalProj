package gonzo.modern.finalproj.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gonzo.modern.finalproj.model.ClassWithStudents
import gonzo.modern.finalproj.ui.theme.PresentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassList(
    classes: List<ClassWithStudents>,
    onClassesUpdated: (List<ClassWithStudents>) -> Unit,
    onClassSelected: (ClassWithStudents) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Classes") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            var newClassName by remember { mutableStateOf("") }

            // Helper function to calculate overall attendance percentage
            fun calculateOverallAttendance(classWithStudents: ClassWithStudents): Float {
                if (classWithStudents.students.isEmpty() || classWithStudents.attendanceRecords.isEmpty()) {
                    return 0f
                }
                val totalPossibleAttendances = classWithStudents.students.size * classWithStudents.attendanceRecords.size
                val totalPresent = classWithStudents.attendanceRecords.sumOf { record ->
                    record.attendance.count { it.value }
                }
                return (totalPresent.toFloat() / totalPossibleAttendances) * 100
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newClassName,
                    onValueChange = { input ->
                        // Limit the length of class name (adjust 30 to desired max length)
                        if (input.length <= 50) {
                            newClassName = input
                        }
                    },
                    label = { Text("New Class Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,  // Force single line
                    maxLines = 1        // Ensure text doesn't wrap
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        if (newClassName.isNotBlank()) {
                            onClassesUpdated(classes + ClassWithStudents(className = newClassName.trim()))
                            newClassName = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classes) { classItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onClassSelected(classItem) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = classItem.className,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,           // Ensure single line
                                    overflow = TextOverflow.Ellipsis  // Add ellipsis if text is too long
                                )
                                Text(
                                    text = "${classItem.students.size} students",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // Add attendance percentage indicator
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val attendancePercentage = calculateOverallAttendance(classItem)
                                CircularProgressIndicator(
                                    modifier = Modifier.fillMaxSize(),
                                    progress = { attendancePercentage / 100 },
                                    color = PresentGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = "${attendancePercentage.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(
                                onClick = {
                                    onClassesUpdated(classes.filter { it.className != classItem.className })
                                }
                            ) {
                                Text("✕")
                            }
                        }
                    }
                }
            }
        }
    }
} 