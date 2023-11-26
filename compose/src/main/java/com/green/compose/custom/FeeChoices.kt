package com.green.compose.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.dimens.size_2
import com.green.compose.dimens.size_5
import com.green.compose.dimens.size_54
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider


@Composable
fun FeeChoices(
    modifier: Modifier = Modifier,
    feePos: (Int) -> Unit = {}
) {
    var chosen by remember { mutableIntStateOf(1) }

    LaunchedEffect(chosen) {
        feePos(chosen)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(size_54)
            .background(color = Provider.current.feeBackground, shape = RoundedCornerShape(size_5))
            .padding(
                vertical = size_5,
                horizontal = size_2
            )
    ) {

        Column(
            modifier = Modifier
                .customFeeChosenBackground(0, chosen)
                .weight(0.33f)
                .clickable {
                    chosen = 0
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DefaultText(
                text = "Long",
                size = text_14,
                color = getColorFeeText(chosen = 0 == chosen)
            )
            DefaultText(
                text = "0 XCH",
                size = text_15,
                color = getColorFeeAmount(chosen = 0 == chosen)
            )
        }

        Column(
            modifier = Modifier
                .customFeeChosenBackground(1, chosen)
                .weight(0.33f)
                .clickable {
                    chosen = 1
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DefaultText(
                text = "Medium",
                size = text_14,
                color = getColorFeeText(chosen = 1 == chosen)
            )
            DefaultText(
                text = "0.00005 XCH",
                size = text_15,
                color = getColorFeeAmount(chosen = 1 == chosen)
            )
        }

        Column(
            modifier = Modifier
                .customFeeChosenBackground(2, chosen)
                .weight(0.33f)
                .clickable {
                    chosen = 2
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DefaultText(
                text = "Short",
                size = text_14,
                color = getColorFeeText(chosen = 2 == chosen)
            )
            DefaultText(
                text = "0.0005 XCH",
                size = text_15,
                color = getColorFeeAmount(chosen = 2 == chosen)
            )
        }
    }
}

@Composable
internal fun Modifier.customFeeChosenBackground(pos: Int, curPos: Int): Modifier {
    val color = if (pos == curPos) Provider.current.feeBackgroundChoosen else Color.Unspecified
    return background(
        color = color,
        shape = RoundedCornerShape(size_5)
    )
}

@Composable
internal fun getColorFeeText(chosen: Boolean): Color {
    if (chosen)
        return Provider.current.green
    return Provider.current.greyText
}

@Composable
internal fun getColorFeeAmount(chosen: Boolean): Color {
    if (chosen)
        return Color.White
    return Provider.current.greyText
}


@Preview(showBackground = true)
@Composable
fun FeeChoicesPreview() {
    GreenWalletTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {
            FeeChoices()
        }
    }
}