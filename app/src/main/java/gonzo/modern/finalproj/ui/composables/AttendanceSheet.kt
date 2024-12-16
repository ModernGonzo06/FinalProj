package gonzo.modern.finalproj.ui.composables

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import gonzo.modern.finalproj.model.AttendanceRecord
import gonzo.modern.finalproj.model.ClassWithStudents
import gonzo.modern.finalproj.model.Student
import gonzo.modern.finalproj.ui.theme.LateOrange
import gonzo.modern.finalproj.ui.theme.PresentGreen
import gonzo.modern.finalproj.ui.theme.PresentGreenContainer
import gonzo.modern.finalproj.util.AttendanceExporter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Move the function outside of AttendanceSheet
@Composable
fun getAttendanceColor(percentage: Float): Color {
    return when {
        percentage >= 90f -> PresentGreen
        percentage >= 70f -> LateOrange
        else -> MaterialTheme.colorScheme.error
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSheet(
    context: Context,
    classWithStudents: ClassWithStudents,
    onClassUpdated: (ClassWithStudents) -> Unit,
    onSaveAndExit: () -> Unit
) {
    var newStudentName by remember { mutableStateOf("") }
    var newStudentEmail by remember { mutableStateOf("") }
    var showEmailError by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAttendanceHistory by remember { mutableStateOf(false) }
    var showAddStudent by remember { mutableStateOf(false) }
    var showStudentInfo by remember { mutableStateOf<Student?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
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
                    Text("Email: ${student.email}")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Add attendance history section
                    Text(
                        "Attendance History",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calculate overall attendance percentage
                    val totalDays = classWithStudents.attendanceRecords.size
                    val daysPresent = classWithStudents.attendanceRecords.count { 
                        it.attendance[student.id] == true 
                    }
                    val attendancePercentage = if (totalDays > 0) {
                        (daysPresent.toFloat() / totalDays) * 100
                    } else 0f
                    
                    Text(
                        "Overall Attendance: ${attendancePercentage.toInt()}%",
                        color = getAttendanceColor(attendancePercentage)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Show individual attendance records
                    classWithStudents.attendanceRecords
                        .sortedByDescending { it.date }
                        .forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(record.date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                Text(
                                    if (record.attendance[student.id] == true) "Present" else "Absent",
                                    color = if (record.attendance[student.id] == true) 
                                        PresentGreen else MaterialTheme.colorScheme.error
                                )
                            }
                        }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Export button on the left
                    IconButton(
                        onClick = {
                            try {
                                val file = AttendanceExporter.exportToCSV(context, classWithStudents)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                
                                context.startActivity(Intent.createChooser(intent, "Share Attendance Records"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Export Attendance Records",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Save button on the right
                    Button(
                        onClick = onSaveAndExit
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
                .padding(horizontal = 16.dp)
        ) {
            // Add course title at the top
            Text(
                text = classWithStudents.className,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Top section with date selector and attendance history
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
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
                                        color = getAttendanceColor(percentage),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    
                                    IconButton(
                                        onClick = { removeAttendanceRecord(record.date) }
                                    ) {
                                        Text("✕")
                                    }
                                }
                                if (record.date != classWithStudents.attendanceRecords.minByOrNull { it.date }!!.date) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
                        IconButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Change Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                                    color = getAttendanceColor(attendancePercentage),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = "${attendancePercentage.toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = getAttendanceColor(attendancePercentage)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add Student section
                    if (showAddStudent) {
                        Column {
                            TextField(
                                value = newStudentName,
                                onValueChange = { newStudentName = it },
                                label = { Text("New Student Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
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
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { 
                                    showAddStudent = false
                                    newStudentName = ""
                                    newStudentEmail = ""
                                    showEmailError = false
                                }) {
                                    Text("Cancel")
                                }
                                
                                Button(onClick = {
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
                                            showAddStudent = false
                                        } else {
                                            showEmailError = true
                                        }
                                    }
                                }) {
                                    Text("Add")
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { showAddStudent = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Student")
                        }
                    }
                }
            }

            // Before the LazyColumn, add a button to mark all present
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search students...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                Button(
                    onClick = {
                        val allPresentAttendance = classWithStudents.students.associate { 
                            it.id to true 
                        }
                        val updatedRecords = classWithStudents.attendanceRecords.toMutableList()
                        val recordIndex = updatedRecords.indexOfFirst { it.date == selectedDate }
                        
                        if (recordIndex >= 0) {
                            updatedRecords[recordIndex] = currentAttendanceRecord.copy(
                                attendance = allPresentAttendance
                            )
                        } else {
                            updatedRecords.add(AttendanceRecord(
                                date = selectedDate,
                                attendance = allPresentAttendance
                            ))
                        }
                        
                        onClassUpdated(classWithStudents.copy(
                            attendanceRecords = updatedRecords
                        ))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PresentGreen
                    )
                ) {
                    Text("Mark All Present")
                }
            }

            // Before the LazyColumn, after the Mark All Present button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Students",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${classWithStudents.students.size} total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Student list in a scrollable container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    classWithStudents.students.filter { student ->
                        student.name.contains(searchQuery, ignoreCase = true)
                    }
                ) { student ->
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
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Student Information",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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