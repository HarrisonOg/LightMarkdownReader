package com.harrisonog.lightmarkdownreader.ui.components

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

    val markwon = remember {
        Markwon.create(context)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextIsSelectable(true)
                setTextColor(textColor)
                setBackgroundColor(backgroundColor)
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.setBackgroundColor(backgroundColor)
            markwon.setMarkdown(textView, markdown)
        }
    )
}
