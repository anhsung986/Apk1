package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ApkForgeViewModel
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureAppCheck(this, intent)

        setContent {
            MyApplicationTheme {
                val viewModel: ApkForgeViewModel = viewModel()
                val projects by viewModel.projects.collectAsState()
                val currentProject by viewModel.currentProject.collectAsState()
                val currentFile by viewModel.currentFile.collectAsState()
                val buildState by viewModel.buildState.collectAsState()
                val builtApks by viewModel.builtApks.collectAsState()
                val aiResponse by viewModel.aiResponse.collectAsState()
                val isAiLoading by viewModel.isAiLoading.collectAsState()

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen is Screen.Dashboard || currentScreen is Screen.ApkVault) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Dashboard,
                                    onClick = { currentScreen = Screen.Dashboard },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Projects") },
                                    label = { Text("Projects") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.ApkVault,
                                    onClick = { currentScreen = Screen.ApkVault },
                                    icon = { Icon(Icons.Default.Storage, contentDescription = "APK Vault") },
                                    label = { Text("APK Vault") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            is Screen.Dashboard -> {
                                DashboardScreen(
                                    projects = projects,
                                    onSelectProject = { viewModel.selectProject(it) },
                                    onNavigateToBuilder = { currentScreen = Screen.Editor },
                                    onCreateNewProject = { name, pkg, desc ->
                                        viewModel.addNewProject(name, pkg, desc)
                                        currentScreen = Screen.Editor
                                    }
                                )
                            }
                            is Screen.Editor -> {
                                currentProject?.let { proj ->
                                    ProjectEditorScreen(
                                        project = proj,
                                        currentFile = currentFile,
                                        onSelectFile = { viewModel.selectFile(it) },
                                        onUpdateFileContent = { viewModel.updateFileContent(it) },
                                        onStartBuild = {
                                            viewModel.startBuild(proj)
                                            currentScreen = Screen.BuildPipeline
                                        },
                                        onNavigateToAi = { currentScreen = Screen.AiAssistant },
                                        onBack = { currentScreen = Screen.Dashboard }
                                    )
                                }
                            }
                            is Screen.BuildPipeline -> {
                                currentProject?.let { proj ->
                                    BuildPipelineScreen(
                                        project = proj,
                                        buildState = buildState,
                                        onStartBuild = { viewModel.startBuild(proj) },
                                        onViewApkVault = { currentScreen = Screen.ApkVault },
                                        onBack = { currentScreen = Screen.Editor }
                                    )
                                }
                            }
                            is Screen.AiAssistant -> {
                                AiAssistantScreen(
                                    aiResponse = aiResponse,
                                    isLoading = isAiLoading.value,
                                    onGenerate = { viewModel.generateAiCode(it) },
                                    onBack = { currentScreen = Screen.Editor }
                                )
                            }
                            is Screen.ApkVault -> {
                                ApkVaultScreen(
                                    apks = builtApks,
                                    onBack = { currentScreen = Screen.Dashboard }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configureAppCheck(context: Context, intent: Intent?) {
        try {
            com.google.firebase.FirebaseApp.initializeApp(context)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            val prefs = context.getSharedPreferences("app_check_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("initialized", true).commit()
            Log.d("AppCheck", "App Check initialized successfully with Debug Provider.")
        } catch (e: Exception) {
            Log.e("AppCheck", "Failed to initialize App Check", e)
        }
    }
}

sealed interface Screen {
    data object Dashboard : Screen
    data object Editor : Screen
    data object BuildPipeline : Screen
    data object AiAssistant : Screen
    data object ApkVault : Screen
}
