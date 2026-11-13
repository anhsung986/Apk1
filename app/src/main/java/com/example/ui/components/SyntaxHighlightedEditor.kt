package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileType

@Composable
fun SyntaxHighlightedEditor(
    text: String,
    fileType: FileType,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val highlightedText = remember(text, fileType) {
        highlightCode(text, fileType)
    }

    val lineCount = remember(text) { text.lineCount().coerceAtLeast(1) }

    Row(
        modifier = modifier
            .background(Color(0xFF0F172A))
            .fillMaxSize()
    ) {
        // Line numbers column
        Column(
            modifier = Modifier
                .background(Color(0xFF1E293B))
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .verticalScroll(verticalScrollState)
        ) {
            for (i in 1..lineCount) {
                Text(
                    text = i.toString(),
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.height(20.dp)
                )
            }
        }

        // Code input & highlight box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(12.dp)
        ) {
            TextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF00E5FF)
                ),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

fun highlightCode(text: String, fileType: FileType): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        for ((index, line) in lines.withIndex()) {
            if (index > 0) append("\n")
            if (fileType == FileType.XML || fileType == FileType.MANIFEST) {
                highlightXmlLine(line)
            } else {
                highlightKotlinLine(line)
            }
        }
    }
}

private fun AnnotatedString.Builder.highlightKotlinLine(line: String) {
    val keywords = listOf("package", "import", "val", "var", "fun", "class", "object", "return", "if", "else", "when", "for", "while", "true", "false", "Composable")
    val words = line.split(Regex("(?<=\\W)|(?=\\W)"))
    for (word in words) {
        when {
            word.startsWith("//") || word.startsWith("/*") -> {
                withStyle(SpanStyle(color = Color(0xFF64748B))) { append(word) }
            }
            word.startsWith("\"") && word.endsWith("\"") -> {
                withStyle(SpanStyle(color = Color(0xFF4ADE80))) { append(word) }
            }
            keywords.contains(word) -> {
                withStyle(SpanStyle(color = Color(0xFFC084FC))) { append(word) }
            }
            word.toIntOrNull() != null -> {
                withStyle(SpanStyle(color = Color(0xFFFB923C))) { append(word) }
            }
            else -> {
                withStyle(SpanStyle(color = Color(0xFFE2E8F0))) { append(word) }
            }
        }
    }
}

private fun AnnotatedString.Builder.highlightXmlLine(line: String) {
    val trimmed = line.trim()
    when {
        trimmed.startsWith("<!--") -> {
            withStyle(SpanStyle(color = Color(0xFF64748B))) { append(line) }
        }
        trimmed.startsWith("<") && trimmed.endsWith(">") -> {
            withStyle(SpanStyle(color = Color(0xFF38BDF8))) { append(line) }
        }
        else -> {
            withStyle(SpanStyle(color = Color(0xFFE2E8F0))) { append(line) }
        }
    }
}

private fun String.lineCount(): Int {
    return this.split("\n").size
}
