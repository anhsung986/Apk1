package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.BuiltApk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkVaultScreen(
    apks: List<BuiltApk>,
    onBack: () -> Unit
) {
    var selectedApkForSim by remember { mutableStateOf<BuiltApk?>(null) }
    var downloadingApk by remember { mutableStateOf<BuiltApk?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadedMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APK Vault & Archive") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Compiled APK Deliverables", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            if (apks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No APKs built yet. Open a project and run the Build Pipeline!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(apks) { apk ->
                        ApkCard(
                            apk = apk,
                            onSimulateRun = { selectedApkForSim = apk },
                            onDownload = {
                                downloadingApk = apk
                                downloadProgress = 0f
                                scope.launch {
                                    for (i in 1..10) {
                                        delay(150)
                                        downloadProgress = i / 10f
                                    }
                                    downloadingApk = null
                                    downloadedMessage = "Successfully downloaded ${apk.projectName}-v${apk.versionName}.apk to /Download/"
                                }
                            },
                            onShare = {
                                downloadedMessage = "Sharing APK file: ${apk.projectName}.apk"
                            }
                        )
                    }
                }
            }
        }
    }

    downloadingApk?.let { apk ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Downloading APK...") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Downloading ${apk.projectName}.apk (${apk.fileSizeKb} KB)")
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { downloadProgress }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(downloadProgress * 100).toInt()}%")
                }
            },
            confirmButton = {}
        )
    }

    downloadedMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { downloadedMessage = null },
            title = { Text("APK Forge Notification") },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = { downloadedMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    selectedApkForSim?.let { apk ->
        AlertDialog(
            onDismissRequest = { selectedApkForSim = null },
            title = { Text("App Simulator: ${apk.projectName}") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Package: ${apk.packageName}")
                    Text("Version: ${apk.versionName}")
                    Text("Size: ${apk.fileSizeKb} KB (Signed & Verified)")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("App compiled and executed successfully inside APK Forge virtual runtime.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { selectedApkForSim = null }) {
                    Text("Close Simulator")
                }
            }
        )
    }
}

@Composable
fun ApkCard(apk: BuiltApk, onSimulateRun: () -> Unit, onDownload: () -> Unit, onShare: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(apk.projectName, style = MaterialTheme.typography.titleMedium)
                }
                Badge { Text("v${apk.versionName}") }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(apk.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Size: ${apk.fileSizeKb} KB", style = MaterialTheme.typography.bodyMedium)
                Text("Signed: ${if (apk.signed) "Yes (Debug)" else "No"}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onSimulateRun, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run")
                }
                Button(onClick = onDownload, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download")
                }
                OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        }
    }
}
