package com.green.compose.text

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit


@Composable
fun DefaultText(
    modifier: Modifier = Modifier,
    text: String,
    size: TextUnit,
    color: Color,
    fontWeight: FontWeight = FontWeight(400),
    textAlign: TextAlign = TextAlign.Start,
    textDecoration: TextDecoration = TextDecoration.None,
    isSingleLine: Boolean = true,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        text = text,
        fontSize = size,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        modifier = modifier,
        textDecoration = textDecoration,
        maxLines = maxLines,
        overflow = overflow
    )
}