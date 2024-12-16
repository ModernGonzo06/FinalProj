package gonzo.modern.finalproj.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gonzo.modern.finalproj.model.ClassWithStudents
import java.time.LocalDate
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer

class UserManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java,
            JsonSerializer<LocalDate> { src, _, _ ->
                gson.toJsonTree(src.toString())
            })
        .registerTypeAdapter(LocalDate::class.java,
            JsonDeserializer<LocalDate> { json, _, _ ->
                LocalDate.parse(json.asString)
            })
        .create()
    
    fun saveUser(username: String, password: String) {
        sharedPreferences.edit()
            .putString("${username}_password", password)
            .apply()
    }
    
    fun validateUser(username: String, password: String): Boolean {
        val savedPassword = sharedPreferences.getString("${username}_password", null)
        return savedPassword == password
    }
    
    fun userExists(username: String): Boolean {
        return sharedPreferences.contains("${username}_password")
    }

    fun saveClassesForUser(username: String, classes: List<ClassWithStudents>) {
        val classesJson = gson.toJson(classes)
        sharedPreferences.edit()
            .putString("${username}_classes", classesJson)
            .apply()
    }

    fun getClassesForUser(username: String): List<ClassWithStudents> {
        val classesJson = sharedPreferences.getString("${username}_classes", null)
        return if (classesJson != null) {
            val type = object : TypeToken<List<ClassWithStudents>>() {}.type
            gson.fromJson(classesJson, type)
        } else {
            listOf()
        }
    }
} 