package gonzo.modern.finalproj.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gonzo.modern.finalproj.model.ClassWithStudents
import gonzo.modern.finalproj.ui.theme.PresentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassList(
    classes: List<ClassWithStudents>,
    onClassesUpdated: (List<ClassWithStudents>) -> Unit,
    onClassSelected: (ClassWithStudents) -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newClassName,
                onValueChange = { newClassName = it },
                label = { Text("New Class Name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newClassName.isNotBlank()) {
                        onClassesUpdated(classes + ClassWithStudents(className = newClassName))
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
                                style = MaterialTheme.typography.titleMedium
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