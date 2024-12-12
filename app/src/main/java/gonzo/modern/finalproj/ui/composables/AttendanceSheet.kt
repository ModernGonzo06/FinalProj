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
import gonzo.modern.finalproj.model.Student
import gonzo.modern.finalproj.ui.theme.PresentGreen
import gonzo.modern.finalproj.ui.theme.PresentGreenContainer
import gonzo.modern.finalproj.ui.theme.OnPresentGreen
import gonzo.modern.finalproj.ui.theme.OnPresentGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSheet(
    classWithStudents: ClassWithStudents,
    onClassUpdated: (ClassWithStudents) -> Unit
) {
    var newStudentName by remember { mutableStateOf("") }
    var newStudentEmail by remember { mutableStateOf("") }
    var showEmailError by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Name field
        TextField(
            value = newStudentName,
            onValueChange = { newStudentName = it },
            label = { Text("New Student Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Email field with error state
        TextField(
            value = newStudentEmail,
            onValueChange = { 
                newStudentEmail = it
                showEmailError = false // Reset error when typing
            },
            label = { Text("Student Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showEmailError,
            supportingText = {
                if (showEmailError) {
                    Text(
                        text = "Email must end with @tufts.edu",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Add button
        Button(
            onClick = {
                if (newStudentName.isNotBlank() && newStudentEmail.isNotBlank()) {
                    if (newStudentEmail.endsWith("@tufts.edu")) {
                        val updatedClass = classWithStudents.copy(
                            students = classWithStudents.students + Student(
                                name = newStudentName,
                                email = newStudentEmail
                            )
                        )
                        onClassUpdated(updatedClass)
                        newStudentName = ""
                        newStudentEmail = ""
                        showEmailError = false
                    } else {
                        showEmailError = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Student")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Update the student card to show email
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(classWithStudents.students) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = student.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    val updatedStudents = classWithStudents.students.filter { it.id != student.id }
                                    val updatedAttendance = classWithStudents.attendance.filterKeys { it != student.id }
                                    onClassUpdated(classWithStudents.copy(
                                        students = updatedStudents,
                                        attendance = updatedAttendance
                                    ))
                                }
                            ) {
                                Text("✕")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val updatedAttendance = classWithStudents.attendance + 
                                        (student.id to true)
                                    onClassUpdated(classWithStudents.copy(
                                        attendance = updatedAttendance
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (classWithStudents.attendance[student.id] == true)
                                        PresentGreen
                                    else
                                        PresentGreenContainer,
                                    contentColor = if (classWithStudents.attendance[student.id] == true)
                                        OnPresentGreen
                                    else
                                        OnPresentGreenContainer
                                )
                            ) {
                                Text("Present")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val updatedAttendance = classWithStudents.attendance + 
                                        (student.id to false)
                                    onClassUpdated(classWithStudents.copy(
                                        attendance = updatedAttendance
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (classWithStudents.attendance[student.id] == false)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.errorContainer,
                                    contentColor = if (classWithStudents.attendance[student.id] == false)
                                        MaterialTheme.colorScheme.onError
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Absent")
                            }
                        }
                    }
                }
            }
        }
    }
} 