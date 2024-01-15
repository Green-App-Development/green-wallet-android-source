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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.green.compose.utils.doubleCeilString


@Composable
fun FeeChoices(
    modifier: Modifier = Modifier,
    normal: Double = 0.001,
    spendableBalance: Double = 0.1,
    fee: (Double) -> Unit = {}
) {
    var chosen by remember { mutableIntStateOf(1) }

    LaunchedEffect(chosen) {
        val chosenFee = getFeeOnChoosePos(chosen)
        fee(chosenFee)
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

            val feeAmount = doubleCeilString(normal)

            DefaultText(
                text = "Normal",
                size = text_14,
                color = getColorFeeText(chosen = 0 == chosen, feeAmount.toDouble() <= spendableBalance)
            )
            DefaultText(
                text = "$feeAmount XCH",
                size = text_15,
                color = getColorFeeAmount(chosen = 0 == chosen, feeAmount.toDouble() <= spendableBalance)
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
            val feeAmount = doubleCeilString(normal*1.5)
            DefaultText(
                text = "Medium",
                size = text_14,
                color = getColorFeeText(chosen = 1 == chosen, feeAmount.toDouble() <= spendableBalance)
            )
            DefaultText(
                text = "$feeAmount XCH",
                size = text_15,
                color = getColorFeeAmount(chosen = 1 == chosen, feeAmount.toDouble() <= spendableBalance)
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
            val feeAmount = doubleCeilString(normal*2)
            DefaultText(
                text = "Fast",
                size = text_14,
                color = getColorFeeText(chosen = 2 == chosen, feeAmount.toDouble() <= spendableBalance)
            )
            DefaultText(
                text = "$feeAmount XCH",
                size = text_15,
                color = getColorFeeAmount(chosen = 2 == chosen, feeAmount.toDouble() <= spendableBalance)
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

private fun getFeeOnChoosePos(pos: Int): Double {
    return when (pos) {
        0 -> 0.0
        1 -> 0.00005
        2 -> 0.0006
        else -> 0.0
    }
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