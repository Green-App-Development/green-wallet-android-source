package com.green.wallet.presentation.main.dapp.trade.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.green.compose.buttons.DefaultButton
import com.green.compose.custom.FeeChoices
import com.green.compose.custom.FixedSpacer
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_100
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_14
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_16
import com.green.compose.dimens.size_18
import com.green.compose.dimens.size_20
import com.green.compose.dimens.size_26
import com.green.compose.dimens.size_300
import com.green.compose.dimens.size_500
import com.green.compose.dimens.size_6
import com.green.compose.dimens.size_80
import com.green.compose.dimens.size_9
import com.green.compose.dimens.text_12
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.dimens.text_16
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.dapp.trade.OfferViewState
import com.green.wallet.presentation.main.dapp.trade.models.OfferToken


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModelBottomSheetOffer(
    modifier: Modifier = Modifier,
    accountAddress: String = "",
    state: OfferViewState = OfferViewState(),
    spendableBalance: Double = 0.0,
    sheetState: ModalBottomSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
    sign: () -> Unit = {},
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetShape = RoundedCornerShape(topStart = size_16, topEnd = size_16),
        sheetState = sheetState,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Provider.current.primaryAppBackground)
                    .padding(
                        horizontal = size_16
                    ),
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
                Column(
                    modifier = Modifier
                        .background(
                            color = Provider.current.blackAppBackground,
                            shape = RoundedCornerShape(size_15)
                        )
                        .fillMaxWidth()
                        .padding(
                            top = size_18,
                            start = size_16,
                            end = size_16
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val offerStatus = if (state.acceptOffer) "Accept offer" else "Create offer"
                    DefaultText(
                        text = offerStatus,
                        size = text_16,
                        color = Provider.current.green,
                        fontWeight = FontWeight.W500
                    )
                    FixedSpacer(height = size_26)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DefaultText(
                            text = "Current account",
                            size = text_14,
                            color = Provider.current.green,
                            fontWeight = FontWeight.W500
                        )
                        DefaultText(
                            text = "xch1jihbwkl...4ciyc",
                            size = text_16,
                            color = Color.White,
                            fontWeight = FontWeight.W500
                        )
                    }
                    FixedSpacer(height = size_12)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(size_1)
                            .background(color = Provider.current.secondGrey),
                    )
                    FixedSpacer(height = size_12)
                    DefaultText(
                        text = "Estimated wallet balance change",
                        size = text_14,
                        color = Provider.current.green,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    FixedSpacer(height = size_12)
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(items = state.offered) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DefaultText(
                                    text = item.code,
                                    size = text_14,
                                    color = Provider.current.txtPrimaryColors,
                                    fontWeight = FontWeight.W500,
                                    textAlign = TextAlign.Start
                                )
                                val fromTokenClr =
                                    if (state.acceptOffer) Provider.current.green else Provider.current.errorColor
                                DefaultText(
                                    text = "${formattedDoubleAmountWithPrecision(item.assetAmount)} ${item.code}",
                                    size = text_14,
                                    color = fromTokenClr,
                                    fontWeight = FontWeight.W500,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(items = state.requested) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DefaultText(
                                    text = item.code,
                                    size = text_14,
                                    color = Provider.current.txtPrimaryColors,
                                    fontWeight = FontWeight.W500,
                                    textAlign = TextAlign.Start
                                )
                                val fromTokenClr =
                                    if (state.acceptOffer) Provider.current.errorColor else Provider.current.green
                                DefaultText(
                                    text = "${formattedDoubleAmountWithPrecision(item.assetAmount)} ${item.code}",
                                    size = text_14,
                                    color = fromTokenClr,
                                    fontWeight = FontWeight.W500,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                    FixedSpacer(height = size_15)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(size_1)
                            .background(color = Provider.current.secondGrey),
                    )
                    FixedSpacer(height = size_18)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        DefaultText(
                            text = "Spendable Balance: ",
                            size = text_12,
                            color = Provider.current.greyText
                        )
                        DefaultText(
                            text = formattedDoubleAmountWithPrecision(spendableBalance),
                            size = text_12,
                            color = Provider.current.greyText
                        )
                    }
                    FixedSpacer(height = size_6)
                    DefaultText(
                        text = "Комиссия сети",
                        size = text_14,
                        color = Provider.current.green,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    FixedSpacer(height = size_9)
                    FeeChoices(
                        feePos = {

                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FixedSpacer(height = size_14)
                    DefaultText(
                        text = "Custom",
                        size = text_14,
                        color = Provider.current.green,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    FixedSpacer(height = size_20)
                }
                FixedSpacer(height = size_20)
                DefaultButton(
                    bcgColor = Provider.current.green,
                    onClick = {
                        sign()
                    }) {
                    DefaultText(
                        text = "Sign",
                        size = text_15,
                        color = Provider.current.txtPrimaryColors
                    )
                }
                FixedSpacer(height = size_10)
            }
        }
    ) {

    }
}


@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun ModelBottomSheetAcceptOfferPreview() {
    GreenWalletTheme {
        ModelBottomSheetOffer(
            state = OfferViewState(
                acceptOffer = false,
                offered = listOf(
                    OfferToken("XCH", "", 0.00003),
                    OfferToken("GAD", "", 0.00003),
                    OfferToken("TIBET", "", 0.00003),
                ),
                requested = listOf(
                    OfferToken("XCH", "", 0.00003),
                    OfferToken("GAD", "", 0.00003),
                    OfferToken("TIBET", "", 0.00003),
                )
            )
        )
    }
}