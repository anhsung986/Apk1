package com.example.model

data class ProjectFile(
    val name: String,
    val path: String,
    val content: String,
    val type: FileType = FileType.KOTLIN
)

enum class FileType {
    KOTLIN, XML, GRADLE, MANIFEST, ASSET
}

data class AndroidProject(
    val id: String,
    val name: String,
    val packageName: String,
    val description: String,
    val version: String = "1.0",
    val files: List<ProjectFile>,
    val updatedAt: Long = System.currentTimeMillis()
)

data class BuiltApk(
    val id: String,
    val projectName: String,
    val packageName: String,
    val versionName: String,
    val fileSizeKb: Int,
    val buildDate: Long = System.currentTimeMillis(),
    val buildDurationSeconds: Int,
    val signed: Boolean = true
)

sealed interface BuildState {
    data object Idle : BuildState
    data class Building(val stageIndex: Int, val stageName: String, val progress: Float, val logs: List<String>) : BuildState
    data class Success(val apk: BuiltApk, val logs: List<String>) : BuildState
    data class Error(val message: String, val logs: List<String>) : BuildState
}
