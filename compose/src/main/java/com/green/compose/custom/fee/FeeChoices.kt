package com.green.compose.custom.fee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.dimens.size_2
import com.green.compose.dimens.size_4
import com.green.compose.dimens.size_5
import com.green.compose.dimens.size_54
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.compose.utils.formattedDoubleAmountWithPrecision
import kotlinx.coroutines.delay


@Composable
fun FeeChoices(
    modifier: Modifier = Modifier,
    normal: Double = 0.001,
    chosen: Int = 0,
    isEnough: Boolean = false,
    fee: (Double) -> Unit = {},
    onChosen: (Int) -> Unit = {}
) {

    val feeAmount1 = formattedDoubleAmountWithPrecision(normal, 6)
    val feeAmount2 = formattedDoubleAmountWithPrecision(normal * 2, 6)
    val feeAmount3 = formattedDoubleAmountWithPrecision(normal * 3, 6)

    LaunchedEffect(normal) {
        fee(feeAmount1.toDouble())
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(size_54)
            .background(color = Provider.current.feeBackground, shape = RoundedCornerShape(size_5))
            .padding(
                horizontal = size_4
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier
                .customFeeChosenBackground(0, chosen)
                .weight(0.33f)
                .clickable {
                    onChosen(0)
                    fee(feeAmount1.toDouble())
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            DefaultText(
                text = "Normal",
                size = text_14,
                color = getColorFeeText(
                    chosen = 0 == chosen,
                    isEnough
                )
            )
            DefaultText(
                text = "$feeAmount1 XCH",
                size = text_15,
                color = getColorFeeAmount(
                    chosen = 0 == chosen,
                    isEnough
                )
            )
        }

        Column(
            modifier = Modifier
                .customFeeChosenBackground(1, chosen)
                .weight(0.33f)
                .clickable {
                    onChosen(1)
                    fee(feeAmount2.toDouble())
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DefaultText(
                text = "Medium",
                size = text_14,
                color = getColorFeeText(
                    chosen = 1 == chosen,
                    isEnough
                )
            )
            DefaultText(
                text = "$feeAmount2 XCH",
                size = text_15,
                color = getColorFeeAmount(
                    chosen = 1 == chosen,
                    isEnough
                )
            )
        }

        Column(
            modifier = Modifier
                .customFeeChosenBackground(2, chosen)
                .weight(0.33f)
                .clickable {
                    onChosen(2)
                    fee(feeAmount3.toDouble())
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DefaultText(
                text = "Fast",
                size = text_14,
                color = getColorFeeText(
                    chosen = 2 == chosen,
                    isEnough
                )
            )
            DefaultText(
                text = "$feeAmount3 XCH",
                size = text_15,
                color = getColorFeeAmount(
                    chosen = 2 == chosen,
                    isEnough
                )
            )
        }
    }
}

@Composable
internal fun Modifier.customFeeChosenBackground(pos: Int, curPos: Int): Modifier {
    val color = if (pos == curPos) Provider.current.feeBackgroundChosen else Color.Unspecified
    return background(
        color = color,
        shape = RoundedCornerShape(size_5)
    )
}

@Composable
internal fun getColorFeeText(chosen: Boolean, enough: Boolean): Color {
    if (chosen) {
        return if (enough) Provider.current.green else Provider.current.errorColor
    }
    return Provider.current.greyText
}

@Composable
internal fun getColorFeeAmount(chosen: Boolean, isEnough: Boolean): Color {
    if (chosen)
        return if (isEnough) Color.White else Provider.current.errorColor
    return Provider.current.greyText
}

@Preview(showBackground = true)
@Composable
fun FeeChoicesPreview() {
    GreenWalletTheme {
        Column(
            modifier = Modifier
                .background(color = Color.White)
        ) {
            FeeChoices()
        }
    }
}