package com.green.compose.bottom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.R
import com.green.compose.buttons.DefaultButton
import com.green.compose.custom.FixedSpacer
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_100
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_16
import com.green.compose.dimens.size_26
import com.green.compose.dimens.size_32
import com.green.compose.dimens.size_4
import com.green.compose.dimens.size_40
import com.green.compose.dimens.size_6
import com.green.compose.dimens.size_62
import com.green.compose.dimens.size_8
import com.green.compose.dimens.size_80
import com.green.compose.dimens.text_15
import com.green.compose.dimens.text_18
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetDialogConnect(
    url: String = "",
    address: String = "",
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
    connect: () -> Unit
) {
    ModalBottomSheetLayout(
        sheetContent = {

        },
        sheetState = sheetState
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = size_16, topEnd = size_16))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Provider.current.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FixedSpacer(height = size_12)
                Box(
                    modifier = Modifier
                        .width(size_80)
                        .height(size_6)
                        .background(
                            color = Provider.current.iconGrey,
                            shape = RoundedCornerShape(size_100)
                        )
                )
                FixedSpacer(height = size_16)
                DefaultText(
                    text = "Подключение к dexie.space",
                    size = text_18,
                    color = Provider.current.green,
                    fontWeight = FontWeight(600)
                )

                FixedSpacer(height = size_10)
                DefaultText(
                    text = "dexie.space запрашивает  доступ к вашему \n кошельку xch1j864768...49898yc",
                    size = text_15,
                    color = Provider.current.greyText,
                    fontWeight = FontWeight(600),
                    textAlign = TextAlign.Center
                )

                FixedSpacer(height = size_26)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_green_wallet_jpeg),
                        contentDescription = null,
                        modifier = Modifier.size(size_80)
                    )
                    val barColor = Provider.current.iconGrey
                    Canvas(
                        modifier = Modifier
                            .width(size_62)
                            .height(size_4)
                    ) {
                        repeat(9) {
                            drawRect(
                                color = barColor,
                                topLeft = Offset(it * 20f, 0f),
                                size = Size(10f, 10f)
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.img_dexie_jpeg),
                        contentDescription = null,
                        modifier = Modifier.size(size_80)
                    )
                }
                FixedSpacer(height = size_40)
                DefaultButton(
                    bcgColor = Provider.current.green,
                    modifier = Modifier
                        .padding(horizontal = size_32),
                    onClick = {

                    }
                ) {
                    DefaultText(
                        text = "Connect wallet",
                        size = text_15,
                        color = Color.White
                    )
                }
                FixedSpacer(height = size_8)
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun BtmSheetDialogConnectPreview() {
    GreenWalletTheme {
        BottomSheetDialogConnect {

        }
    }
}