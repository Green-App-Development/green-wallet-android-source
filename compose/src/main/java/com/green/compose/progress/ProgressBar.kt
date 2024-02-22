package com.green.compose.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.green.compose.dimens.size_4
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider


@Composable
fun CircularProgressBar(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp
) {

    CircularProgressIndicator(
        modifier = Modifier.size(size),
        color = Provider.current.green,
        strokeWidth = size_4,
        strokeCap = StrokeCap.Round,
        trackColor = Provider.current.progressTrackColor
    )

}

@Preview(showBackground = true)
@Composable
fun ProgressBarPreview() {
    GreenWalletTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            CircularProgressBar()
        }
    }
}