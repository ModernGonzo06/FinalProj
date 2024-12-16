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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    
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
        // Calculate student's attendance percentage
        val totalDays = classWithStudents.attendanceRecords.size
        val presentDays = classWithStudents.attendanceRecords.count { record ->
            record.attendance[student.id] == true
        }
        val attendancePercentage = if (totalDays > 0) {
            (presentDays.toFloat() / totalDays) * 100
        } else 0f

        AlertDialog(
            onDismissRequest = { showStudentInfo = null },
            modifier = Modifier
                .widthIn(max = 400.dp)  // Limit maximum width
                .heightIn(max = 600.dp), // Limit maximum height
            title = { 
                Column {
                    Text(
                        text = "Student Information",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    // Email
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Overall Attendance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Attendance",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { attendancePercentage / 100 },
                                modifier = Modifier.fillMaxSize(),
                                color = getAttendanceColor(attendancePercentage),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${attendancePercentage.toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Attendance History
                    Text(
                        text = "Attendance History",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show attendance records sorted by date
                    classWithStudents.attendanceRecords
                        .sortedByDescending { it.date }
                        .forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = record.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = when(record.attendance[student.id]) {
                                        true -> "Present"
                                        false -> "Absent"
                                        null -> "N/A"
                                    },
                                    color = when(record.attendance[student.id]) {
                                        true -> PresentGreen
                                        false -> MaterialTheme.colorScheme.error
                                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            studentToDelete = student
                            showDeleteConfirmation = true
                            showStudentInfo = null
                        }
                    ) {
                        Text("Remove Student", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showStudentInfo = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Add delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false 
                studentToDelete = null
            },
            title = { Text("Confirm Removal") },
            text = { Text("Are you sure you want to remove this student?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        studentToDelete?.let { student ->
                            // Remove student from the list
                            val updatedStudents = classWithStudents.students.filter { it.id != student.id }
                            
                            // Remove student's attendance records
                            val updatedRecords = classWithStudents.attendanceRecords.map { record ->
                                val updatedAttendance = record.attendance.filterKeys { it != student.id }
                                record.copy(attendance = updatedAttendance)
                            }
                            
                            // Update the class
                            onClassUpdated(classWithStudents.copy(
                                students = updatedStudents,
                                attendanceRecords = updatedRecords
                            ))
                        }
                        showDeleteConfirmation = false
                        studentToDelete = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmation = false
                        studentToDelete = null
                    }
                ) {
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