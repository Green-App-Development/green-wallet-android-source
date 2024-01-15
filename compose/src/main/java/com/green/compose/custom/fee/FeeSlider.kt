package com.green.compose.custom.fee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.R
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_100
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_13
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_16
import com.green.compose.dimens.size_2
import com.green.compose.dimens.size_4
import com.green.compose.dimens.size_5
import com.green.compose.dimens.size_6
import com.green.compose.dimens.size_8
import com.green.compose.dimens.text_16
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.compose.utils.PRECISION_XCH
import com.green.compose.utils.formattedDoubleAmountWithPrecision


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseFeeProgressValue(
    modifier: Modifier = Modifier,
    initialValue: Long = 0L,
    spendableBalance: Double = 0.0,
    maxValue: Long = 1_000_000_000L,
    onFee: (Double) -> Unit = {},
    onX: () -> Unit = {},
) {
    var curValue by remember { mutableLongStateOf(initialValue) }
    var feeEnough by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .background(Provider.current.feeBackground, shape = RoundedCornerShape(size_8))
            .fillMaxWidth()
            .height(size_100)
            .padding(
                top = size_4,
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = size_15, start = size_8, top = size_1),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DefaultText(
                text = "$curValue",
                size = text_16,
                color = if (feeEnough) Provider.current.txtPrimaryColor
                else Provider.current.errorColor
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_x),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(end = size_5, top = size_2)
                    .size(size_13)
                    .clickable {
                        onX()
                    }
            )
        }

        androidx.compose.material3.Slider(
            value = curValue.toFloat(),
            onValueChange = {
                curValue = it.toLong()
                onFee(curValue / PRECISION_XCH)
                feeEnough = spendableBalance >= curValue / PRECISION_XCH
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = size_6, bottom = size_10)
                .height(size_16),
            valueRange = initialValue.toFloat()..maxValue.toFloat(),
            colors = androidx.compose.material3.SliderDefaults.colors(
                activeTrackColor = Provider.current.green,
                inactiveTrackColor = Provider.current.green,
                thumbColor = Provider.current.green
            ),
            thumb = {
                Box(modifier = Modifier.fillMaxHeight()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(size_15)
                            .background(color = Color.White, shape = CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(size_12)
                            .background(color = Provider.current.green, shape = CircleShape)
                    )
                }
            }
        )

        val feePrecision = formattedDoubleAmountWithPrecision(curValue / PRECISION_XCH)

        DefaultText(
            text = "= $feePrecision XCH",
            size = text_16,
            color = if (feeEnough) Provider.current.txtPrimaryColor
            else Provider.current.errorColor,
            modifier = Modifier.padding(start = size_8)
        )

        LaunchedEffect(true) {
            onFee(curValue / PRECISION_XCH)
            feeEnough = spendableBalance >= curValue / PRECISION_XCH
        }

    }
}

@Preview
@Composable
fun ChooseFeeProgressValuePreview() {
    GreenWalletTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ChooseFeeProgressValue()
        }
    }
}