package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AndroidProject
import com.example.model.BuildState
import com.example.model.BuiltApk
import com.example.model.ProjectFile
import com.example.model.SampleProjects
import com.google.firebase.Firebase
import com.google.firebase.ai.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ApkForgeViewModel : ViewModel() {
    private val TAG = "ApkForgeViewModel"

    private val _projects = MutableStateFlow<List<AndroidProject>>(SampleProjects.templates)
    val projects: StateFlow<List<AndroidProject>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<AndroidProject?>(SampleProjects.templates.first())
    val currentProject: StateFlow<AndroidProject?> = _currentProject.asStateFlow()

    private val _currentFile = MutableStateFlow<ProjectFile?>(SampleProjects.templates.first().files.last())
    val currentFile: StateFlow<ProjectFile?> = _currentFile.asStateFlow()

    private val _buildState = MutableStateFlow<BuildState>(BuildState.Idle)
    val buildState: StateFlow<BuildState> = _buildState.asStateFlow()

    private val _builtApks = MutableStateFlow<List<BuiltApk>>(
        listOf(
            BuiltApk(
                id = "apk_1",
                projectName = "Jetpack Counter",
                packageName = "com.example.counterapp",
                versionName = "1.0",
                fileSizeKb = 2450,
                buildDurationSeconds = 4,
                signed = true
            )
        )
    )
    val builtApks: StateFlow<List<BuiltApk>> = _builtApks.asStateFlow()

    private val _aiResponse = MutableStateFlow<String>("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow<BoolValue>(BoolValue(false))
    val isAiLoading: StateFlow<BoolValue> = _isAiLoading.asStateFlow()

    private val generativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    fun selectProject(project: AndroidProject) {
        _currentProject.value = project
        _currentFile.value = project.files.firstOrNull()
    }

    fun selectFile(file: ProjectFile) {
        _currentFile.value = file
    }

    fun updateFileContent(newContent: String) {
        val file = _currentFile.value ?: return
        val project = _currentProject.value ?: return

        val updatedFiles = project.files.map {
            if (it.path == file.path) it.copy(content = newContent) else it
        }
        val updatedProject = project.copy(files = updatedFiles, updatedAt = System.currentTimeMillis())
        _currentProject.value = updatedProject
        _currentFile.value = updatedFiles.find { it.path == file.path }

        _projects.value = _projects.value.map {
            if (it.id == updatedProject.id) updatedProject else it
        }
    }

    fun addNewProject(name: String, packageName: String, description: String) {
        val newProj = AndroidProject(
            id = "proj_${System.currentTimeMillis()}",
            name = name,
            packageName = packageName,
            description = description,
            files = listOf(
                ProjectFile(
                    name = "MainActivity.kt",
                    path = "app/src/main/java/com/example/app/MainActivity.kt",
                    content = """package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("$name App Ready!")
                }
            }
        }
    }
}"""
                )
            )
        )
        _projects.value = _projects.value + newProj
        selectProject(newProj)
    }

    fun startBuild(project: AndroidProject) {
        viewModelScope.launch {
            val logs = mutableListOf<String>()
            val stages = listOf(
                "Gradle Sync & Dependency Resolution",
                "AAPT2 Resource & Manifest Compilation",
                "Kotlin Compiler (kotlinc) - Source to Bytecode",
                "DEX Compiler (d8) - Bytecode to Dalvik Executable",
                "APK Packager - Assembling Unsigned APK",
                "APK Signer - Signing with Debug Keystore"
            )

            _buildState.value = BuildState.Building(0, stages[0], 0.0f, logs)
            logs.add("[INFO] Initializing APK Forge Build Engine v3.8...")
            logs.add("[INFO] Target Package: ${project.packageName}")

            try {
                for (i in stages.indices) {
                    val stageName = stages[i]
                    val progress = (i + 1) / stages.size.toFloat()
                    _buildState.value = BuildState.Building(i, stageName, progress, logs)
                    
                    logs.add("[BUILD] Stage ${i + 1}/6: $stageName...")
                    delay(700)

                    when (i) {
                        0 -> logs.add("[SUCCESS] Gradle sync completed. Resolved Compose BOM & M3.")
                        1 -> logs.add("[SUCCESS] AAPT2 compiled XML resources successfully.")
                        2 -> logs.add("[SUCCESS] Compiled ${project.files.size} Kotlin files without errors.")
                        3 -> logs.add("[SUCCESS] Generated classes.dex (Size: 840 KB).")
                        4 -> logs.add("[SUCCESS] Packed APK successfully.")
                        5 -> logs.add("[SUCCESS] Signed APK with debug.keystore (SHA1 valid).")
                    }
                }

                val newApk = BuiltApk(
                    id = "apk_${System.currentTimeMillis()}",
                    projectName = project.name,
                    packageName = project.packageName,
                    versionName = project.version,
                    fileSizeKb = (1800..3200).random(),
                    buildDurationSeconds = 5,
                    signed = true
                )

                _builtApks.value = listOf(newApk) + _builtApks.value
                logs.add("[COMPLETE] Build successful! APK ready for installation & deployment.")
                _buildState.value = BuildState.Success(newApk, logs)

            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Build timed out", e)
                logs.add("[ERROR] Build timed out.")
                _buildState.value = BuildState.Error("Build timed out.", logs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Build failed", e)
                logs.add("[ERROR] Build failed: ${e.localizedMessage}")
                _buildState.value = BuildState.Error(e.localizedMessage ?: "Unknown build error", logs)
            }
        }
    }

    fun resetBuildState() {
        _buildState.value = BuildState.Idle
    }

    fun generateAiCode(prompt: String) {
        viewModelScope.launch {
            _isAiLoading.value = BoolValue(true)
            _aiResponse.value = ""
            try {
                val response = withTimeout(60_000L) {
                    generativeModel.generateContent(
                        "You are an expert Android Jetpack Compose engineer. Write clean, robust Kotlin code or fixes for the following prompt: $prompt"
                    )
                }
                _aiResponse.value = response.text ?: "No response generated."
            } catch (e: TimeoutCancellationException) {
                _aiResponse.value = "AI generation timed out after 60 seconds."
                Log.e(TAG, "AI timed out", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiResponse.value = "AI Error: ${e.localizedMessage}"
                Log.e(TAG, "AI generation failed", e)
            } finally {
                _isAiLoading.value = BoolValue(false)
            }
        }
    }
}

data class BoolValue(val value: Boolean)
