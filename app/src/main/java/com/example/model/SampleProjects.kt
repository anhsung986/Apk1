package com.example.model

object SampleProjects {
    val templates = listOf(
        AndroidProject(
            id = "proj_counter",
            name = "Jetpack Counter",
            packageName = "com.example.counterapp",
            description = "A sleek Jetpack Compose counter app with increment, decrement, and reset controls.",
            version = "1.0",
            files = listOf(
                ProjectFile(
                    name = "AndroidManifest.xml",
                    path = "app/src/main/AndroidManifest.xml",
                    content = """<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.counterapp">
    <application
        android:label="Counter App"
        android:theme="@style/Theme.Material3.DayNight.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>""",
                    type = FileType.MANIFEST
                ),
                ProjectFile(
                    name = "MainActivity.kt",
                    path = "app/src/main/java/com/example/counterapp/MainActivity.kt",
                    content = """package com.example.counterapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                CounterScreen()
            }
        }
    }
}

@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("APK Forge Counter", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Text("\u0024count", fontSize = 72.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { count-- }) { Text("Decrease") }
            Button(onClick = { count++ }) { Text("Increase") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { count = 0 }) { Text("Reset") }
    }
}""",
                    type = FileType.KOTLIN
                ),
                ProjectFile(
                    name = "strings.xml",
                    path = "app/src/main/res/values/strings.xml",
                    content = """<resources>
    <string name="app_name">Counter App</string>
</resources>""",
                    type = FileType.XML
                )
            )
        ),
        AndroidProject(
            id = "proj_todo",
            name = "Task Master",
            packageName = "com.example.taskmaster",
            description = "A robust Jetpack Compose task manager with add, complete, and delete task list items.",
            version = "1.2",
            files = listOf(
                ProjectFile(
                    name = "AndroidManifest.xml",
                    path = "app/src/main/AndroidManifest.xml",
                    content = """<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.taskmaster">
    <application
        android:label="Task Master"
        android:theme="@style/Theme.Material3.DayNight.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>""",
                    type = FileType.MANIFEST
                ),
                ProjectFile(
                    name = "MainActivity.kt",
                    path = "app/src/main/java/com/example/taskmaster/MainActivity.kt",
                    content = """package com.example.taskmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                TaskListScreen()
            }
        }
    }
}

data class Task(val id: Int, val title: String, var done: Boolean = false)

@Composable
fun TaskListScreen() {
    var tasks by remember { mutableStateOf(listOf(Task(1, "Learn APK Building"), Task(2, "Test Gemini AI Code Gen"))) }
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Task Master", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("New Task...") }
            )
            Button(onClick = {
                if (input.isNotBlank()) {
                    tasks = tasks + Task(tasks.size + 1, input)
                    input = ""
                }
            }) { Text("Add") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.done, onCheckedChange = { checked ->
                                tasks = tasks.map { if (it.id == task.id) it.copy(done = checked) else it }
                            })
                            Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
                        }
                        IconButton(onClick = { tasks = tasks.filter { it.id != task.id } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}""",
                    type = FileType.KOTLIN
                )
            )
        )
    )
}
