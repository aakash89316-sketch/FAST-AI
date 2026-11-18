package com.example.chat

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.ui.theme.FastAiHeaderBlue

@Composable
fun FormattedMessageText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    val annotated = remember(text) { formatBasicMarkdown(text) }
    Text(
        text = annotated,
        color = color,
        style = style,
        modifier = modifier
    )
}

private fun formatBasicMarkdown(raw: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = raw.lines()
        lines.forEachIndexed { lineIndex, line ->
            val trimmed = line.trimStart()
            val formattedLine = when {
                trimmed.startsWith("### ") -> "• " + trimmed.removePrefix("### ")
                trimmed.startsWith("## ") -> trimmed.removePrefix("## ")
                trimmed.startsWith("# ") -> trimmed.removePrefix("# ")
                trimmed.startsWith("* ") -> "• " + trimmed.removePrefix("* ")
                trimmed.startsWith("- ") -> "• " + trimmed.removePrefix("- ")
                else -> line
            }
            appendInlineMarkdown(formattedLine)
            if (lineIndex < lines.lastIndex) {
                append('\n')
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(line: String) {
    var index = 0
    while (index < line.length) {
        if (line.startsWith("**", index)) {
            val end = line.indexOf("**", startIndex = index + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(line.substring(index + 2, end))
                }
                index = end + 2
                continue
            }
        }
        if (line[index] == '`') {
            val end = line.indexOf('`', startIndex = index + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color = FastAiHeaderBlue
                    )
                ) {
                    append(line.substring(index + 1, end))
                }
                index = end + 1
                continue
            }
        }
        append(line[index])
        index++
    }
}
