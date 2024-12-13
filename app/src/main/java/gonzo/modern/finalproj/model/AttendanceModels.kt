package gonzo.modern.finalproj.model

import java.time.LocalDate

data class Student(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val email: String
)

data class AttendanceRecord(
    val date: LocalDate,
    val attendance: Map<String, Boolean> = mapOf() // Student ID to attendance status
)

data class ClassWithStudents(
    val classId: String = java.util.UUID.randomUUID().toString(),
    val className: String,
    val students: List<Student> = listOf(),
    val attendanceRecords: List<AttendanceRecord> = listOf()
) 