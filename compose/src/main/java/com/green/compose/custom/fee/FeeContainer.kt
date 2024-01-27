package com.green.compose.custom.fee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.dimens.size_18
import com.green.compose.dimens.text_14
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.compose.utils.PRECISION_XCH
import kotlinx.coroutines.delay


@Composable
fun FeeContainer(
    normal: Double = 0.0005,
    isEnough: Boolean = false,
    fee: (Double) -> Unit = {}
) {

    var chosenPos by remember { mutableIntStateOf(0) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        var customShown by remember { mutableStateOf(false) }
        if (customShown) {
            ChooseFeeProgressValue(
                initialValue = (normal * PRECISION_XCH).toLong(),
                maxValue = (normal * PRECISION_XCH).toLong() * 2,
                onX = {
                    customShown = false
                },
                isEnough = isEnough,
                onFee = fee
            )
        } else {
            FeeChoices(
                normal = normal,
                fee = fee,
                isEnough = isEnough,
                onChosen = {
                    chosenPos = it
                },
                chosen = chosenPos
            )
            DefaultText(
                text = "Custom",
                size = text_14,
                color = Provider.current.green,
                fontWeight = FontWeight.W500,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        customShown = true
                    }
                    .padding(top = size_18),
                textAlign = TextAlign.Start
            )
        }
    }
}


@Preview
@Composable
fun FeeContainerPreview() {
    GreenWalletTheme {
        FeeContainer()
    }
}