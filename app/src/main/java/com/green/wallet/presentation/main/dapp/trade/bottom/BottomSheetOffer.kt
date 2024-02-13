package com.green.wallet.presentation.main.dapp.trade.bottom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.common.tools.formatString
import com.green.compose.buttons.DefaultButton
import com.green.compose.custom.fee.FeeContainer
import com.green.compose.custom.fee.FixedSpacer
import com.green.compose.dimens.size_1
import com.green.compose.dimens.size_10
import com.green.compose.dimens.size_100
import com.green.compose.dimens.size_12
import com.green.compose.dimens.size_130
import com.green.compose.dimens.size_15
import com.green.compose.dimens.size_16
import com.green.compose.dimens.size_18
import com.green.compose.dimens.size_20
import com.green.compose.dimens.size_26
import com.green.compose.dimens.size_300
import com.green.compose.dimens.size_4
import com.green.compose.dimens.size_6
import com.green.compose.dimens.size_80
import com.green.compose.dimens.size_9
import com.green.compose.dimens.text_12
import com.green.compose.dimens.text_14
import com.green.compose.dimens.text_15
import com.green.compose.dimens.text_16
import com.green.compose.extension.pxToDp
import com.green.compose.text.DefaultText
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.wallet.R
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.main.dapp.trade.OfferViewState
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.tools.VLog
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModelBottomSheetOffer(
    modifier: Modifier = Modifier,
    accountAddress: String = "",
    state: OfferViewState = OfferViewState(),
    sheetState: ModalBottomSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
    sign: () -> Unit = {},
    choseFee: (Double) -> Unit = {}
) {

    var spendableExpanded by remember { mutableStateOf(false) }
    val modifierLazy = remember { Modifier }
    var heightOfLazyColumn by remember { mutableStateOf(-1) }

    LaunchedEffect(heightOfLazyColumn) {
        if (heightOfLazyColumn != -1)
            modifierLazy.height(heightOfLazyColumn.dp)
    }

    ModalBottomSheetLayout(
        modifier = modifier
            .nestedScroll(rememberNestedScrollInteropConnection()),
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
                            .background(color = Provider.current.dividerOffer),
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

                    LazyColumn(
                        modifier = modifierLazy
                            .fillMaxWidth()
                            .heightIn(0.dp, size_300)
                            .onGloballyPositioned {
                                heightOfLazyColumn = pxToDp(it.size.height)
                            }
                    ) {
                        items(items = state.offered) { item ->
                            when (item) {
                                is CatToken -> {
                                    CatTokenItem(item, !state.acceptOffer)
                                }

                                is NftToken -> {
                                    NFTTokenDApp(item, !state.acceptOffer)
                                }
                            }
                            FixedSpacer(height = size_10)
                        }
                        items(items = state.requested) { item ->
                            when (item) {
                                is CatToken -> {
                                    CatTokenItem(item, state.acceptOffer)
                                }

                                is NftToken -> {
                                    NFTTokenDApp(item, state.acceptOffer)
                                }
                            }
                            FixedSpacer(height = size_10)
                        }
                    }

                    FixedSpacer(height = size_15)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(size_1)
                            .background(color = Provider.current.dividerOffer),
                    )
                    FixedSpacer(height = size_18)

                    val spendableBalanceTokens =
                        if (state.acceptOffer) state.requested else state.offered

                    if (spendableBalanceTokens.isNotEmpty())
                        SpendableBalance(
                            state = state,
                            expanded = spendableExpanded,
                            expand = {
                                VLog.d("Height of lazy column : $heightOfLazyColumn")
                                spendableExpanded = !spendableExpanded
                                heightOfLazyColumn -= 100
                            }
                        )

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

                    FeeContainer(
                        normal = state.dexieFee,
                        isEnough = state.feeEnough,
                        fee = choseFee
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
                        color = Provider.current.txtSecondaryColor
                    )
                }
                FixedSpacer(height = size_10)
            }
        }
    ) {

    }

}

