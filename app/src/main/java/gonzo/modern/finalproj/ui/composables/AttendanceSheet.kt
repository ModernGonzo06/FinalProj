package gonzo.modern.finalproj.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gonzo.modern.finalproj.model.AttendanceRecord
import gonzo.modern.finalproj.model.ClassWithStudents
import gonzo.modern.finalproj.model.Student
import gonzo.modern.finalproj.ui.theme.PresentGreen
import gonzo.modern.finalproj.ui.theme.PresentGreenContainer
import gonzo.modern.finalproj.ui.theme.OnPresentGreen
import gonzo.modern.finalproj.ui.theme.OnPresentGreenContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSheet(
    classWithStudents: ClassWithStudents,
    onClassUpdated: (ClassWithStudents) -> Unit,
    onSaveAndExit: () -> Unit
) {
    var newStudentName by remember { mutableStateOf("") }
    var newStudentEmail by remember { mutableStateOf("") }
    var showEmailError by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Find attendance record for selected date
    val currentAttendanceRecord = classWithStudents.attendanceRecords.find { 
        it.date == selectedDate 
    } ?: AttendanceRecord(selectedDate)

    // Calculate attendance percentage for current date
    val attendancePercentage = if (classWithStudents.students.isNotEmpty()) {
        val presentCount = currentAttendanceRecord.attendance.count { it.value }
        (presentCount.toFloat() / classWithStudents.students.size) * 100
    } else {
        0f
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .plusDays(1)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    todayContentColor = PresentGreen,
                    todayDateBorderColor = PresentGreen,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    selectedDate = selectedDate.minusDays(1)
                }) {
                    Text("←")
                }
                
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = { 
                    selectedDate = selectedDate.plusDays(1)
                }) {
                    Text("→")
                }
            }
        }

        // Add attendance percentage indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Attendance",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        progress = { attendancePercentage / 100 },
                        color = PresentGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${attendancePercentage.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Save button
        Button(
            onClick = onSaveAndExit,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        // Attendance list
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
                                    // Update all attendance records to remove the student
                                    val updatedRecords = classWithStudents.attendanceRecords.map { record ->
                                        record.copy(
                                            attendance = record.attendance.filterKeys { it != student.id }
                                        )
                                    }
                                    onClassUpdated(classWithStudents.copy(
                                        students = updatedStudents,
                                        attendanceRecords = updatedRecords
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
                                    val updatedAttendance = currentAttendanceRecord.attendance + 
                                        (student.id to true)
                                    val updatedRecords = classWithStudents.attendanceRecords.toMutableList()
                                    val recordIndex = updatedRecords.indexOfFirst { it.date == selectedDate }
                                    if (recordIndex >= 0) {
                                        updatedRecords[recordIndex] = currentAttendanceRecord.copy(
                                            attendance = updatedAttendance
                                        )
                                    } else {
                                        updatedRecords.add(AttendanceRecord(
                                            date = selectedDate,
                                            attendance = updatedAttendance
                                        ))
                                    }
                                    onClassUpdated(classWithStudents.copy(
                                        attendanceRecords = updatedRecords
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentAttendanceRecord.attendance[student.id] == true)
                                        PresentGreen
                                    else
                                        PresentGreenContainer
                                )
                            ) {
                                Text("Present")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val updatedAttendance = currentAttendanceRecord.attendance + 
                                        (student.id to false)
                                    val updatedRecords = classWithStudents.attendanceRecords.toMutableList()
                                    val recordIndex = updatedRecords.indexOfFirst { it.date == selectedDate }
                                    if (recordIndex >= 0) {
                                        updatedRecords[recordIndex] = currentAttendanceRecord.copy(
                                            attendance = updatedAttendance
                                        )
                                    } else {
                                        updatedRecords.add(AttendanceRecord(
                                            date = selectedDate,
                                            attendance = updatedAttendance
                                        ))
                                    }
                                    onClassUpdated(classWithStudents.copy(
                                        attendanceRecords = updatedRecords
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentAttendanceRecord.attendance[student.id] == false)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.errorContainer,
                                    contentColor = if (currentAttendanceRecord.attendance[student.id] == false)
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