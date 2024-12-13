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
    var showDateDropdown by remember { mutableStateOf(false) }
    var showAttendanceHistory by remember { mutableStateOf(false) }
    var showAddStudent by remember { mutableStateOf(false) }
    var showStudentInfo by remember { mutableStateOf<Student?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
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

    // Add this function inside AttendanceSheet
    fun removeAttendanceRecord(date: LocalDate) {
        val updatedRecords = classWithStudents.attendanceRecords.filter { it.date != date }
        onClassUpdated(classWithStudents.copy(attendanceRecords = updatedRecords))
    }

    // Add this dialog outside the Scaffold
    showStudentInfo?.let { student ->
        AlertDialog(
            onDismissRequest = { showStudentInfo = null },
            title = { Text("Student Information") },
            text = {
                Column {
                    Text("Name: ${student.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${student.email}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ID: ${student.id}")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = true
                    }
                ) {
                    Text("Remove Student", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStudentInfo = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Add delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Removal") },
            text = { Text("Are you sure you want to remove this student?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStudentInfo?.let { student ->
                            val updatedStudents = classWithStudents.students.filter { it.id != student.id }
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
                        showDeleteConfirmation = false
                        showStudentInfo = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onSaveAndExit,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Date selector and attendance percentage card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Attendance History Dropdown
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAttendanceHistory = !showAttendanceHistory }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attendance History",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(if (showAttendanceHistory) "▼" else "▶")
                        }
                    }

                    // Attendance Records List (shown when dropdown is expanded)
                    if (showAttendanceHistory && classWithStudents.attendanceRecords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(
                                classWithStudents.attendanceRecords.sortedByDescending { it.date }
                            ) { record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { 
                                            selectedDate = record.date
                                            showAttendanceHistory = false 
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = record.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Show attendance percentage for this date
                                    val presentCount = record.attendance.count { it.value }
                                    val percentage = if (classWithStudents.students.isNotEmpty()) {
                                        (presentCount.toFloat() / classWithStudents.students.size) * 100
                                    } else 0f
                                    
                                    Text(
                                        text = "${percentage.toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    
                                    IconButton(
                                        onClick = { removeAttendanceRecord(record.date) }
                                    ) {
                                        Text("✕")
                                    }
                                }
                                if (record.date != classWithStudents.attendanceRecords.sortedByDescending { it.date }.last().date) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date: ${selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Change Date")
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name field
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (!showAddStudent) {
                                Button(
                                    onClick = { showAddStudent = true },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Add Student")
                                }
                            } else {
                                Column {
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
                                            showEmailError = false
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
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { 
                                                showAddStudent = false
                                                newStudentName = ""
                                                newStudentEmail = ""
                                                showEmailError = false
                                            }
                                        ) {
                                            Text("Cancel")
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
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
                                                        showAddStudent = false  // Hide the form after adding
                                                    } else {
                                                        showEmailError = true
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Add")
                                        }
                                    }
                                }
                            }
                        }
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
                                        }
                                        IconButton(
                                            onClick = { showStudentInfo = student }
                                        ) {
                                            Text("ℹ️")
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
        }
    }
} 