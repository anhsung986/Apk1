package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.AndroidProject
import com.example.model.ProjectFile
import com.example.ui.components.SyntaxHighlightedEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectEditorScreen(
    project: AndroidProject,
    currentFile: ProjectFile?,
    onSelectFile: (ProjectFile) -> Unit,
    onUpdateFileContent: (String) -> Unit,
    onStartBuild: () -> Unit,
    onNavigateToAi: () -> Unit,
    onBack: () -> Unit
) {
    var textContent by remember(currentFile?.path) { mutableStateOf(currentFile?.content ?: "") }

    LaunchedEffect(currentFile) {
        textContent = currentFile?.content ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Code, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAi) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = onStartBuild,
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Build APK")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = project.files.indexOfFirst { it.path == currentFile?.path }.coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            ) {
                project.files.forEach { file ->
                    Tab(
                        selected = file.path == currentFile?.path,
                        onClick = { onSelectFile(file) },
                        text = { Text(file.name) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (currentFile != null) {
                    SyntaxHighlightedEditor(
                        text = textContent,
                        fileType = currentFile.type,
                        onTextChanged = { newVal ->
                            textContent = newVal
                            onUpdateFileContent(newVal)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No file selected")
                    }
                }
            }
        }
    }
}
