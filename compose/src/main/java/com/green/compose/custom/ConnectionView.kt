package com.green.compose.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.dimens.size_4
import com.green.compose.dimens.size_62


@Composable
fun ConnectionPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Canvas(
            modifier = Modifier
                .width(size_62)
                .height(size_4)
                .background(color = Color.Blue)
        ) {
            repeat(8) {
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(it * 20f, 0f),
                    size = Size(10f, 10f)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ConnectionsPreview() {
    ConnectionPreview()
}