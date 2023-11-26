package com.green.compose.buttons

import android.service.autofill.OnClickAction
import android.text.style.BackgroundColorSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.dimens.size_30
import com.green.compose.dimens.size_50


@Composable
fun DefaultButton(
    modifier: Modifier = Modifier,
    bcgColor: Color = Color.Blue,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(size_50)
            .background(
                color = bcgColor,
                shape = RoundedCornerShape(size_30)
            ),
        colors = ButtonDefaults.buttonColors(bcgColor),
        onClick = {
            onClick()
        }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun DefaultButtonPreview() {

}