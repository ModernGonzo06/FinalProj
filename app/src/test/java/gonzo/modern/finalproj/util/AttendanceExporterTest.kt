package gonzo.modern.finalproj.util

import android.content.Context
import gonzo.modern.finalproj.model.AttendanceRecord
import gonzo.modern.finalproj.model.ClassWithStudents
import gonzo.modern.finalproj.model.Student
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalDate

class AttendanceExporterTest {
    @get:Rule
    val tempFolder = TemporaryFolder()
    
    private lateinit var mockContext: Context
    private lateinit var exportDir: File

    @Before
    fun setup() {
        exportDir = tempFolder.newFolder("test_exports")
        mockContext = mockk<Context>(relaxed = true)
        every { mockContext.getExternalFilesDir(null) } returns exportDir
    }

    @Test
    fun `exportToCSV creates correct CSV format`() {
        // Create test data
        val students = listOf(
            Student(id = "1", name = "John Doe", email = "john@tufts.edu"),
            Student(id = "2", name = "Jane Smith", email = "jane@tufts.edu")
        )
        
        val date1 = LocalDate.of(2024, 3, 1)
        val date2 = LocalDate.of(2024, 3, 2)
        
        val attendanceRecords = listOf(
            AttendanceRecord(
                date = date1,
                attendance = mapOf(
                    "1" to true,
                    "2" to false
                )
            ),
            AttendanceRecord(
                date = date2,
                attendance = mapOf(
                    "1" to true,
                    "2" to true
                )
            )
        )

        val classWithStudents = ClassWithStudents(
            className = "Test Class",
            students = students,
            attendanceRecords = attendanceRecords
        )

        // Export to CSV
        val file = AttendanceExporter.exportToCSV(mockContext, classWithStudents)

        // Read the file content
        val content = file.readText()
        
        // Verify header row
        assertTrue(content.startsWith("Student Name,Email,2024-03-01,2024-03-02,Overall Attendance"))
        
        // Verify John's row
        assertTrue(content.contains("John Doe,john@tufts.edu,Present,Present,100%"))
        
        // Verify Jane's row
        assertTrue(content.contains("Jane Smith,jane@tufts.edu,Absent,Present,50%"))
    }

    @Test
    fun `exportToCSV handles empty class correctly`() {
        val emptyClass = ClassWithStudents(
            className = "Empty Class",
            students = emptyList(),
            attendanceRecords = emptyList()
        )

        val file = AttendanceExporter.exportToCSV(mockContext, emptyClass)
        val content = file.readText()

        // Should only contain header row without dates
        assertEquals("Student Name,Email,Overall Attendance\n", content)
    }

    @Test
    fun `exportToCSV handles missing attendance records correctly`() {
        val students = listOf(
            Student(id = "1", name = "John Doe", email = "john@tufts.edu")
        )
        
        val date = LocalDate.of(2024, 3, 1)
        val attendanceRecords = listOf(
            AttendanceRecord(
                date = date,
                attendance = emptyMap() // No attendance recorded
            )
        )

        val classWithStudents = ClassWithStudents(
            className = "Test Class",
            students = students,
            attendanceRecords = attendanceRecords
        )

        val file = AttendanceExporter.exportToCSV(mockContext, classWithStudents)
        val content = file.readText()

        // Verify that missing attendance is marked as N/A
        assertTrue(content.contains("John Doe,john@tufts.edu,N/A,0%"))
    }
}