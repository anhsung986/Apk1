package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AndroidProject
import com.example.model.BuildState
import com.example.model.BuiltApk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildPipelineScreen(
    project: AndroidProject,
    buildState: BuildState,
    onStartBuild: () -> Unit,
    onViewApkVault: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build APK: ${project.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Refresh, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (val state = buildState) {
                is BuildState.Idle -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ready to compile ${project.name} into an APK", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onStartBuild, modifier = Modifier.fillMaxWidth(0.6f)) {
                            Text("Start Build Pipeline")
                        }
                    }
                }
                is BuildState.Building -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Stage ${state.stageIndex + 1}/6: ${state.stageName}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    BuildConsoleLogs(logs = state.logs)
                }
                is BuildState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Build Successful!", style = MaterialTheme.typography.titleLarge)
                        Text("APK Size: ${state.apk.fileSizeKb} KB | Signed with debug keystore", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onViewApkVault) {
                            Text("View in APK Vault")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    BuildConsoleLogs(logs = state.logs)
                }
                is BuildState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Build Failed", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onStartBuild) {
                            Text("Retry Build")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    BuildConsoleLogs(logs = state.logs)
                }
            }
        }
    }
}

@Composable
fun BuildConsoleLogs(logs: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { log ->
                val color = when {
                    log.contains("[SUCCESS]") || log.contains("[COMPLETE]") -> Color(0xFF4ADE80)
                    log.contains("[ERROR]") -> Color(0xFFF87171)
                    log.contains("[BUILD]") -> Color(0xFF38BDF8)
                    else -> Color(0xFF94A3B8)
                }
                Text(
                    text = log,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