@Composable
fun SpendableBalance(
    state: OfferViewState,
    expanded: Boolean = false,
    expand: (Int) -> Unit
) {
    val requested = if (state.acceptOffer) state.requested else state.offered
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (requested.size == 1) {
            val str = when (val item = requested[0]) {
                is CatToken -> {
                    "${item.code} ${item.spendableBalance}"
                }

                else -> ""
            }

            if (str.isNotEmpty()) {
                val clr = getColorOfSpendableBalanceCAT(state, requested[0] as CatToken)
                DefaultText(
                    text = "Spendable Balance: $str",
                    size = text_12,
                    color = clr
                )
            }
        } else {
            if (!expanded) {
                DefaultText(
                    text = "Spendable Balance:",
                    size = text_12,
                    color = Provider.current.greyText
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_downword),
                    contentDescription = null,
                    modifier = Modifier
                        .width(size_15)
                        .clickable {
                            expand(requested.size * 10)
                        }
                )
            } else {
                Column {
                    DefaultText(
                        text = "Spendable Balance:",
                        size = text_12,
                        color = Provider.current.greyText
                    )

                    for (i in requested.indices) {
                        val str = when (val item = requested[i]) {
                            is CatToken -> {
                                "${item.code} ${item.spendableBalance}"
                            }

                            else -> ""
                        }

                        if (str.isNotEmpty()) {
                            val clr = getColorOfSpendableBalanceCAT(state, requested[0] as CatToken)

                            DefaultText(
                                text = str,
                                size = text_12,
                                color = clr
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_upward),
                            contentDescription = null,
                            modifier = Modifier
                                .width(size_15)
                                .clickable {
                                    expand(0)
                                }
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getColorOfSpendableBalanceCAT(state: OfferViewState, catToken: CatToken): Color {
    return when (catToken.code) {
        "XCH" -> {
            val total = catToken.amount + state.chosenFee
            if (total <= catToken.spendableBalance) {
                Provider.current.greyText
            } else
                Provider.current.errorColor
        }

        else -> {
            if (catToken.amount <= catToken.spendableBalance) {
                Provider.current.greyText
            } else
                Provider.current.errorColor
        }
    }
}

@Composable
fun NftItem(nftToken: NftToken, acceptOffer: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(size_130)
    ) {
        GlideImage(
            imageModel = { nftToken.imgUrl },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            ),
            modifier = Modifier
                .size(size_130)
                .clip(RoundedCornerShape(size_10))
        )
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = size_15)
            ) {
                DefaultText(
                    text = nftToken.collection,
                    size = text_14,
                    color = Provider.current.secondPrimaryTextColor
                )
                FixedSpacer(height = size_12)
                DefaultText(
                    text = "NFT ID",
                    size = text_14,
                    color = Provider.current.green
                )
                FixedSpacer(height = size_4)
                DefaultText(
                    text = formatString(10, nftToken.nftId, 6),
                    size = text_14,
                    color = Provider.current.secondPrimaryTextColor
                )
            }

            val nftText = if (acceptOffer) "-1 NFT" else "+1 NFT"
            val nftColor =
                if (acceptOffer) Provider.current.errorColor else Provider.current.green
            DefaultText(
                text = nftText,
                size = text_15,
                color = nftColor,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun NFTTokenDApp(item: NftToken, acceptOffer: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DefaultText(
            text = item.collection,
            size = text_14,
            color = Provider.current.txtPrimaryColor,
            textAlign = TextAlign.Start
        )
        val nftText = if (acceptOffer) "-1 NFT" else "+1 NFT"
        val nftColor =
            if (acceptOffer) Provider.current.errorColor else Provider.current.green
        DefaultText(
            text = nftText,
            size = text_14,
            color = nftColor,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun CatTokenItem(item: CatToken, acceptOffer: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DefaultText(
            text = item.code,
            size = text_14,
            color = Provider.current.txtPrimaryColor,
            textAlign = TextAlign.Start
        )
        val fromTokenClr =
            if (acceptOffer) Provider.current.errorColor else Provider.current.green
        val sign = if (acceptOffer) "-" else "+"
        DefaultText(
            text = "$sign ${formattedDoubleAmountWithPrecision(item.amount)} ${item.code}",
            size = text_14,
            color = fromTokenClr,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Start
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun ModelBottomSheetAcceptOfferPreview() {
    GreenWalletTheme {
        ModelBottomSheetOffer(
            state = OfferViewState(
                acceptOffer = true,
                offered = listOf(
                    CatToken("XCH", "", 0.00003),
                    CatToken("GAD", "", 0.00003)
                ),
                requested = listOf(
                    CatToken("XCC", "", 0.0013),
                    CatToken("GWT", "", 0.11),
                    CatToken("CHIA", "", 0.456)
                )
            )
        )
    }
}