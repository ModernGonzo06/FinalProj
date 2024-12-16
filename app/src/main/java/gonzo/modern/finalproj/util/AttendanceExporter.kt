package gonzo.modern.finalproj.util

import android.content.Context
import gonzo.modern.finalproj.model.ClassWithStudents
import java.io.File
import java.time.format.DateTimeFormatter

class AttendanceExporter {
    companion object {
        fun exportToCSV(context: Context, classWithStudents: ClassWithStudents): File {
            val fileName = "${classWithStudents.className}_attendance.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            file.printWriter().use { out ->
                // Header row with dates
                out.print("Student Name,Email")
                classWithStudents.attendanceRecords
                    .sortedBy { it.date }
                    .forEach { record ->
                        out.print(",${record.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                    }
                out.println(",Overall Attendance")

                // Student rows
                classWithStudents.students.forEach { student ->
                    out.print("${student.name},${student.email}")
                    
                    // Print attendance for each date
                    classWithStudents.attendanceRecords
                        .sortedBy { it.date }
                        .forEach { record ->
                            val status = when(record.attendance[student.id]) {
                                true -> "Present"
                                false -> "Absent"
                                null -> "N/A"
                            }
                            out.print(",$status")
                        }
                    
                    // Calculate and print overall attendance
                    val totalDays = classWithStudents.attendanceRecords.size
                    val presentDays = classWithStudents.attendanceRecords.count { 
                        it.attendance[student.id] == true 
                    }
                    val percentage = if (totalDays > 0) {
                        (presentDays.toFloat() / totalDays) * 100
                    } else 0f
                    
                    out.println(",${percentage.toInt()}%")
                }
            }
            
            return file
        }
    }
} 